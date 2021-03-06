/**
 * IMS (It Makes Sense) -- NUS WSD System
 * Copyright (c) 2010 National University of Singapore.
 * All Rights Reserved.
 */
package sg.edu.nus.comp.nlp.ims.implement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sg.edu.nus.comp.nlp.ims.classifiers.CGravesLSTMTrainer;
import sg.edu.nus.comp.nlp.ims.classifiers.IModelTrainer;
import sg.edu.nus.comp.nlp.ims.corpus.ACorpus;
import sg.edu.nus.comp.nlp.ims.corpus.CLexicalCorpus;
import sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination;
import sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor;
import sg.edu.nus.comp.nlp.ims.instance.CInstanceExtractor;
import sg.edu.nus.comp.nlp.ims.instance.IInstance;
import sg.edu.nus.comp.nlp.ims.instance.IInstanceExtractor;
import sg.edu.nus.comp.nlp.ims.io.CGravesLSTMModelWriter;
import sg.edu.nus.comp.nlp.ims.io.IModelWriter;
import sg.edu.nus.comp.nlp.ims.lexelt.CCollocationFeatureSelector;
import sg.edu.nus.comp.nlp.ims.lexelt.CFeatureSelectorCombination;
import sg.edu.nus.comp.nlp.ims.lexelt.CLexelt;
import sg.edu.nus.comp.nlp.ims.lexelt.CPOSFeatureSelector;
import sg.edu.nus.comp.nlp.ims.lexelt.CSurroundingWordFeatureSelector;
import sg.edu.nus.comp.nlp.ims.lexelt.CVectorSequenceFeatureSelector;
import sg.edu.nus.comp.nlp.ims.lexelt.IFeatureSelector;
import sg.edu.nus.comp.nlp.ims.lexelt.ILexelt;
import sg.edu.nus.comp.nlp.ims.util.CArgumentManager;
import sg.edu.nus.comp.nlp.ims.util.CJWNL;
import sg.edu.nus.comp.nlp.ims.util.COpenNLPPOSTagger;
import sg.edu.nus.comp.nlp.ims.util.COpenNLPSentenceSplitter;

/**
 * main interface of training.
 *
 * @author zhongzhi
 * 
 * Modified by @author Jiayee
 *
 */
public class CTrainModel {

	// models
	protected ArrayList<Object> m_Models = new ArrayList<Object>();
	// model trainer
	protected IModelTrainer m_Trainer = new CGravesLSTMTrainer();
	// model writer
	protected IModelWriter m_Writer = new CGravesLSTMModelWriter();
	// corpus class name
	protected String m_CorpusName = CLexicalCorpus.class.getName();
	// instance extractor class name
	protected String m_InstanceExtractorName = CInstanceExtractor.class.getName();

	// cut off parameters
	protected Hashtable<String, Integer> m_CutOffs = new Hashtable<String, Integer>();
	// delimiter
	protected String m_Delimiter = null;
	// sentence split
	protected boolean m_Split = false;
	// tokenized
	protected boolean m_Tokenized = false;
	// lemmatized
	protected boolean m_Lemmatized = false;
	// pos tagged
	protected boolean m_POSTagged = false;

	/*** new features ***/ 
	
	// embeddings window size
	protected int windowSize;

	protected String integrationStrategy;
	
	// embeddings only flag 
	private boolean onlyEmbed;
	private boolean skipSur = true;
	private boolean skipCol = true;
	private boolean skipPOS = true;
	
	/*** end new features ***/
	
	
	/**
	 * default constructor
	 */
	public CTrainModel() {
	}

	/**
	 * train model with given xml and key
	 *
	 * @param p_XmlFile
	 *            train xml file
	 * @param p_KeyFile
	 *            train key file
	 * @throws Exception
	 *             train exception
	 */
	public void train(String p_XmlFile, String p_KeyFile, String embFile) throws Exception {
		Reader xmlReader = new InputStreamReader(new FileInputStream(p_XmlFile));
		BufferedReader keyReader = new BufferedReader(new InputStreamReader(new FileInputStream(p_KeyFile)));
		this.train(xmlReader, keyReader, embFile);
 
		xmlReader.close();
		keyReader.close();
	}

