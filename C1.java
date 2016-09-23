package p1;

import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class C1 {
	
	public static Map<String, Integer> getFeatures(int wordOfInterest, List<Word> sentence, String prevLabel) {
		Map<String, Integer> features = new HashMap<>();
		String word = sentence.get(wordOfInterest).theWord;
		String prevWord = "";
		features.put("w0="+word, 1);
		if (wordOfInterest > 0) {
			features.put("w-1="+sentence.get(wordOfInterest-1), 1);
			if (wordOfInterest > 1) {
				features.put("w-2="+sentence.get(wordOfInterest-2), 1);
				if (wordOfInterest > 2) {
					features.put("w-3="+sentence.get(wordOfInterest-3), 1);
				}
			}
			
			int len = word.length();
			features.put("p1="+word.substring(0,1), 1);
			features.put("p2="+word.substring(len-1), 1);
			if (len > 1) {
				features.put("p1p2="+word.substring(0,2), 1);
				features.put("s1s2="+word.substring(len-2), 1);
				if (len > 2) {
					features.put("p1p2p3="+word.substring(0,3), 1);
					features.put("s1s2s3="+word.substring(len-3), 1);
					if (len > 3) {
						features.put("p1p2p3p4="+word.substring(0,3), 1);
						features.put("s1s2s3s4="+word.substring(len-4), 1);
					}
				}
			}
			
			prevWord = sentence.get(wordOfInterest-1).theWord;
			int pLen = prevWord.length();
			features.put("prevP1="+prevWord.substring(0,1), 1);
			features.put("prevS1="+prevWord.substring(pLen-1), 1);
			if (pLen > 1) {
				features.put("prevP1P2="+prevWord.substring(0,2), 1);
				features.put("prevS1S2="+prevWord.substring(pLen-2), 1);
				if (pLen > 2) {
					features.put("prevP1P2P3="+prevWord.substring(0,3), 1);
					features.put("prevS1S2S3="+prevWord.substring(pLen-3), 1);
					if (pLen > 3) {
						features.put("prevP1P2P3P4="+prevWord.substring(0,3), 1);
						features.put("prevS1S2S3S4="+prevWord.substring(pLen-4), 1);
					}
				}
			}
			features.put("prevLABEL="+prevLabel, 1);
			
			if (Character.isUpperCase(prevWord.charAt(0))) {
				features.put("prevInitUC", 1);
				if (word.equals(prevWord.toUpperCase())) {
					features.put("prevAllUC", 1);
				}
			} else {
				features.put("prevInitLC", 1);
				if (word.equals(prevWord.toLowerCase())) {
					features.put("prevAllLC", 1);
				}
			}
			
			Boolean pHN = false, pHP = false;
			for (char c : prevWord.toCharArray()) {
				if (Character.isDigit(c)) {
					pHN = true;
				} else if (!Character.isLetter(c)) {
					pHP = true;
				}
			}
			if (pHN)
				features.put("pHasNum", 1);
			if (pHP)
				features.put("pHasPunc", 1);
			
		}
		
		if (wordOfInterest < sentence.size() - 1) {
			features.put("w+1="+sentence.get(wordOfInterest+1), 1);
			if (wordOfInterest < sentence.size() - 2) {
				features.put("w+2="+sentence.get(wordOfInterest+2), 1);
				if (wordOfInterest < sentence.size() - 3) {
					features.put("w+3="+sentence.get(wordOfInterest+3), 1);
				}
			}
		}
		
		if (Character.isUpperCase(word.charAt(0))) {
			features.put("initUC", 1);
			if (word.equals(word.toUpperCase())) {
				features.put("allUC", 1);
			}
		} else {
			features.put("initLC", 1);
			if (word.equals(word.toLowerCase())) {
				features.put("allLC", 1);
			}
		}
		

		
		Boolean hasNum = false, hasPunc = false;
		for (char c : word.toCharArray()) {
			if (Character.isDigit(c)) {
				hasNum = true;
			} else if (!Character.isLetter(c)) {
				hasPunc = true;
			}
		}
		if (hasNum)
			features.put("hasNUM", 1);
		if (hasPunc)
			features.put("hasPUNC", 1);		
		
		return features;
	}
	
	public static List<List<Word>> createListOfLists(String url, Map<String, Map<String, Double>> m) throws Exception {
		String thisLine = "";
		List<List<Word>> listOfSentences = new ArrayList<List<Word>>();

		// Create list of lists
		BufferedReader in = new BufferedReader(new FileReader(url));
		List<Word> low = new ArrayList<Word>();
		while ((thisLine = in.readLine()) != null) {
		   	String[] split = thisLine.split("\t");
		   	if (thisLine.equals("")) {
		   		listOfSentences.add(low);
		   		low = new ArrayList<Word>();
		   	} else {
		   		Word w = new Word(split[1], split[3]);
		    	low.add(w);	
		    	if (!m.containsKey(split[3])) {
		    		m.put(split[3], new HashMap<String, Double>());
		    	}
		   	}		    	
		}
		in.close();
		return listOfSentences;
	}
	
	public static String predict(Map<String, Integer> f, Map<String, Map<String, Double>> w) {
		String best_label = null;
		int best_score = Integer.MIN_VALUE;
		
		for (String label : w.keySet()) {
			int score_pLabel = 0;
			Map<String, Double> w_label = w.get(label);
			for (String key : f.keySet()) {
				if (w_label.get(key) != null) {
					score_pLabel += f.get(key)*w_label.get(key);
				}
			}
			if (score_pLabel > best_score) {
				best_score = score_pLabel;
				best_label = label;
			}
		}
		
		return best_label;
	}
	
	public static void evaluate(Map<String, Map<String, Double>> w_map, Map<String, Map<String, Integer>> cM) throws Exception {
		// Test with new data
		int right = 0;
		int wrong = 0;
		String hlabel = "";

		//List<List<Word>> listOfSentences = createListOfLists(
		//		"/home/Desktop/treebank data/enParseDevelFile.gold",
		//		w_map);

		// Loop through list of lists
		for (int i = 0; i < listOfSentences.size(); i++) {
			for (int j = 0; j < listOfSentences.get(i).size(); j++) {
				Map<String, Integer> feature = getFeatures(j, listOfSentences.get(i), hlabel);
				List<Word> low = listOfSentences.get(i);
				hlabel = predict(feature, w_map);
				String tlabel = low.get(j).getPOS();
				Map<String, Integer> tCM = cM.get(tlabel);
				if (!cM.containsKey(tlabel)) {
		    		cM.put(tlabel, new HashMap<String, Integer>());
		    		cM.get(tlabel).put(hlabel, 1);
		    	} else {
		    		if (!tCM.containsKey(hlabel)) {
		    			tCM.put(hlabel, 1);
		    		} else {
		    			tCM.put(hlabel, tCM.get(hlabel)+1);
		    		}
		    	}
				if (hlabel.equals(tlabel)) {
					right++;
				} else {
					wrong++;
					/*if ((hlabel.equals("NN") && tlabel.equals("JJ")) || (tlabel.equals("NN") && hlabel.equals("JJ"))) {
						System.err.println();
						for (Word w : low) {
							System.err.print(w.getWord() + " ");
						}
						System.err.println();
						System.err.println("Word: " + low.get(j).getWord() + ", Guess: " + hlabel + ", Actual: " + tlabel);
					}*/
				}
			}
		}

		System.err.println("Percentage right: " + (double) right / (right + wrong));
	}
	
	public static void main (String[] array) throws Exception{
		// Initialize stuff
		Map<String, Map<String, Double>> w_map = new HashMap<String, Map<String, Double>>();
		

		//List<List<Word>> listOfSentences = createListOfLists("/home/Desktop/treebank data/enParseTrainingFile.gold", w_map);
		
		for (int k = 0; k < 20; k++) {
			// Loop through list of lists
			int numUpdates = 0;
			String hlabel = "";
			for (int i = 0; i < listOfSentences.size(); i++) {
				for (int j = 0; j < listOfSentences.get(i).size(); j++) {
					Map<String, Integer> feature = getFeatures(j, listOfSentences.get(i), hlabel);
					hlabel = predict(feature, w_map);
					String tlabel = listOfSentences.get(i).get(j).getPOS();
					Map<String, Double> h_wmap = w_map.get(hlabel);
					Map<String, Double> t_wmap = w_map.get(tlabel);
					if (hlabel.equals(tlabel)) {
					} else {			
						numUpdates++;
						for (String key : feature.keySet()) {
							Double hcount = h_wmap.get(key);
							Double tcount = t_wmap.get(key);
							if (hcount != null && tcount != null) {
								h_wmap.put(key, hcount - 1);
								t_wmap.put(key, tcount + 1);
							} else {
								h_wmap.put(key, -1.0);
								t_wmap.put(key,  1.0);
							}
						} 
					}				
				}
			}
			System.err.println();
			System.err.println("Num updates: " + numUpdates);
			Map<String, Map<String, Integer>> cMatrix = new HashMap<String, Map<String, Integer>>();
			evaluate(w_map, cMatrix);
			List<String> allLabels = new ArrayList<>(cMatrix.keySet());
			Collections.sort(allLabels);
			String prevTK = "";
			for (String hK : allLabels) {
				System.err.print("\t" + hK);
			}
			for (String tK : allLabels) {
				for (String hK : allLabels) {
					if (!prevTK.equals(tK)) {
						System.err.println();
						System.err.print(tK + "\t");
					}
					if (cMatrix.get(tK).get(hK) == null) {
						System.err.print("0" + "\t");
					} else {
						System.err.print(cMatrix.get(tK).get(hK) + "\t");
					}
					prevTK = tK;
				}
			}
			
		}
	}
	
	
}



// hadoop: implements map-reduce which is a framework for doing parallel processing (running using multiple machines).