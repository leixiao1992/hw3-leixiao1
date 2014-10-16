package edu.cmu.lti.f14.hw3.hw3_leixiao1.annotators;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_leixiao1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_leixiao1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_leixiao1.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	public ArrayList<String> wordList;
	public List<String> ss;

	public static int calculate(String input, String target) {
		int count = 0;
		StringTokenizer tokenizer = new StringTokenizer(input);
		while (tokenizer.hasMoreElements()) {
			String element = (String) tokenizer.nextElement();

			/** not ignore the case to compare */

			if (target.equals(element))
				count++;
		}
		return count;
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}

	/**
	 * A basic white-space tokenizer, it deliberately does not split on
	 * punctuation!
	 *
	 * @param doc
	 *            input text
	 * @return a list of tokens.
	 */

	List<String> tokenize0(String doc) {
		List<String> res = new ArrayList<String>();

		for (String s : doc.split("\\s+"))
			res.add(s);
		return res;
	}

	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();

		// TO DO: construct a vector of tokens and update the tokenList in CAS
		// TO DO: use tokenize0 from above
		ArrayList<Token> tokenList = new ArrayList<Token>();

		wordList = new ArrayList<String>();
		
		ss = new ArrayList<String>();
		ss = tokenize0(docText);

		int i;
		String temp;

		for (i = 0; i < ss.size(); i++) {

			temp = ss.get(i);

			/** delete the same token */

			if (wordList.indexOf(temp) != -1) {

				continue;
			}

			wordList.add(temp);
			Token name = new Token(jcas);
			name.setText(temp);
			int count = calculate(docText, temp);
			name.setFrequency(count);

			tokenList.add(name);
			name.addToIndexes();
			
			//System.out.println(temp+" "+count);

		}
		FSList fsTokenList = Utils.fromCollectionToFSList(jcas, tokenList);
		doc.setTokenList(fsTokenList);
		//System.out.println(docText);

	}

}