	/**
	 * train model with given xml and key
	 * 
	 * Sample key file format:
	 * 
	 * SensEval 2 all-words task:
	 * d02 d02.s81.t14 publish%2:32:00::
	 * 
	 * SensEval 3 all-words task:
	 * d002 d002.s142.t001 be%2:42:03::
	 * 
	 * SemEval 2007 fine-grained all-words task:
	 * <answer head="d02.s57.t20" senseid="salute%2:32:01::"/>
	 * Reformatted:
	 * d02 d02.s57.t20 salute%2:32:01::
	 * 
	 * SemEval 2007 coarse-grained all-words task:
	 * d005 d005.s034.t008 great%5:00:01:important:00 great%5:00:00:good:01 great%5:00:00:enthusiastic:00
	 * 
	 * Documented by @author Jiayee
	 *
	 * @param p_XmlReader
	 *            train xml file reader
	 * @param p_KeyReader
	 *            train key file reader
	 * @throws Exception
	 *             train exception
	 */
	public void train(Reader p_XmlReader, BufferedReader p_KeyReader, String embFile)
			throws Exception {
		// 1. Read key file and store { instance id: [sense id] } as a hash map in memory
		StringTokenizer tokenizer = null; // works like an iterator of tokens once initialized with a string
		Hashtable<String, String[]> tags = new Hashtable<String, String[]>(); // { instance id: [sense id] }
		String id;
		String line;
		while ((line = p_KeyReader.readLine()) != null) {
			tokenizer = new StringTokenizer(line);
			tokenizer.nextToken(); // Skip first token (e.g. d02, d002, etc.)
			id = tokenizer.nextToken();
			String[] ss = new String[tokenizer.countTokens()];
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				ss[i++] = tokenizer.nextToken();
			}
			tags.put(id, ss);
		}

		// 2. Create corpus instance depending on the corpus/task type
		// E.g. CAllWordsCoarseTaskCorpus, CAllWordsFineTaskCorpus, CAllWordsPlainCorpus, CLexicalCorpus (default)
		ACorpus corpus = (ACorpus) Class.forName(this.m_CorpusName).newInstance();

		// 3. Initialize the feature extractor builder (which accepts any number of feature extractors)
		CFeatureExtractorCombination.Builder builder = new CFeatureExtractorCombination.Builder();
		
		// 4. Add the true feature extractors
		if (!this.onlyEmbed) {
			if (!this.skipPOS) {
				builder = builder.addPOSFeature();
			}
			if (!this.skipCol) {
				builder = builder.addCollocationFeature();
			}
			if (!this.skipSur) {
				builder = builder.addSurroundingWordFeature();
			}
		}
		if (!embFile.isEmpty()) {
			switch(this.integrationStrategy) {
				/*
				case "CON":
					builder.addConcatenatedEmbeddingFeature(embFile, windowSize);
					break;
				case "AVG":
					builder.addAveragedEmbeddingFeature(embFile, windowSize);
					break;
				case "FRA":
					builder.addFractionalDecayedEmbeddingFeature(embFile, windowSize);
					break;
				case "EXP":
					builder.addExponentialDecayedEmbeddingFeature(embFile, windowSize);
					break;
				*/
				case "CON":
					builder.addConcatenatedEmbeddingFeature(embFile, windowSize);
					break;
			}
		}

		// 4. Build and instantiate a master feature extractor
		IFeatureExtractor featExtractor = builder.build();

		// 5. Set the corpus instance properties and load the training corpus file into the corpus instance
		if (this.m_Delimiter != null) {
			corpus.setDelimiter(this.m_Delimiter);
		}
		corpus.setSplit(this.m_Split); // Whether sentence-split or not
		corpus.setTokenized(this.m_Tokenized); // Whether tokenized or not
		corpus.setPOSTagged(this.m_POSTagged); // Whether POS-tagged or not
		corpus.setLemmatized(this.m_Lemmatized); // Whether lemmatized or not
		corpus.load(p_XmlReader);

