package me.akuz.mnist.digits.visor.learning;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.StatsUtils;
import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.mnist.digits.visor.VisorLayer;
import me.akuz.mnist.digits.visor.algo.MDDP2;

/**
 * Color-inferring visor layer, which collapses
 * the infinite number of potential multi-channel
 * colors into a finite set of colors that can
 * the visor can recognize.
 *
 */
public final class FiniteColors2 extends VisorLayer {

	// base distribution for colors
	public static final double COLOR_DP_BASE_INIT  = 1.0;
	public static final double COLOR_DP_BASE_NOISE = 0.1;

	// base distribution for a channel in each color
	public static final double COLOR_CHANNEL_DP_BASE_INIT  = 1.0;
	public static final double COLOR_CHANNEL_DP_BASE_NOISE = 0.1;

	private final int _specialColors;
	private final MDDP2 _colors;
	private final MDDP2 _colorsChannels;

	/**
	 * Height of the input image tensor (size of dim 0).
	 */
	public final int inputHeight;

	/**
	 * Width of the input image tensor (size of dim 1).
	 */
	public final int inputWidth;
	
	/**
	 * Number of channels in the input image (size of dim 2).
	 */
	public final int inputChannelCount;
	
	/**
	 * Number of colors recognized by the layer.
	 */
	public final int colorCount;
	
	/**
	 * Shape of the output tensor.
	 */
	public final Shape outputShape;
	
	/**
	 * Output to be used at the next level.
	 */
	public final Tensor output;
	
	/**
	 * Create color visor layer with the shape of the
	 * input tensor (must have ndim 3 with the last
	 * dimension spanning the color channels) and 
	 * a specified number of colors to recognize.
	 */
	public FiniteColors2(
			final Shape inputShape, 
			final int specialColors,
			final int colorCount,
			final double startTemperature,
			final double startConfidence) {
		
		super(inputShape);
		
		if (inputShape.ndim != 3) {
			throw new IllegalArgumentException(
				"Shape of the color input must have ndim 3");
		}
		
		_specialColors = specialColors;
		
		this.inputHeight = inputShape.sizes[0];
		this.inputWidth = inputShape.sizes[1];
		this.inputChannelCount = inputShape.sizes[2];
		
		if (this.inputHeight < 1) {
			throw new IllegalArgumentException(
				"Input image tensor height (size of dim 0) must be positive");
		}
		if (this.inputWidth < 1) {
			throw new IllegalArgumentException(
				"Input image tensor width (size of dim 1) must be positive");
		}
		if (this.inputChannelCount < 1) {
			throw new IllegalArgumentException(
				"Input channel count (size of dim 2) must be positive");
		}
		
		if (colorCount < 2) {
			throw new IllegalArgumentException(
				"Color count must be >= 2, got " + colorCount);
		}
		this.colorCount = colorCount;
		
		this.outputShape = new Shape(
				this.inputHeight,
				this.inputWidth,
				colorCount);
		
		this.output = new Tensor(outputShape);
		final Random rnd = ThreadLocalRandom.current();
		final int[] outputIndices = new int[3];
		final Location outputLoc = new Location(outputIndices);
		final double[] outputData = this.output.data();
		for (int i=0; i<this.inputHeight; i++) {
			outputIndices[0] = i;
			for (int j=0; j<this.inputWidth; j++) {
				outputIndices[1] = j;
				final int idx = this.outputShape.calcFlatIndexFromLocation(outputLoc);
				for (int k=0; k<colorCount; k++) {
					outputData[idx + k] = rnd.nextDouble();
				}
				StatsUtils.normalizeInPlace(outputData, idx, colorCount);
			}
		}

		_colors = new MDDP2(
				new Shape(this.colorCount),
				COLOR_DP_BASE_INIT,
				COLOR_DP_BASE_NOISE,
				startTemperature,
				startConfidence);
		
		_colorsChannels = new MDDP2(
				new Shape(
						this.colorCount, 
						this.inputChannelCount,
						2),
				COLOR_CHANNEL_DP_BASE_INIT,
				COLOR_CHANNEL_DP_BASE_NOISE,
				startTemperature,
				startConfidence);
	}
	
	public void setTemperature(final double temperature) {
		_colors.setTemperature(temperature);
		_colorsChannels.setTemperature(temperature);
	}
	
	public void setContrast(final double contrast) {
		_colors.setContrast(contrast);
		_colorsChannels.setContrast(contrast);
	}

