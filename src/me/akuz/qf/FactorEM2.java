package me.akuz.qf;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.DiagMatrix;
import me.akuz.core.math.GaussianFunc;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

/**
 * Factor model EM inference.
 *
 */
public final class FactorEM2 {
	
	private static final double INIT_W_RANGE = 0.3;

	private final Matrix _mX;
	private final int    _factorCount;
	private final int    _variableCount;
	private Matrix[]     _xExpectedFactor;
	private Matrix[]     _xExpectedFactorFactor;
	private Matrix       _pFactorBias;
	private DiagMatrix   _pFactorPhi;
	private Matrix       _pW;
	private Matrix       _pVariableBias;
	private DiagMatrix   _pVariableKsi;
	private Matrix       _C;
	private Matrix       _CNoKsi;
	private double       _logLike;
	
	public FactorEM2(
			final Matrix mX, 
			final int factorCount,
			final boolean randomW) {
		
		if (mX == null || mX.getRowDimension() == 0 || mX.getColumnDimension() == 0) {
			throw new IllegalArgumentException("Matrix X must not be null or empty");
		}
		if (factorCount < 1) {
			throw new IllegalArgumentException("Factor count must be positive");
		}
		_mX = mX;
		_factorCount = factorCount;
		_variableCount = mX.getColumnDimension();
		
		// init parameters
		final Random rnd = ThreadLocalRandom.current();
		_pFactorBias = new Matrix(_factorCount, 1);
		_pFactorPhi = new DiagMatrix(_factorCount);
		for (int f=0; f<_factorCount; f++) {
			_pFactorPhi.setDiag(f, 1.0);
		}
		if (randomW) {
			_pW = new Matrix(_variableCount, _factorCount);
			for (int v=0; v<_variableCount; v++) {
				for (int f=0; f<_factorCount; f++) {
					_pW.set(v, f, INIT_W_RANGE * (1.0 - 2.0 * rnd.nextDouble()));
				}
			}
		} else {
			_pW = initNonRandomW();
		}
		_pVariableBias = new Matrix(_variableCount, 1);
		_pVariableKsi = new DiagMatrix(_variableCount);
		for (int v=0; v<_variableCount; v++) {
			_pVariableKsi.setDiag(v, 1.0);
		}
		updateC();
		_logLike = Double.NaN;
	}
	
	private final Matrix initNonRandomW() {
		
		Matrix W = new Matrix(_variableCount, _factorCount);
		
		for (int j=0; j<_factorCount; j++) {

			final double lenPi = 2.0 * _variableCount / (double)(j+1);
			
			for (int i=0; i<_variableCount; i++) {
				
				final double rad = Math.PI / lenPi * (double)i;
				
				final double value = 1.0 / Math.log(Math.E + j) + Math.cos(rad);
				
				W.set(i, j, value);
			}
		}
		
		return W;
	}
	
