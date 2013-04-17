package com.omni;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.InflaterInputStream;

public class Words {
	
	private static Map<String, ArrayList<Word>> dictonary = new HashMap<String, ArrayList<Word>>();
	private static Map<String, Word> words = new HashMap<String, Word>();
	
	private static boolean loaded;
	
	private static double percent;
	
	//
	public static void init(InputStream file) {
		try {
			DataInputStream input = new DataInputStream(new InflaterInputStream(file));
			int size = input.readInt();
			int count = size;
			for (int i = 0; i < size; i++) {
				Word word = new Word(input.readUTF());
				boolean pNull = input.readBoolean();
				boolean eNull = input.readBoolean();
				int dSize = input.readShort();
				int sSize = input.readShort();
				if (!pNull)
					word.setPronunciation(input.readUTF());
				if (!eNull)
					word.setSpeech(Speech.valueOf(input.readUTF()));
				for (int x = 0; x < dSize; x++)
					word.getDefinitions().add(input.readUTF());
				for (int y = 0; y < sSize; y++)
					word.getSynonyms().add(input.readUTF());
				ArrayList<Word> resolve = dictonary.get(word.getText().toLowerCase().substring(0, 1));
				if(resolve == null) {
					resolve = new ArrayList<Word>();
					dictonary.put(word.getText().toLowerCase().substring(0, 1), resolve);
				}
				resolve.add(word);
				Words.words.put(word.getText().toLowerCase(), word);
				count--;
				percent = (((double) size - (double) count) / (double) size);
			}
			for(Entry<String, ArrayList<Word>> set : dictonary.entrySet()) {
				Collections.sort(set.getValue(),  new Comparator<Word>() {
					@Override
					public int compare(Word w1, Word w2) {
						return w1.getText().length() - w2.getText().length();
					}
				});
			}
			loaded = true;
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isDictionaryWord(String word) {
		return words.get(word.toLowerCase()) != null;
	}
	
	public static String ending(String start) {
		if(start.length() <= 0) 
			return "";
		List<Word> words = dictonary.get(start.toLowerCase().substring(0, 1));
		if(words == null) 
			return null;
		for(int i = 0; i < words.size(); i++) {
			Word s = words.get(i);
			String string = s.getText().toLowerCase();
			if(string.startsWith(start.toLowerCase()) && string.length() > start.length()) 
				return string.replace(start.toLowerCase(), "");
		}
		return null;
	}

	public static double getPercent() {
		return percent;
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static Word getWord(String string) {
		return words.get(string.toLowerCase());
	}

}
