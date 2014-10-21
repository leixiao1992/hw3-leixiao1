package edu.cmu.lti.f14.hw3.hw3_leixiao1.casconsumers;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_leixiao1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_leixiao1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_leixiao1.utils.Utils;

/**
 * Problem: when some document sentences have the same score, it will only
 * choose the first one
 * 
 * @author leixiao
 *
 */
public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	/** record the number of the sentence **/
	public ArrayList<Integer> senList;

	/** record the token and its frequency in one sentence **/
	public ArrayList<Map> al;

	/** record the cosine similarity for each sentence **/
	public ArrayList<Double> score;

	/** record the vector of the two sentences **/
	public ArrayList<Integer> s1;

	public ArrayList<Integer> s2;

	/** record the rank of each document sentence with the format <senId,rank> **/
	public Map<Integer, Integer> rankList;

	/** sort the rank **/
	public ArrayList<Double> r;

	/** get the rank score for each query **/
	public ArrayList<Integer> rankScore;

	/** record the sentence text **/
	public ArrayList<String> sentenceList;

	/** record the rank of each document sentence with the format <senId,score> **/
	public ArrayList<Double> newrank;

	/**
	 * record the highest score for each query and its sentenceID with the
	 * format <senId,score>
	 **/
	public Map<Integer, Double> scoreList;

	public ArrayList<Map> df;

	public Map<Integer, Integer> newscoremap;

	public int sen = 0;

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		senList = new ArrayList<Integer>();

		al = new ArrayList<Map>();

		df = new ArrayList<Map>();

		score = new ArrayList<Double>();

		s1 = new ArrayList<Integer>();

		s2 = new ArrayList<Integer>();

		rankList = new HashMap<Integer, Integer>();

		r = new ArrayList<Double>();

		rankScore = new ArrayList<Integer>();

		sentenceList = new ArrayList<String>();

		scoreList = new HashMap<Integer, Double>();

		newscoremap = new HashMap<Integer, Integer>();
		newrank = new ArrayList<Double>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		sen++;

		JCas jcas;
		try {
			jcas = aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

		if (it.hasNext()) {

			// Map<String,Integer> map = new HashMap<String,Integer>();
			al.add(new HashMap<String, Integer>());

			// System.out.println("sen" + sen);
			Document doc = (Document) it.next();

			// Make sure that your previous annotators have populated this in
			// CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
			Iterator<Token> iter = tokenList.iterator();

			/**
			 * read one sentence text from the document, and got each token and
			 * its frequency in each sentence and now deal with the word, to
			 * find the frequency of each word in each document with the same
			 * qid
			 */
			while (iter.hasNext()) {
				Token token = iter.next();
				// System.out.println(token.getText());
				// System.out.println(token.getFrequency());
				String text = token.getText();
				int frequency = token.getFrequency();
				al.get(sen - 1).put(text, frequency);
			}

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			senList.add(sen - 1);
			sentenceList.add(doc.getText());

			// Do something useful here

		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
		int i = 0;
		int id = qIdList.get(0);
		int querynumber = 0;

		/** recognize the query and documents with the relevance value
		 * if the rel=99, the sentence will be a query
		 * and if the rel=1 or 0, the sentence will be doucument
		 * */
		
		for (i = 0; i < relList.size(); i++) {
			if (qIdList.get(i) == id) {
				if (relList.get(i) == 99) {
					// System.out.println(i + " is query");
					querynumber = i;
					score.add(0.0);
				} else {

					double sc = computeCosineSimilarity((HashMap<String, Integer>) al.get(querynumber),
							(HashMap<String, Integer>) al.get(i));
					score.add(sc);
					newrank.add(sc);
					// System.out.println("Sentence  " + i + " has " + sc +
					// " score");
				}
			} else {

				Collections.sort(newrank);
				Collections.reverse(newrank);
				
				/**only check the rank of the sentence whose rel=1**/
				
				int s = 0;
				int l = 0;
				for (s = 0; s < newrank.size(); s++) {

					if (relList.get(i - 1 - s) == 1) {
						l = i - 1 - s;
						break;
					}
				}
				// System.out.println("Sentence  "+ (l) +" has rank:");
				
				/**if the rel=1, we will check the rank by sorting the newrank list**/
				
				for (int j = 0; j < newrank.size(); j++) {

					// System.out.println();
					if (score.get(l).equals(newrank.get(j))) {

						newscoremap.put(l, j + 1);
						// System.out.println("Sentence  " + (l) + " has rank:"
						// + (j + 1));
						break;
					}

				}
				newrank.clear();

				/****************/

				id = qIdList.get(i);
				i--;
			}

		}

		Collections.sort(newrank);
		Collections.reverse(newrank);

		int s = 0;
		int l = 0;
		for (s = 0; s < newrank.size(); s++) {

			if (relList.get(i - 1 - s) == 1) {
				l = i - 1 - s;
				break;
			}
		}
		// System.out.println("Sentence  "+ (l) +" has rank:");
		for (int j = 0; j < newrank.size(); j++) {

			// System.out.println();
			if (score.get(l).equals(newrank.get(j))) {

				newscoremap.put(l, j + 1);
				// System.out.println("Sentence  " + (l) + " has rank:" + (j +
				// 1));
				break;
			}

		}
		newrank.clear();

		/****************/

		// TODO :: compute the metric:: mean reciprocal rank

		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);

		/** compute the report.txt **/
		/**
		 * with the format of
		 * cosine=...<tab>rank=...<tab>qid=...<tab>rel=1<tab><sentence>
		 **/
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(4);

		FileWriter writer = new FileWriter("report.txt");

		for (l = 0; l < senList.size(); l++) {

			double scores = score.get(l);

			int ranks = 0;
			if (relList.get(l) == 1) {
				ranks = newscoremap.get(l);
				String m = "cosine=" + nf.format(scores) + "\t" + "rank=" + ranks + "\t" + "qid=" + qIdList.get(l)
						+ "\t" + "rel=" + relList.get(l) + "\t" + sentenceList.get(l) + "\n";

				writer.write(m);

			}
		}

		writer.write("MRR=" + metric_mrr + "\n");
		writer.close();

	}

	/**
	 *@param queryVector
	 *@param docVector
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		int vec = 0;

		Map<String, Integer> temp = new HashMap<String, Integer>();

		// TODO :: compute cosine similarity between two sentences

		temp.putAll(queryVector);
		temp.putAll(docVector);

		/**combine the two map together to form a new map which contains the key in query or doc**/
		for (Map.Entry<String, Integer> entry : temp.entrySet()) {
			String a = entry.getKey();

			/**if the key is both in query and document, we will calculate **/
			if (queryVector.containsKey(a) && docVector.containsKey(a)) {
				vec += queryVector.get(a) * docVector.get(a);
			}

		}

		double doc1 = 0.0;
		double doc2 = 0.0;

		/**using the cosine similarity formula to calculate the score**/
		
		for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
			doc1 += Math.pow(entry.getValue(), 2);
		}
		for (Map.Entry<String, Integer> entry : docVector.entrySet()) {
			doc2 += Math.pow(entry.getValue(), 2);
		}

		double sq1 = Math.sqrt(doc1);
		double sq2 = Math.sqrt(doc2);

		cosine_similarity = (double) (vec) / (double) (sq1 * sq2);

		return cosine_similarity;
	}

	/**
	 * @param newscoremap
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr = 0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

		int num = newscoremap.size();
		double ranksum = 0.0;

		/**using the formula of MRR to calculate the score**/
		
		for (Map.Entry<Integer, Integer> entry : newscoremap.entrySet()) {
			int a = entry.getKey();
			int b = entry.getValue();
			// System.out.println("sentence " + a + " " + "rank:" + b);
			ranksum += (double) 1 / (double) b;

		}
		metric_mrr = (double) ranksum / (double) num;

		return metric_mrr;
	}

}