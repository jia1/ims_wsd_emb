package sg.edu.nus.comp.nlp.ims.lexelt;

import sg.edu.nus.comp.nlp.ims.feature.CVectorSequence;

/**
 * 
 * @author Jiayee
 *
 */
public class CVectorSequenceFeatureSelector extends AListFeatureSelector {
	public CVectorSequenceFeatureSelector(int p_M2) {
		this.m_M2 = p_M2;
		this.m_FeatureName = CVectorSequence.class.getName();
	}
}
