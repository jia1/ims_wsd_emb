package sg.edu.nus.comp.nlp.ims.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import liblinear.FeatureNode;
import liblinear.Problem;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;
import sg.edu.nus.comp.nlp.ims.instance.IInstance;
import sg.edu.nus.comp.nlp.ims.lexelt.ILexelt;
import sg.edu.nus.comp.nlp.ims.lexelt.IStatistic;

/**
 * 
 * @author Jiayee
 *
 */

public class CGravesLSTMLexeltWriter implements ILexeltWriter {
	/**
	 * load the statistic of p_iLexelt
	 * for each feature type in statistic
	 * 	if feature is binary
	 * 		keep it
	 * 	else
	 * 		if feature is list and the number of values is less than 2
	 * 			one new feature
	 * 		else
	 * 			set each value as a new feature
	 * @param p_iLexelt lexelt
	 * @return indices
	 * @throws ClassNotFoundException
	 */
	protected int[][] loadStatistic(ILexelt p_iLexelt) throws ClassNotFoundException {
		int[][] retIndice = null;
		int accuIndex = 1;
		if (p_iLexelt != null) {
			IStatistic stat = p_iLexelt.getStatistic();
			int keySize = stat.getKeys().size();
			retIndice = new int[keySize][0];
			int keyIndex = 0;
			for (keyIndex = 0;keyIndex < keySize;keyIndex++) {
				List<String> values = stat.getValue(keyIndex);
				retIndice[keyIndex] = new int[values.size()];
				for (int i = 0;i < values.size();i++) {
					retIndice[keyIndex][i] = accuIndex++;
				}
			}
		}
		return retIndice;
	}

	/**
	 * generate feature vector for one instance
	 * @param p_Instance input instance
	 * @param p_Stat statisitc of training data set
	 * @param p_Indice feature indices
	 * @return feature vector
	 */
	protected String toString(IInstance p_Instance, IStatistic p_Stat, int[][] p_Indice){
		StringBuilder featureBuilder = new StringBuilder();
		FeatureNode[][] features = this.getVector(p_Instance, p_Stat, p_Indice);
		for (int i = 0; i < features.length; i++) {
		    for (int j = 0; j < features[i].length; j++) {
		        featureBuilder.append(" ");
		        featureBuilder.append(features[i][j].index);
		        featureBuilder.append(":");
		        featureBuilder.append(features[i][j].value);
		    }
		}
		String featureOnly = featureBuilder.toString();
		StringBuilder featureVector = new StringBuilder();
		for (Integer tag : this.processTags(p_Stat, p_Instance.getTag())) {
			featureVector.append(tag.toString());
			featureVector.append(featureOnly);
			featureVector.append("\n");
		}
		return featureVector.toString();
	}

	/**
	 * get the vector of one instance
	 * @param p_Instance input instance
	 * @param p_Stat statistic
	 * @return feature vector
	 */
	protected FeatureNode[][] getVector(IInstance p_Instance, IStatistic p_Stat, int[][] p_Indice){
		String value = null;
		INDArray finVal = null;
		int kIndex = 0;
		int featureSize = p_Instance.size();
		Hashtable<Integer, Double> exist = new Hashtable<>();
		for(int fIndex = 0;fIndex < featureSize;fIndex++) {
			IFeature feature = p_Instance.getFeature(fIndex);
			kIndex = p_Stat.getIndex(feature.getKey());
			if (kIndex < 0) {
				continue;
			}
			List<String> values = p_Stat.getValue(kIndex);
			value = feature.getValue();
			if (value == null || !p_Stat.contains(kIndex, value)) {
				// value = p_Stat.getDefaultValue(); // Override
			    finVal = Nd4j.zeros(1, 1);
			} else {
			    finVal = Nd4j.create(fromString(value));
			}
			for (int i = 0;i < values.size();i++) {
				if (values.get(i).equals(value)) {
					exist.put(p_Indice[kIndex][i], 1.0);
					break;
				}
			}
		}
		ArrayList<Integer> indice = new ArrayList<Integer>(exist.keySet());
		Collections.sort(indice);
		FeatureNode[][] retVal = new FeatureNode[exist.size()][];
		for (int i = 0; i < indice.size(); i++) {
		    for (int j = 0; j < finVal.shape()[0]; j++) {
		        double val = finVal.getDouble(j);
		        retVal[i][j] = new FeatureNode((int) (Math.pow(2, i) * Math.pow(3, j)), val);
		    }
		}
		return retVal;
	}

