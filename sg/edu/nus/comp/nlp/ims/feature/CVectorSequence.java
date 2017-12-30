package sg.edu.nus.comp.nlp.ims.feature;

/**
 * 
 * @author Jiayee
 *
 */

public class CVectorSequence extends AListFeature {
	private static final long serialVersionUID = 1L;

	public CVectorSequence() {
	}

	/*
	 * (non-Javadoc)
	 * @see sg.edu.nus.comp.nlp.ims.feature.AListFeature#clone()
	 */
	public Object clone() {
		CVectorSequence clone = new CVectorSequence();
		clone.m_Key = this.m_Key;
		clone.m_Value = this.m_Value;
		return clone;
	}
}
