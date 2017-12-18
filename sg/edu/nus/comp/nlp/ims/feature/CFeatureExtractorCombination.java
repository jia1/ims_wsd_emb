/**
 * IMS (It Makes Sense) -- NUS WSD System
 * Copyright (c) 2010 National University of Singapore.
 * All Rights Reserved.
 */
package sg.edu.nus.comp.nlp.ims.feature;

import java.util.ArrayList;

import pkg.feature.CEmbeddingsDimensionExtractor;
import pkg.feature.emb.IntegrationStrategy;
import sg.edu.nus.comp.nlp.ims.corpus.ICorpus;

/**
 * a combination of feature extractors.
 *
 * @author zhongzhi
 * 
 * Modified by @author Jiayee
 *
 */
public class CFeatureExtractorCombination implements IFeatureExtractor {
	// list of feature extractor
	protected ArrayList<IFeatureExtractor> m_FeatureExtractors = new ArrayList<IFeatureExtractor>();

	// feature extractor index
	protected int m_Index = 0;

	// instance index
	protected int m_InstanceIndex = 0;

	// corpus
	protected ICorpus m_Corpus = null;

	// current feature
	protected IFeature m_CurrentFeature = null;

	/**
	 * Default constructor
	 */
	public CFeatureExtractorCombination() {
		this.m_FeatureExtractors.add(new CPOSFeatureExtractor());
		this.m_FeatureExtractors.add(new CCollocationExtractor());
		this.m_FeatureExtractors.add(new CSurroundingWordExtractor());
	}

	/**
	 * Constructor when an ArrayList of feature extractors is provided
	 *
	 * @param p_FeatureExtractors
	 *            feature extractor list
	 */
	public CFeatureExtractorCombination(ArrayList<IFeatureExtractor> p_FeatureExtractors) {
		if (p_FeatureExtractors == null) {
			throw new IllegalArgumentException("argument cannot be null.");
		}
		this.m_FeatureExtractors.addAll(p_FeatureExtractors);
	}

	/**
	 * A master feature extractor builder. Exposes public methods to allow the addition of various feature extractors
	 * to build up this master feature extractor.
	 *
	 * Modified and documented by @author Jiayee
	 *
	 */
	public static class Builder {
		
		private ArrayList<IFeatureExtractor> features = new ArrayList<IFeatureExtractor>();
		
		public Builder addPOSFeature() {
			features.add(new CPOSFeatureExtractor());
			return this;
		}
		
		public Builder addCollocationFeature() {
			features.add(new CCollocationExtractor());
			return this;
		}
		
		public Builder addSurroundingWordFeature() {
			features.add(new CSurroundingWordExtractor());
			return this;
		}

		/*
		public Builder addConcatenatedEmbeddingFeature(String file, int windowSize) {
			features.add(new CEmbeddingsDimensionExtractor(file, IntegrationStrategy.concatenation(windowSize)));
			return this;
		}
			
		public Builder addAveragedEmbeddingFeature(String file, int windowSize) {
			features.add(new CEmbeddingsDimensionExtractor(file, IntegrationStrategy.average(windowSize)));
			return this;
		}
			
		public Builder addFractionalDecayedEmbeddingFeature(String file, int windowSize) {
			features.add(new CEmbeddingsDimensionExtractor(file, IntegrationStrategy.fractional(windowSize)));
			return this;
		}
			
		public Builder addExponentialDecayedEmbeddingFeature(String file, int windowSize) {
			features.add(new CEmbeddingsDimensionExtractor(file, IntegrationStrategy.exponential(windowSize)));
			return this;
		}
		*/

		public Builder addConcatenatedEmbeddingFeature(String file, int windowSize) {
			features.add(new CEmbeddingsDimensionExtractor(file, IntegrationStrategy.concatenation(windowSize)));
			return this;
		}

		public IFeatureExtractor build() {
			return new CFeatureExtractorCombination(features);
		}
		
	}

	/**
	 * check the validity of index
	 *
	 * @param p_Index
	 *            index
	 * @return valid or not
	 */
	protected boolean validIndex(int p_Index) {
		if (this.m_Corpus != null && this.m_Corpus.size() > p_Index
				&& p_Index >= 0) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#getCurrentInstanceID()
	 */
	@Override
	public String getCurrentInstanceID() {
		if (this.validIndex(this.m_InstanceIndex)) {
			return this.m_Corpus.getValue(this.m_InstanceIndex, "id");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if (this.m_CurrentFeature != null) {
			return true;
		}
		if (this.validIndex(this.m_InstanceIndex)) {
			while (this.m_Index < this.m_FeatureExtractors.size()) {
				if (this.m_FeatureExtractors.get(this.m_Index).hasNext()) {
					this.m_CurrentFeature = this.m_FeatureExtractors.get(
							this.m_Index).next();
				}
				if (this.m_CurrentFeature != null) {
					return true;
				}
				this.m_Index++;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#next()
	 */
	@Override
	public IFeature next() {
		IFeature feature = null;
		if (this.hasNext()) {
			feature = this.m_CurrentFeature;
			this.m_CurrentFeature = null;
		}
		return feature;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#restart()
	 */
	@Override
	public boolean restart() {
		this.m_Index = 0;
		for (IFeatureExtractor fe : this.m_FeatureExtractors) {
			if (!fe.restart()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#setCorpus(sg.edu.nus.comp.nlp.ims.corpus.ICorpus)
	 */
	@Override
	public boolean setCorpus(ICorpus p_Corpus) {
		if (p_Corpus == null) {
			return false;
		}
		this.m_Corpus = p_Corpus;
		this.m_InstanceIndex = 0;
		for (IFeatureExtractor fe : this.m_FeatureExtractors) {
			if (!fe.setCorpus(p_Corpus)) {
				return false;
			}
		}
		this.restart();
		this.m_InstanceIndex = -1;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#setCurrentInstance(int)
	 */
	@Override
	public boolean setCurrentInstance(int p_InstanceIndex) {
		for (IFeatureExtractor fe : this.m_FeatureExtractors) {
			if (!fe.setCurrentInstance(p_InstanceIndex)) {
				return false;
			}
		}
		this.m_InstanceIndex = p_InstanceIndex;
		return true;
	}

}
