package com.example.scheduo.domain.schedule.entity;

import lombok.Getter;

@Getter
public enum Color {
	RED("#FF999999"),
	ORANGE("#FFBB80"),
	AMBER("#FFE080"),
	GREEN("#99D9B3"),
	TEAL("#80CCCC"),
	CYAN("#80CCFF"),
	PURPLE("#C299FF"),
	PINK("#FF99C2"),
	GRAY("#B3B3B3");

	private final String hexCode;

	Color(String hexCode) {
		this.hexCode = hexCode;
	}
}