	public void execute(final int startRow, final int endRow, final int iterationCount) {
		
		if (startRow + 1 >= endRow) {
			throw new IllegalArgumentException("Arguments must satisfy: startRow + 1 < endRow (at least two rows)");
		}

		if (_xExpectedFactor == null) {
			_xExpectedFactor = new Matrix[_mX.getRowDimension()];
			_xExpectedFactorFactor = new Matrix[_mX.getRowDimension()];
		}
		
		final int sampleCount = endRow - startRow;
		
		for (int iter=1; iter<=iterationCount; iter++) {
			
			// ***********
			// expectation
			Matrix G = _pFactorPhi.inverse().plus(_pVariableKsi.inverse().timesOnLeft(_pW.transpose()).times(_pW)).inverse();
			Matrix WTranKsiInv = _pVariableKsi.inverse().timesOnLeft(_pW.transpose());
			{
				double currLogLike = 0;
				Matrix xMean = _pW.times(_pFactorBias).plus(_pVariableBias);
				Matrix xCov = _pVariableKsi.plus(_pFactorPhi.timesOnLeft(_pW).times(_pW.transpose()));
				Matrix xInverseCov = StatsUtils.generalizedInverse(xCov);
				
				final double logPseudoNormalizer = GaussianFunc.calcLogPseudoNormalizer(xMean, xCov);
				
				for (int n=startRow; n<endRow; n++) {
	
					Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
					_xExpectedFactor[n] = G.times(WTranKsiInv.times(x_n.minus(_pVariableBias)).plus(_pFactorPhi.inverse().timesOnRight(_pFactorBias)));
					_xExpectedFactorFactor[n] = G.plus(_xExpectedFactor[n].times(_xExpectedFactor[n].transpose()));
					
					currLogLike += logPseudoNormalizer + GaussianFunc.calcLogUnnormalizedPdf(xMean, xInverseCov, x_n);
				}
//				System.out.println("LogLike: " + currLogLike);
				_logLike = currLogLike;
			}
			
			// ************
			// maximization
			_pFactorBias = new Matrix(_factorCount, 1);
			Matrix factorSampleCovariance = new Matrix(_factorCount, _factorCount);
			_pVariableBias = new Matrix(_variableCount, 1);
			for (int n=startRow; n<endRow; n++) {
				
				Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
				_pFactorBias.plusEquals(_xExpectedFactor[n].times(1.0/sampleCount));
				factorSampleCovariance.plusEquals(_xExpectedFactorFactor[n].times(1.0/sampleCount));
				_pVariableBias.plusEquals(x_n.minus(_pW.times(_xExpectedFactor[n])).times(1.0/sampleCount));
			}
			_pFactorPhi = new DiagMatrix(factorSampleCovariance.minus(_pFactorBias.times(_pFactorBias.transpose())));
			final Matrix leftW = new Matrix(_variableCount, _factorCount);
			final Matrix rightW = new Matrix(_factorCount, _factorCount);
			final Matrix mS = new Matrix(_variableCount, _variableCount);
			for (int n=startRow; n<endRow; n++) {
				
				Matrix x_n = MatrixUtils.getRows(_mX, n, n+1).transpose();
				Matrix x_n_cen = x_n.minus(_pVariableBias);
				leftW.plusEquals(x_n_cen.times(_xExpectedFactor[n].transpose()));
				rightW.plusEquals(_xExpectedFactorFactor[n]);
				
				for (int i=0; i<mS.getRowDimension(); i++) {
					for (int j=0; j<mS.getRowDimension(); j++) {
						mS.set(i, j, mS.get(i, j) + x_n_cen.get(i, 0) * x_n_cen.get(j, 0) / sampleCount);
					}
				}
			}
			_pW = leftW.times(rightW.inverse());
			Matrix rightKsi = leftW.transpose().timesEquals(1.0/sampleCount);
			_pVariableKsi = new DiagMatrix(mS.minus(_pW.times(rightKsi)));
			
			updateC();
		}
	}
	
	/**
	 * Calculate factor history (expected values of factors | X).
	 * 
	 */
	public Matrix extractF(final int startRow, final int endRow) {
		
		Matrix res = new Matrix(endRow-startRow, _factorCount);
		for (int n=startRow; n<endRow; n++) {
			Matrix x_n_expectedFactor = _xExpectedFactor[n];
			if (x_n_expectedFactor == null) {
				throw new IllegalStateException("Factors at index " + n + " are not initialized");
			}
			for (int k=0; k<_factorCount; k++) {
				res.set(n-startRow, k, x_n_expectedFactor.get(k, 0));
			}
		}
		return res;
	}
	
	public Matrix getFactorBias() {
		return _pFactorBias;
	}
	
	public DiagMatrix getFactorPhi() {
		return _pFactorPhi;
	}
	
	public Matrix getW() {
		return _pW;
	}
	
	public Matrix getVariableBias() {
		return _pVariableBias;
	}

	public DiagMatrix getVariableKsi() {
		return _pVariableKsi;
	}
	
	private void updateC() {
		_CNoKsi = _pFactorPhi.timesOnLeft(_pW).times(_pW.transpose());
		_C = _pVariableKsi.plus(_CNoKsi);		
	}
	
	public Matrix getC() {
		return _C;
	}
	
	public Matrix getCNoKsi() {
		return _CNoKsi;
	}
	
	public double getLogLike() {
		return _logLike;
	}
	
}
