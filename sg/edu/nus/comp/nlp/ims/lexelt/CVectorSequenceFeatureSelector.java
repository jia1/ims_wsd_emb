package sg.edu.nus.comp.nlp.ims.lexelt;

import sg.edu.nus.comp.nlp.ims.feature.CVectorSequenceFeature;

/**
 * 
 * @author Jiayee
 *
 */
public class CVectorSequenceFeatureSelector extends AListFeatureSelector {
	public CVectorSequenceFeatureSelector(int p_M2) {
		this.m_M2 = p_M2;
		this.m_FeatureName = CVectorSequenceFeature.class.getName();
	}
}
