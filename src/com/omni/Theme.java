package com.omni;

import java.awt.Color;

public enum Theme {
	
	DICT(Color.DARK_GRAY),
	THES(Color.BLACK),
	MUSIC(Color.RED);
	
	private final Color background;
	
	Theme(Color background) {
		this.background = background;
	}
	
	public Color getBackground() {
		return background;
	}

	public static Theme next(Theme mode) {
		switch(mode) {
		case THES:
			return MUSIC;
		case DICT:
			return THES;
		default:
			return DICT;
		}
	}

}
