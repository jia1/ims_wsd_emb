/**
 * IMS EMBED is an extension of the original IMS system 
 * 
 * @author: iiacobac
 *
 * IMS EMBED is an output of the MultiJEDI ERC Starting Grant No. 259234.
 * IMS EMBED is licensed under a Creative Commons Attribution - Noncommercial - Share Alike 3.0 License.
 *
 * Modified by @author Jiayee
 */
package pkg.feature.emb;

import java.util.ArrayList;
import java.util.Arrays;

import pkg.feature.CEmbeddingsDimensionExtractor;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.feature.CDoubleFeature;
import sg.edu.nus.comp.nlp.ims.feature.CVectorSequenceFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;

public abstract class IntegrationStrategy {

	protected final int WINDOW;

	private IntegrationStrategy(int windowSize) {
		this.WINDOW = windowSize;
	}

	public abstract double[] getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex);
	
	public abstract IFeature getNext(CEmbeddingsDimensionExtractor extractor);
	
	protected String formEmbeddingDimensionName(int p_EmbeddingDimensionIndex) {
		return "DIM_" + p_EmbeddingDimensionIndex;
	}

	/*
	public static IntegrationStrategy concatenation(int windowSize) {
		return new IntegrationStrategy(windowSize) {
			public String getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex) {
				int i = Math.floorDiv(p_EmbeddingDimensionIndex, WINDOW);
				int pos;
				if (i < WINDOW) {
					pos = extractor.getM_IndexInSentence() - WINDOW + i;
				} else {
					pos = extractor.getM_IndexInSentence() - WINDOW + i + 1;
				}
				if (pos > -1 && pos < extractor.getSentenceSize()) {
					IItem item = extractor.getItem(pos);
					int index = p_EmbeddingDimensionIndex % WINDOW;
					String dim = item.get(0).toLowerCase();
					if (extractor.getWordMap().containsKey(dim))
						return Double.toString(extractor.getWordMap().get(dim)[index]);
				}
				return Double.toString(0.0);
			}
			
			public IFeature getNext(CEmbeddingsDimensionExtractor extractor) {
				IFeature feature = null;
				if (extractor.getM_EmbeddingDimensionIndex() >= 0 && extractor.getM_EmbeddingDimensionIndex() < (extractor.getVectorSize() * WINDOW * 2)) {
					feature = new CDoubleFeature();
					
					feature.setKey(this.formEmbeddingDimensionName(extractor.getM_EmbeddingDimensionIndex()));
					feature.setValue(getEmbeddingDimension(extractor, extractor.getM_EmbeddingDimensionIndex()));
					extractor.setM_EmbeddingDimensionIndex(extractor.getM_EmbeddingDimensionIndex() + 1);
				}
				return feature;
			}

		};
	}

	public static IntegrationStrategy average(int windowSize) {
		return new IntegrationStrategy(windowSize) {
			public String getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex) {
				double val = 0.0;
				for (int i = extractor.getM_IndexInSentence() - WINDOW; i <= extractor.getM_IndexInSentence() + WINDOW; i++) {
					if (i > -1 && i < extractor.getSentenceSize()) {
						int relI = extractor.getM_IndexInSentence() - i;
						double coef = coeficient(relI);
						IItem item = extractor.getItem(i);
						String dim = item.get(0).toLowerCase();
						if (extractor.getWordMap().containsKey(dim)) {
							val += coef * extractor.getWordMap().get(dim)[p_EmbeddingDimensionIndex];
						}
					}
				}
				return Double.toString(val);
			}

			private double coeficient(int relI) {
				return Math.pow(WINDOW * 1.0, -1.0);
			}
			
			public IFeature getNext(CEmbeddingsDimensionExtractor extractor) {
				IFeature feature = null;
				if (extractor.getM_EmbeddingDimensionIndex() >= 0 && extractor.getM_EmbeddingDimensionIndex() < extractor.getVectorSize()) {
					feature = new CDoubleFeature();
					
					feature.setKey(this.formEmbeddingDimensionName(extractor.getM_EmbeddingDimensionIndex()));
					feature.setValue(getEmbeddingDimension(extractor, extractor.getM_EmbeddingDimensionIndex()));
					extractor.setM_EmbeddingDimensionIndex(extractor.getM_EmbeddingDimensionIndex() + 1);
				}
				return feature;
			}

		};
	}

	public static IntegrationStrategy fractional(int windowSize) {
		return new IntegrationStrategy(windowSize) {
			public String getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex) {
				double val = 0.0;
				for (int i = extractor.getM_IndexInSentence() - WINDOW; i <= extractor.getM_IndexInSentence() + WINDOW; i++) {
					if (i > -1 && i < extractor.getSentenceSize()) {
						int relI = extractor.getM_IndexInSentence() - i;
						double coef = coeficient(relI);
						IItem item = extractor.getItem(i);
						String dim = item.get(0).toLowerCase();
						if (extractor.getWordMap().containsKey(dim)) {
							val += coef * extractor.getWordMap().get(dim)[p_EmbeddingDimensionIndex];
						}
					}
				}
				return Double.toString(val);
			}

			private double coeficient(int relI) {
				return ((WINDOW + (relI > 0 ? -relI : relI) + 1) * 1.0) / (WINDOW * 1.0);
			}
			
			public IFeature getNext(CEmbeddingsDimensionExtractor extractor) {
				IFeature feature = null;
				if (extractor.getM_EmbeddingDimensionIndex() >= 0 && extractor.getM_EmbeddingDimensionIndex() < extractor.getVectorSize()) {
					feature = new CDoubleFeature();
					
					feature.setKey(this.formEmbeddingDimensionName(extractor.getM_EmbeddingDimensionIndex()));
					feature.setValue(getEmbeddingDimension(extractor, extractor.getM_EmbeddingDimensionIndex()));
					extractor.setM_EmbeddingDimensionIndex(extractor.getM_EmbeddingDimensionIndex() + 1);
				}
				return feature;
			}

		};
	}

	public static IntegrationStrategy exponential(int windowSize) {
		return new IntegrationStrategy(windowSize) {
			public String getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex) {
				double val = 0.0;
				for (int i = extractor.getM_IndexInSentence() - WINDOW; i <= extractor.getM_IndexInSentence() + WINDOW; i++) {
					if (i > -1 && i < extractor.getSentenceSize()) {
						int relI = extractor.getM_IndexInSentence() - i;
						double coef = coeficient(relI);
						IItem item = extractor.getItem(i);
						String dim = item.get(0).toLowerCase();
						if (extractor.getWordMap().containsKey(dim)) {
							val += coef * extractor.getWordMap().get(dim)[p_EmbeddingDimensionIndex];
						}
					}
				}
				return Double.toString(val);
			}

			private final double ALPHA = 1 - Math.pow(0.1, 1 / (WINDOW * 1.0));

			private double coeficient(int n) {
				if (n == 0)
					return 1;
				else
					return Math.pow(1 - ALPHA, Math.abs(n) - 1);
			}
			
			public IFeature getNext(CEmbeddingsDimensionExtractor extractor) {
				IFeature feature = null;
				if (extractor.getM_EmbeddingDimensionIndex() >= 0 && extractor.getM_EmbeddingDimensionIndex() < extractor.getVectorSize()) {
					feature = new CDoubleFeature();
					
					feature.setKey(this.formEmbeddingDimensionName(extractor.getM_EmbeddingDimensionIndex()));
					feature.setValue(getEmbeddingDimension(extractor, extractor.getM_EmbeddingDimensionIndex()));
					extractor.setM_EmbeddingDimensionIndex(extractor.getM_EmbeddingDimensionIndex() + 1);
				}
				return feature;
			}

		};
	}
	*/

	public static IntegrationStrategy concatenation(int windowSize) {
		return new IntegrationStrategy(windowSize) {
			public double[] getEmbeddingDimension(CEmbeddingsDimensionExtractor extractor, int p_EmbeddingDimensionIndex) {
				double[] vector = new double[WINDOW + 1];
				int firstIndex = extractor.getM_IndexInSentence() - WINDOW;
				int lastIndex = extractor.getM_IndexInSentence() + WINDOW;
				for (int i = firstIndex; i <= lastIndex; i++) {
					if (i > -1 && i < extractor.getSentenceSize()) {
						IItem item = extractor.getItem(i);
						String dim = item.get(0).toLowerCase();
						if (extractor.getWordMap().containsKey(dim)) {
							vector[i - firstIndex] = extractor.getWordMap().get(dim)[p_EmbeddingDimensionIndex];
						} else {
							vector[i - firstIndex] = 0;
						}
					} else {
						vector[i - firstIndex] = 0;
					}
				}
				return vector;
			}

			public IFeature getNext(CEmbeddingsDimensionExtractor extractor) {
				IFeature feature = null;
				if (extractor.getM_EmbeddingDimensionIndex() >= 0 && extractor.getM_EmbeddingDimensionIndex() < extractor.getVectorSize()) {
					feature = new CVectorSequenceFeature();
					feature.setKey(this.formEmbeddingDimensionName(extractor.getM_EmbeddingDimensionIndex()));
					feature.setValue(Arrays.toString(getEmbeddingDimension(extractor, extractor.getM_EmbeddingDimensionIndex())));
					extractor.setM_EmbeddingDimensionIndex(extractor.getM_EmbeddingDimensionIndex() + 1);
				}
				return feature;
			}

		};
	}

}
