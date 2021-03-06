package me.akuz.nlp.run.lda;

import me.akuz.core.Dbo;
import me.akuz.core.gson.GsonSerializers;

public final class ProgramOptions extends Dbo {

	private static final String _inputDir               = "inputDir";
	private static final String _outputDir              = "outputDir";
	private static final String _topicsConfigFile       = "topicsConfigFile";
	private static final String _topicOutputStemsCount  = "topicOutputStemsCount";
	private static final String _stopWordsFile          = "stopWordsFile";
	private static final String _threadCount            = "threadCount";
	private static final String _burnInStartTemp        = "burnInStartTemp";
	private static final String _burnInEndTemp          = "burnInEndTemp";
	private static final String _burnInTempDecay        = "burnInTempDecay";
	private static final String _burnInTempIter         = "burnInTempIter";
	private static final String _samplingIter           = "samplingIter";
	
	public ProgramOptions(
			String inputDir,
			String outputDir,
			String topicsConfigFile,
			Integer topicOutputStemsCount,
			String stopWordsFile,
			Integer threadCount,
			Double burnInStartTemp,
			Double burnInEndTemp,
			Double burnInTempDecay,
			Integer burnInTempIter,
			Integer samplingIter) {
		
		set(_inputDir, inputDir);
		set(_outputDir, outputDir);
		set(_topicsConfigFile, topicsConfigFile);
		set(_topicOutputStemsCount, topicOutputStemsCount);
		set(_stopWordsFile, stopWordsFile);
		set(_threadCount, threadCount);
		set(_burnInStartTemp, burnInStartTemp);
		set(_burnInEndTemp, burnInEndTemp);
		set(_burnInTempDecay, burnInTempDecay);
		set(_burnInTempIter, burnInTempIter);
		set(_samplingIter, samplingIter);
	}
	
	public String getInputDir() {
		return getString(_inputDir);
	}
	public String getOutputDir() {
		return getString(_outputDir);
	}
	public String getTopicsConfigFile() {
		return getString(_topicsConfigFile);
	}
	public Integer getTopicOutputStemsCount() {
		return getInteger(_topicOutputStemsCount);
	}
	public String getStopWordsFile() {
		return getString(_stopWordsFile);
	}
	public Integer getThreadCount() {
		return getInteger(_threadCount);
	}
	public Double getBurnInStartTemp() {
		return getDouble(_burnInStartTemp);
	}
	public Double getBurnInEndTemp() {
		return getDouble(_burnInEndTemp);
	}
	public Double getBurnInTempDecay() {
		return getDouble(_burnInTempDecay);
	}
	public Integer getBurnInTempIter() {
		return getInteger(_burnInTempIter);
	}
	public Integer getSamplingIter() {
		return getInteger(_samplingIter);
	}
	
	@Override
	public String toString() {
		return GsonSerializers.NoHtmlEscapingPretty.toJson(getMap());
	}
}