	// TODO: Move to util
	private static double[] fromString(String string) {
	    String[] strings = string.replace("[", "").replace("]", "").split(", ");
	    double result[] = new double[strings.length];
	    for (int i = 0; i < result.length; i++) {
	        result[i] = Double.parseDouble(strings[i]);
        }
	    return result;
    }

	/**
	 * change tags to integer (start from 1)
	 * @param p_Stat statistic
	 * @param p_Tags real tags
	 * @return new tags
	 */
	protected HashSet<Integer> processTags(IStatistic p_Stat, ArrayList<String> p_Tags) {
		HashSet<Integer> retVal = new HashSet<Integer>();
		if (p_Tags == null || p_Tags.size() == 0) {
			retVal.add(0);
		} else {
			for (String tag : p_Tags) {
				Integer iTag = 0;
				if (!tag.equals("'?'") && !tag.equals("?")) {
					iTag = p_Stat.getTagsInOrder().indexOf(tag);
					if (iTag < 0) {
						iTag = -1;
					}
					iTag++;
	            }
				retVal.add(iTag);
			}
		}
		return retVal;
	}

	@Override
	public void write(String p_Filename, ILexelt p_Lexelt) throws Exception {
		int[][] indice = this.loadStatistic(p_Lexelt);
		if (indice == null) {
			throw new IllegalArgumentException("the input lexelt should not be null.");
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(p_Filename));
		IStatistic stat = p_Lexelt.getStatistic();
		int size = p_Lexelt.size(); // instance count
		for (int i = 0;i < size;i++) {
			IInstance instance = p_Lexelt.getInstance(i);
			writer.write(this.toString(instance, stat, indice));
		}
		writer.flush();
		writer.close();
	}

	@Override
	public String toString(ILexelt p_Lexelt) throws Exception {
		int[][] indice = this.loadStatistic(p_Lexelt);
		if (indice == null) {
			throw new IllegalArgumentException("the input lexelt should not be null.");
		}
		StringBuilder builder = new StringBuilder();
		IStatistic stat = p_Lexelt.getStatistic();
		int size = p_Lexelt.size(); // instance count
		for (int i = 0;i < size;i++) {
			IInstance instance = p_Lexelt.getInstance(i);
			builder.append(this.toString(instance, stat, indice));
		}
		return builder.toString();
	}

	@Override
	public Object getInstances(ILexelt p_Lexelt) throws Exception {
		Problem retVal = new Problem();
		ArrayList<FeatureNode[][]> featureVectors = new ArrayList<>();
		ArrayList<Integer> classes = new ArrayList<Integer>();
		int[][] indice = this.loadStatistic(p_Lexelt);
		if (indice == null) {
			throw new IllegalArgumentException("the input lexelt should not be null.");
		}
		IStatistic stat = p_Lexelt.getStatistic();
		int size = p_Lexelt.size(); // instance count
		for (int i = 0;i < size;i++) {
			IInstance instance = p_Lexelt.getInstance(i);
			FeatureNode[][] featureVector = this.getVector(instance, stat, indice);
			for (Integer tag: this.processTags(stat, instance.getTag())) {
				featureVectors.add(Arrays.copyOf(featureVector, featureVector.length));
				classes.add(tag);
			}
		}
		retVal.l = featureVectors.size();
		retVal.x = new FeatureNode[retVal.l][];
		retVal.y = new int[retVal.l];
		for (int i = 0; i < retVal.l; i++) {
			retVal.y[i] = classes.get(i);
		}
		return new Object[]{ retVal, featureVectors }; // Incomplete Problem and ArrayList<FeatureNode[][]>
	}

}
