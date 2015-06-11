package com.shawn.sample;

import java.util.Comparator;

import com.shawn.sample.MainActivity.Item;

public class PinyinComparator implements Comparator<Item> {

	public int compare(Item o1, Item o2) {
		if (o1.sortLetter.equals("@")
				|| o2.sortLetter.equals("#")) {
			return -1;
		} else if (o1.sortLetter.equals("#")
				|| o2.sortLetter.equals("@")) {
			return 1;
		} else {
			return o1.sortLetter.compareTo(o2.sortLetter);
		}
	}

}
