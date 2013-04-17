package com.omni;

import java.util.ArrayList;
import java.util.List;

public class Word {

	public Word(String text) {
		this.text = text;
		this.speech      = Speech.UNKNOWN;
		this.synonyms 	 = new ArrayList<String>();
		this.definitions = new ArrayList<String>();
	}
	
	private String pronunciation;
	private Speech speech;
	private final String text;
	private final List<String> synonyms;
	private final List<String> definitions;
	
	public String toString() {
		return text;
	}
	
	public String getText() {
		return text;
	}
	
	public String getPronunciation() {
		return pronunciation;
	}
	
	public void setPronunciation(String pronunciation) {
		this.pronunciation = pronunciation;
	}
	
	public Speech getSpeech() {
		return speech;
	}
	
	public void setSpeech(Speech speech) {
		this.speech = speech;
	}
	
	public List<String> getSynonyms() {
		return synonyms;
	}
	
	public List<String> getDefinitions() {
		return definitions;
	}

}
