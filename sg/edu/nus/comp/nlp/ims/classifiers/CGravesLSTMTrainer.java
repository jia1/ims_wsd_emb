package sg.edu.nus.comp.nlp.ims.classifiers;

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
			Problem prob = (Problem) lexeltWriter.getInstances(lexelt);
			FeatureNode[][] featVectors = prob.x;
			int[] labels = prob.y;

			VectorSequenceIterator dataSetIterator = new VectorSequenceIterator(featVectors, labels, miniBatchSize);
			for (int i = 0; i < numEpochs; i++) {
                lstm.fit(dataSetIterator);
            }
			modelInfo.model = lstm;
		}

		return modelInfo;
	}

}