		// 6. Create an instance extractor (i.e. CInstanceExtractor)
		/**
		 * Sample instances
		 * 
		 * SensEval 2 all-words task
		 * <?xml version="1.0"?>
		 * <!DOCTYPE corpus SYSTEM  "all-words.dtd">
		 * <corpus lang="en">
		 * ... (plain text, one word/token per line)
		 * <head id="d02.s81.t07">fellow</head>
		 * ... (plain text, one word/token per line)
		 * </corpus>
		 * 
		 * SensEval 3 lexical task
		 * <?xml version="1.0" encoding="UTF-8"?>
		 * <!DOCTYPE corpus SYSTEM "lexical-sample.dtd">
		 * <corpus lang="english">
		 * <lexelt item="appear.v" pos="unk">
		 * <instance id="appear.v.bnc.00068627" docsrc="BNC">
		 * <context>
		 * Feminist criticism ,  like Marxist ,  is avowedly evaluative ,  which sharply distinguishes it from the generality
		 * of current academic criticism ,  of whatever school .  This is desirable in itself ,  though I do not warm to the
		 * feminist MacCarthyism which subjects texts to a close ,  hostile interrogation in a search for sexist attitudes .
		 * I have attempted to take a rapid view of developments in critical theory , or criticism with a theoretical
		 * consciousness , as they have <head>appeared</head> in British culture in the past twenty years . There is an
		 * immediate contrast with the literary criticism and theory of the early twentieth century ,  in that most recent
		 * work begins and ends in the academy ,  and has little contact with current literary practice .  Both the New
		 * Criticism and Scrutiny  were products of the modernist literary revolution ,  and drew on it for their methods and
		 * their assumptions .
		 * </context>
		 * </instance>
		 * </lexelt>
		 * </corpus>
		 */
		IInstanceExtractor instExtractor = (IInstanceExtractor) Class.forName(this.m_InstanceExtractorName).newInstance();
		instExtractor.setCorpus(corpus);
		instExtractor.setFeatureExtractor(featExtractor);

		// 7. Use the instance extractor to extract <instance> and grouping those with the same <lexelt> together
		Hashtable<String, ILexelt> lexelts = new Hashtable<String, ILexelt>();
		while (instExtractor.hasNext()) { // must have corpus and feature extractor set
			IInstance instance = instExtractor.next();
			String lexeltID = instance.getLexeltID(); // E.g. "appear.v"
			id = instance.getID(); // instance id
			if (!lexelts.containsKey(lexeltID)) {
				// Map lexelt id to CLexelt instance, which can contain many CInstance
				// Representation of a training file
				lexelts.put(lexeltID, new CLexelt(lexeltID));
			}
			if (tags.containsKey(id)) { // tags is a data structure for the key file: { instance id: [sense id] }
				for (String tag : tags.get(id)) { // for each sense id
					instance.setTag(tag); // works like add tag because append option is true by default, see CInstance
				}
			} else {
				throw new Exception("cannot find tag for instance " + id);
			}
			lexelts.get(lexeltID).addInstance(instance, true); // true means to add to the current lexelt's statistics
		}

