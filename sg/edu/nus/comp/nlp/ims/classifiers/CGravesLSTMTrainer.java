package sg.edu.nus.comp.nlp.ims.classifiers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import liblinear.FeatureNode;
import liblinear.Problem;
import sg.edu.nus.comp.nlp.ims.io.CGravesLSTMLexeltWriter;
import sg.edu.nus.comp.nlp.ims.io.ILexeltWriter;
import sg.edu.nus.comp.nlp.ims.lexelt.CModelInfo;
import sg.edu.nus.comp.nlp.ims.lexelt.ILexelt;
import sg.edu.nus.comp.nlp.ims.lexelt.IStatistic;

/**
 * 
 * @author Jiayee
 *
 */

public class CGravesLSTMTrainer implements IModelTrainer {
	/* (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.classifiers.IModelTrainer#setOptions(java.lang.String[])
	 */
	@Override
	public void setOptions(String[] options) {

	}

	/* (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.classifiers.IModelTrainer#train(java.lang.Object)
	 */
	@Override
	public Object train(Object p_Lexelt) throws Exception {
		ILexelt lexelt = (ILexelt) p_Lexelt;
		CModelInfo modelInfo = new CModelInfo();
		modelInfo.lexelt = lexelt.getID();
		modelInfo.model = null;
		modelInfo.statistic = lexelt.getStatistic();

		if (((IStatistic) modelInfo.statistic).getTagsInOrder().size() <= 1) {
			modelInfo.model = null;
		} else {
			int numIterPerPass = 1;
			int seed = 2018;
			double alpha = 0.1;
			double lambda = 0.001; // 1e-3

			int windowSize = 10;
			int numSenses = 5;

			int layerLength = 200;
	        int tbpttLength = windowSize / 2;
	        int miniBatchSize = 32;
			int numEpochs = 1;

			MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
					.weightInit(WeightInit.XAVIER)
					.learningRate(alpha)
					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(numIterPerPass)
					.seed(seed)
					.regularization(true)
					.l2(lambda)
			        .updater(Updater.RMSPROP)
					.list()
					.layer(0, new GravesLSTM.Builder()
							.nIn(windowSize)
							.nOut(layerLength)
							.activation(Activation.TANH).build())
					.layer(1, new GravesLSTM.Builder()
							.nIn(layerLength)
							.nOut(layerLength)
							.activation(Activation.TANH).build())
					.layer(2, new RnnOutputLayer.Builder(LossFunction.MCXENT)
							.activation(Activation.SOFTMAX)
							.nIn(layerLength)
							.nOut(numSenses).build())
					.pretrain(false)
					.backprop(true)
					.backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
					.build();

			MultiLayerNetwork lstm = new MultiLayerNetwork(conf);
			lstm.init();

			ILexeltWriter lexeltWriter = new CGravesLSTMLexeltWriter();
			Object[] instances = (Object[]) lexeltWriter.getInstances(lexelt); // { retVal, featureVectors }
			Problem prob = (Problem) instances[0];
			ArrayList<FeatureNode[][]> featVectors = (ArrayList<FeatureNode[][]>) instances[1];
			int[] labels = prob.y;

			// Transform 2D featVectors to 3D: Number of examples, number of time steps, word vectors
			for (int i = 0; i < featVectors.size(); i++) {
			    // TODO
			}

			for (int i = 0; i < featVectors.size(); i++) {
			    for (int j = 0; j < featVectors.get(i).length; j++) {
			        for (int k = 0; k < featVectors.get(i)[j].length; k++) {
			            FeatureNode vectorNode = featVectors.get(i)[j][k];
			            int vectorIndex = vectorNode.index;
			            double value = vectorNode.value;
			            int[] vectorIndices = getExponents(vectorIndex);
			            int a = vectorIndices[0]; // which example
			            int b = vectorIndices[1]; // which feature in the example (in a flattened array)
			            // TODO
			        }
			    }
			}

			int numPossibleLabels = 0;
			for (int i = 0; i < labels.length; i++) {
			    numPossibleLabels = Math.max(labels[i], numPossibleLabels);
			    BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("labels%d.csv", i)));
			    writer.write(labels[i]);
			    writer.close();
			}

			SequenceRecordReader featureReader = new CSVSequenceRecordReader(0, ",");
			SequenceRecordReader labelReader = new CSVSequenceRecordReader(0, ",");
			featureReader.initialize(new NumberedFileInputSplit("values%d.csv", 0, 9)); // TODO: Update 9
			labelReader.initialize(new NumberedFileInputSplit("labels%d.csv", 0, labels.length - 1));

			// For classification problems: numPossibleLabels is the number of classes in your data set. Use regression = false.
			DataSetIterator dataSetIterator = (DataSetIterator) new SequenceRecordReaderDataSetIterator(
			        featureReader,
			        labelReader,
			        miniBatchSize,
			        numPossibleLabels,
			        false, // regression parameter
			        SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

			// VectorSequenceIterator dataSetIterator = new VectorSequenceIterator(featVectors, labels, miniBatchSize);
			for (int i = 0; i < numEpochs; i++) {
                lstm.fit(dataSetIterator);
            }
			modelInfo.model = lstm;
		}

		return modelInfo;
	}

	private int[] getExponents(int x) {
	    int[] exponents = new int[2];
	    while (x % 2 == 0) {
	        exponents[0] += 1;
	    }
	    while (x % 3 == 0) {
            exponents[1] += 1;
        }
	    return exponents;
	}

}