	@Override
	public void infer() {
		
		final Tensor input = getInputNotNull();
		
		final int[] inputDeepIdxs = new int[3];
		final int[] outputDeepIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputDeepLoc = new Location(inputDeepIdxs);
		final Location outputDeepLoc = new Location(outputDeepIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		final double[] outputData = this.output.data();
		final double[] channelData = new double[2];
		
		for (int i=0; i<this.inputHeight;i++) {
			inputDeepIdxs[0] = i;
			outputDeepIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputDeepIdxs[1] = j;
				outputDeepIdxs[1] = j;
				
				// get output index
				outputDeepIdxs[2] = 0;
				final int outputStartIdx = outputShape.calcFlatIndexFromLocation(outputDeepLoc);
				
				// fill color priors
				_colors.fillPosteriorMean(colorChannelLoc, outputData, outputStartIdx);
				
				// calculate color log probs
				for (int colorIdx=0; colorIdx<this.colorCount; colorIdx++) {
					
					// output data index
					final int outputIdx = outputStartIdx + colorIdx;
					
					// convert color prior to log space
					outputData[outputIdx] = Math.log(outputData[outputIdx]);
					
					// set first color index
					colorChannelIdxs[0] = colorIdx;

					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						inputDeepIdxs[2] = channelIdx;
						colorChannelIdxs[1] = channelIdx;
						
						channelData[1] = input.get(inputDeepLoc);
						channelData[0] = 1.0 - channelData[1];

						final double colorChannelLogLike =
							StatsUtils.checkFinite(
								_colorsChannels.calcPosteriorLogLike(
									colorChannelLoc, 
									channelData, 
									0));
						
						outputData[outputIdx] += colorChannelLogLike;
					}
				}
				
				StatsUtils.logLikesToProbsInPlace(
						outputData, 
						outputStartIdx, 
						this.colorCount);
			}
		}
	}

	@Override
	public void learn() {
		
		final Tensor input = getInputNotNull();

		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		final double[] outputData = this.output.data();
		final double[] channelValues = new double[this.inputChannelCount];
		final double[] channelData = new double[2];
		
		_colors.clearObservations();
		_colorsChannels.clearObservations();

		for (int i=0; i<this.inputHeight; i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;

				// collect channel values
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					inputIdxs[2] = channelIdx;
					
					final double channelValue = input.get(inputLoc);
					if (channelValue < 0.0 || channelValue > 1.0) {
						throw new IllegalStateException(
							"Input image channels can only contain values " +
							"within interval [0, 1], got " + channelValue);
					}
					
					channelValues[channelIdx] = channelValue;
				}

				final int outputStartIdx = this.outputShape.calcFlatIndexFromLocation(outputLoc);
				_colors.addObservation(1.0, null, outputData, outputStartIdx);

				// update each color
				for (int colorIdx=0; colorIdx<this.colorCount; colorIdx++) {
					
					// get output color prob
					final double colorProb = outputData[outputStartIdx + colorIdx];

					// update color channels
					colorChannelIdxs[0] = colorIdx;
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorChannelIdxs[1] = channelIdx;

						// get channel value at this point
						final double channelValue = channelValues[channelIdx];

						if (_specialColors == 1 && colorIdx == 0) {
							channelData[1] = 0.5;
							channelData[0] = 0.5;

						} if (_specialColors == 2 && colorIdx == 0) {
							channelData[1] = 1.0;
							channelData[0] = 0.0;
							
						} else if (_specialColors == 2 && colorIdx == 1) {
							channelData[1] = 0.0;
							channelData[0] = 1.0;
							
						} else {
							channelData[1] = channelValue;
							channelData[0] = 1.0 - channelValue;
						}

						_colorsChannels.addObservation(
								colorProb,
								colorChannelLoc,
								channelData,
								0);
					}
				}
			}
		}
	}

	@Override
	public void dream() {
		
		final Tensor input = getInputNotNull();
		
		final int[] inputIdxs = new int[3];
		final int[] outputIdxs = new int[3];
		final int[] colorChannelIdxs = new int[2];
		final Location inputLoc = new Location(inputIdxs);
		final Location outputLoc = new Location(outputIdxs);
		final Location colorChannelLoc = new Location(colorChannelIdxs);
		final double[] channelValues = new double[this.inputChannelCount];
		final double[] channelData = new double[2];
		for (int i=0; i<this.inputHeight;i++) {
			inputIdxs[0] = i;
			outputIdxs[0] = i;
			
			for (int j=0; j<this.inputWidth; j++) {
				inputIdxs[1] = j;
				outputIdxs[1] = j;
				
				// reset channels array
				Arrays.fill(channelValues, 0.0);
				
				// aggregate color probs
				for (int colorIdx=0; colorIdx<this.colorCount; colorIdx++) {
					
					// get output color prob
					outputIdxs[2] = colorIdx;
					final double colorProb = this.output.get(outputLoc);
					
					// aggregate color channels
					colorChannelIdxs[0] = colorIdx;
					
					// now add channel observations probability
					for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
						
						colorChannelIdxs[1] = channelIdx;
						
						_colorsChannels.fillPosteriorMean(
								colorChannelLoc, 
								channelData, 
								0);
						
						// merge into the channel
						channelValues[channelIdx] += 
								colorProb * channelData[1];
					}
				}
				
				// set input channel values at this location
				for (int channelIdx=0; channelIdx<this.inputChannelCount; channelIdx++) {
					
					// populate channel value
					inputIdxs[2] = channelIdx;
					input.set(inputLoc, channelValues[channelIdx]);
				}
			}
		}
	}

}
