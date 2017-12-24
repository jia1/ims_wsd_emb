package sg.edu.nus.comp.nlp.ims.classifiers;

import java.util.List;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * 
 * @author Jiayee
 *
 */

public class VectorSequenceIterator implements DataSetIterator {

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataSet next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean asyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int batch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cursor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> getLabels() {
		// TODO Auto-generated method stub
		return null;
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
	public DataSet next(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numExamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean resetSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int totalExamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int totalOutcomes() {
		// TODO Auto-generated method stub
		return 0;
	}

}