		// 8. For each lexelt instance
		// 		a. Do feature selection (based on cut-off)
		//		b. Train on the lexelt and get the statistics based on the training file
		for (String lexeltID : lexelts.keySet()) {
			ILexelt lexelt = lexelts.get(lexeltID);
			System.out.println(lexeltID + " " + lexelt.getInstanceIDs().size()); // E.g. collaborate.v 57
			ArrayList<IFeatureSelector> selectors = new ArrayList<IFeatureSelector>();

			// -s2 cut off for surrounding word (default 0)
			// -c2 cut off for collocation (default 0)
			// -p2 cut off for pos (default 0)
			int s2 = 0, c2 = 0, p2 = 0;
			if (this.m_CutOffs.containsKey("s2")) {
				s2 = this.m_CutOffs.get("s2");
			}
			if (s2 > 1) {
				selectors.add(new CSurroundingWordFeatureSelector(s2));
			}
			if (this.m_CutOffs.containsKey("c2")) {
				c2 = this.m_CutOffs.get("c2");
			}
			if (c2 > 1) {
				selectors.add(new CCollocationFeatureSelector(c2));
			}
			if (this.m_CutOffs.containsKey("p2")) {
				p2 = this.m_CutOffs.get("p2");
			}
			if (p2 > 1) {
				selectors.add(new CPOSFeatureSelector(p2));
			}
			// Default: No cut-off for sequence vectors
			selectors.add(new CVectorSequenceFeatureSelector(0));

			IFeatureSelector masterSelector = new CFeatureSelectorCombination(selectors);
			lexelt.getStatistic().select(masterSelector);
			Object model = this.m_Trainer.train(lexelt);
			System.err.println("done");
			this.m_Models.add(model);
		}
	}

	/**
	 * whether the input is already split
	 * @param p_Split whether split
	 */
	public void setSplit(boolean p_Split) {
		this.m_Split = p_Split;
	}

	/**
	 * whether sentences are already tokenized
	 * @param p_Tokenized whether tokenized
	 */
	public void setTokenized(boolean p_Tokenized) {
		this.m_Tokenized = p_Tokenized;
	}

	/**
	 * whether the pos info is provided
	 * @param p_POSTagged whether pos tagged
	 */
	public void setPOSTagged(boolean p_POSTagged) {
		this.m_POSTagged = p_POSTagged;
	}

	/**
	 * whether the lemma info is provided
	 * @param p_Lemmatized whether lemmatized
	 */
	public void setLemmatized(boolean p_Lemmatized) {
		this.m_Lemmatized = p_Lemmatized;
	}

	/**
	 * set the delimiter
	 * @param p_Delimiter delimiter
	 */
	public void setDelimiter(String p_Delimiter) {
		this.m_Delimiter = p_Delimiter;
	}

	/**
	 * set model trainer
	 *
	 * @param p_ModelTrainer
	 *            model trainer
	 */
	public void setModelTrainer(IModelTrainer p_ModelTrainer) {
		this.m_Trainer = p_ModelTrainer;
	}

	/**
	 * set model writer
	 *
	 * @param p_ModelWriter
	 *            model writer
	 */
	public void setModelWriter(IModelWriter p_ModelWriter) {
		this.m_Writer = p_ModelWriter;
	}

	/**
	 * set the corpus class name
	 *
	 * @param p_Name
	 *            corpus class name
	 */
	public void setCorpusClassName(String p_Name) {
		this.m_CorpusName = p_Name;
	}

	/**
	 * set the instance extractor name
	 *
	 * @param p_Name
	 *            instance extractor name
	 */
	public void setInstanceExtractorName(String p_Name) {
		this.m_InstanceExtractorName = p_Name;
	}

	/**
	 * set cut off
	 *
	 * @param p_Key
	 *            key name
	 * @param p_Value
	 *            value
	 */
	public void setCutOff(String p_Key, int p_Value) {
		this.m_CutOffs.put(p_Key, p_Value);
	}

	/**
	 * get models
	 *
	 * @return models
	 */
	public ArrayList<Object> getModels() {
		return this.m_Models;
	}

	/**
	 * clear the generated models
	 */
	public void clear() {
		this.m_Models.clear();
	}

	/**
	 * write models to disk
	 *
	 * @throws IOException
	 *             exception while save model
	 */
	public void write() throws IOException {
		for (Object modelInfo : this.m_Models) {
			this.m_Writer.write(modelInfo);
		}
	}

	private void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setIntegrationStrategy(String integrationStrategy) {
		this.integrationStrategy = integrationStrategy;
	}
	
	private void setSkipSur(boolean skipSur) {
		this.skipSur = skipSur;
	}

	private void setSkipCol(boolean skipCol) {
		this.skipCol = skipCol;
	}

	private void setSkipPOS(boolean skipPOS) {
		this.skipPOS = skipPOS;		
	}
	
	private void setOnlyEmbed(boolean onlyEmbed) {
		this.onlyEmbed = onlyEmbed;
	}

	/**
	 * @param p_Args
	 *            arguments
	 */
	public static void main(String[] p_Args) {
		try {
			String generalOptions = "Usage: train.xml train.key saveDir\n"
				+ "\t-i class name of Instance Extractor(default sg.edu.nus.comp.nlp.ims.instance.CInstanceExtractor)\n"
				+ "\t-f class name of Feature Extractor(default sg.edu.nus.comp.nlp.ims.feature.CMixedFeatureExtractor)\n"
				+ "\t-c class name of Corpus(default sg.edu.nus.comp.nlp.ims.corpus.CLexicalCorpus)\n"
				+ "\t-t class name of Trainer(default sg.edu.nus.comp.nlp.ims.classifiers.CGravesLSTMTrainer)\n"
				+ "\t-m class name of Model Writer(default sg.edu.nus.comp.nlp.ims.io.CGravesLSTMModelWriter)\n"
				+ "\t-algorithm svm(default) or naivebayes\n"
				+ "\t-s2 cut off for surrounding word(default 0)\n"
				+ "\t-c2 cut off for collocation(default 0)\n"
				+ "\t-p2 cut off for pos(default 0)\n"
				+ "\t-split 1/0 whether the corpus is sentence splitted(default 0)\n"
				+ "\t-ssm path of sentence splitter model\n"
				+ "\t-token 1/0 whether the corpus is tokenized(default 0)\n"
				+ "\t-pos 1/0 whether the pos tag is provided in corpus(default 0)\n"
				+ "\t-ptm path of pos tagger model\n"
				+ "\t-dict path of dictionary for opennlp POS tagger\n"
				+ "\t-tagdict path of tagdict for opennlp POS tagger\n"
				+ "\t-lemma 1/0 whether the lemma is provided in the corpus(default 0)\n"
				+ "\t-prop path of prop.xml for JWNL\n"
				+ "\t-type type of train.xml\n"
				+ "\t\tdirectory train all xml files under directory trainPath\n"
				+ "\t\tlist train all xml files listed in file trainPath\n"
				+ "\t\tfile(default) train file trainPath\n"
				+ "\t-onlyEmb skip not embeddings features path\n"
				+ "\t-skipPos skip POS features\n"
				+ "\t-skipCol skip Collocations features\n"
				+ "\t-skipSur skip Surrounding word features\n"
				+ "\t-emb embeddings file path\n"
				+ "\t-ws embeddings windows size\n"
				+ "\t-str embeddings integration strategy (CON, AVG, FRA, EXP) \n";
				
			CArgumentManager argmgr = new CArgumentManager(p_Args);
			CTrainModel trainModel = new CTrainModel();
			String type = "file";
			if (argmgr.has("type")) {
				type = argmgr.get("type");
			}
			if (argmgr.size() != 3) { // check arguments
				throw new IllegalArgumentException(generalOptions);
			}
			if (!argmgr.has("prop")) {
				System.err.println("prop.xml file for JWNL has not been set.");
				throw new IllegalArgumentException(generalOptions);
			}
			CJWNL.initial(new FileInputStream(argmgr.get("prop")));
			File trainXmlDir = new File(argmgr.get(0));
			File trainKeyDir = new File(argmgr.get(1));

			// set model writer
			String writerName = CGravesLSTMModelWriter.class.getName();
			if (argmgr.has("w")) {
				writerName = argmgr.get("w");
			}
			IModelWriter writer = (IModelWriter) Class.forName(writerName)
					.newInstance();
			writer.setOptions(new String[] { "-m", argmgr.get(2) });

			// set sentence splitter
			if (argmgr.has("split")) {
				if (Integer.parseInt(argmgr.get("split")) == 1) {
					trainModel.setSplit(true);
				}
			}
			if (argmgr.has("ssm")) {
				COpenNLPSentenceSplitter.setDefaultModel(argmgr.get("ssm"));
			}

			if (argmgr.has("token")) {
				if (Integer.parseInt(argmgr.get("token")) == 1) {
					trainModel.setTokenized(true);
				}
			}

			// set pos tagger
			if (argmgr.has("pos")) {
				if (Integer.parseInt(argmgr.get("pos")) == 1) {
					trainModel.setPOSTagged(true);
					trainModel.setTokenized(true);
				}
			}
			if (argmgr.has("ptm")) {
				COpenNLPPOSTagger.setDefaultModel(argmgr.get("ptm"));
			}
			if (argmgr.has("dict")) {
				COpenNLPPOSTagger.setDefaultDictionary(argmgr.get("dict"));
			}
			if (argmgr.has("tagdict")) {
				COpenNLPPOSTagger
						.setDefaultPOSDictionary(argmgr.get("tagdict"));
			}

			if (argmgr.has("lemma")) {
				if (Integer.parseInt(argmgr.get("lemma")) == 1) {
					trainModel.setLemmatized(true);
					trainModel.setTokenized(true);
				}
			}

			// set trainer
			String trainerName = CGravesLSTMTrainer.class.getName();
			IModelTrainer trainer = null;
			if (argmgr.has("t")) {
				trainerName = argmgr.get("t");
			}
			trainer = (IModelTrainer) Class.forName(trainerName).newInstance();

			trainModel.setModelWriter(writer);
			trainModel.setModelTrainer(trainer);
			if (argmgr.has("i")) {
				trainModel.setInstanceExtractorName(argmgr.get("i"));
			} 
			
			/** new options */
			
			File embFile = null;
			if (argmgr.has("emb")) {
				embFile = new File(argmgr.get("emb"));
			}
			if (argmgr.has("ws")) {
				trainModel.setWindowSize(Integer.parseInt(argmgr.get("ws")));
			}
			if (argmgr.has("str")) {
				trainModel.setIntegrationStrategy(argmgr.get("str"));
			}
			
			if (argmgr.has("onlyEmb")) {
				trainModel.setOnlyEmbed(true);
			}
			if (argmgr.has("skipPos")) {
				trainModel.setSkipPOS(true);
			}
	
			if (argmgr.has("skipCol")) {
				trainModel.setSkipCol(true);
			}
			if (argmgr.has("skipSur")) {
				trainModel.setSkipSur(true);
			}
			
			/** end options */
			
			if (argmgr.has("c")) {
				trainModel.setCorpusClassName(argmgr.get("c"));
			}
			if (argmgr.has("s2")) {
				int s2 = Integer.parseInt(argmgr.get("s2"));
				trainModel.setCutOff("s2", s2);
			}
			if (argmgr.has("c2")) {
				int c2 = Integer.parseInt(argmgr.get("c2"));
				trainModel.setCutOff("c2", c2);
			}
			if (argmgr.has("p2")) {
				int p2 = Integer.parseInt(argmgr.get("p2"));
				trainModel.setCutOff("p2", p2);
			}
			if (argmgr.has("p2")) {
				int p2 = Integer.parseInt(argmgr.get("p2"));
				trainModel.setCutOff("p2", p2);
			}
			
			
			ArrayList<File> trainXmlList = new ArrayList<File>();
			ArrayList<File> trainKeyList = new ArrayList<File>();
			Pattern xmlPattern = Pattern.compile("([^\\/]*)\\.xml$");
			Matcher matcher = null;
			if (type.equalsIgnoreCase("list")) { // in file
				String line;
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(trainXmlDir)));
				while ((line = reader.readLine()) != null) {
					trainXmlList.add(new File(line));
				}
				reader.close();
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(trainKeyDir)));
				while ((line = reader.readLine()) != null) {
					trainKeyList.add(new File(line));
				}
				reader.close();
				if (trainXmlList.size() != trainKeyList.size()) {
					throw new Exception(
							"Error: the numbers of xml files and key files do not match.");
				}
			} else if (type.equalsIgnoreCase("directory")) {
				if (!trainXmlDir.exists() || !trainXmlDir.isDirectory()
						|| !trainKeyDir.exists() || !trainKeyDir.isDirectory()) {
					throw new Exception("Error: cannot find directory "
							+ trainXmlDir.getName() + " or "
							+ trainKeyDir.getName() + "!\n");
				}
				File[] trainXmlFiles = trainXmlDir.listFiles();
				for (File xmlFile : trainXmlFiles) {
					matcher = xmlPattern.matcher(xmlFile.getAbsolutePath());
					if (matcher.find()) {
						File keyFile = new File(trainKeyDir + "/"
								+ matcher.group(1) + ".key");
						if (!keyFile.exists() || !keyFile.isFile()) {
							throw new Exception(
									"Error: cannot find key file for "
											+ xmlFile.getAbsolutePath());
						}
						trainXmlList.add(xmlFile);
						trainKeyList.add(keyFile);
					}
				}
			} else {
				trainXmlList.add(trainXmlDir);
				trainKeyList.add(trainKeyDir);
			}
			for (int i = 0; i < trainXmlList.size(); i++) {
				File xmlFile = trainXmlList.get(i);
				File keyFile = trainKeyList.get(i);
				System.err.println(xmlFile.getAbsolutePath());
				trainModel.train(
						xmlFile.getAbsolutePath(),
						keyFile.getAbsolutePath(),
						(embFile != null) ? embFile.getAbsolutePath() : ""
				);
				trainModel.write();
				trainModel.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}







}
