package sg.edu.nus.comp.nlp.ims.classifiers;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import liblinear.FeatureNode;

/**
 * 
 * @author Jiayee
 *
 */

public class VectorSequenceIterator implements DataSetIterator {

	// Size of each mini batch (number of windows of words)
	private int miniBatchSize;
	private int[] arrayOfLabels;
	private INDArray dataSetValues;
	private INDArray dataSetLabels;
	private int index; // for tracking hasNext and next

	public VectorSequenceIterator(ArrayList<FeatureNode[][]> featVectors, int[] labels, int miniBatchSize) {
		this.miniBatchSize = miniBatchSize;
		this.arrayOfLabels = labels;
		this.dataSetLabels = Nd4j.create(labels);

		double[][][] values = new double[featVectors.size()][][]; // number of examples, number of time steps, word vectors
		for (int i = 0; i < featVectors.size(); i++) {
			values[i] = new double[featVectors.get(i).length][];
			for (int j = 0; j < featVectors.get(i).length; j++) {
			    for (int k = 0; k < featVectors.get(i)[j].length; k++) {
			        values[i][j][k] = featVectors.get(i)[j][k].value;
			    }
			}
		}
		// this.dataSetValues = Nd4j.create(values); // TODO: Fix this
		this.index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < dataSetValues.length();
	}

	@Override
	public DataSet next() {
		return next(miniBatchSize);
	}

	@Override
	public boolean asyncSupported() {
		return true;
	}

	@Override
	public int batch() {
		return miniBatchSize;
	}

	@Override
	public int cursor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> getLabels() {
		ArrayList<String> labels = new ArrayList<>();
		for (int i = 0; i < arrayOfLabels.length; i++) {
			labels.add(Integer.toString(arrayOfLabels[i]));
		}
		return labels;
	}

	@Override
	public DataSetPreProcessor getPreProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int inputColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataSet next(int size) {
		int n = Math.min(size, dataSetValues.length() - index);
		// INDArray slice(int i, int dimension)
		// Returns the specified slice of this ndarray
		DataSet dataSet = new DataSet(dataSetValues.slice(index, n), dataSetLabels.slice(index, n));
		index = index + n;
		return dataSet;
	}

	@Override
	public int numExamples() {
		return dataSetValues.length();
	}

	@Override
	public void reset() {
		index = 0;
	}

	@Override
	public boolean resetSupported() {
		return true;
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public int totalExamples() {
		return numExamples();
	}

	@Override
	public int totalOutcomes() {
		return numExamples();
	}

}
