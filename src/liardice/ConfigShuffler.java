package liardice;

import java.util.*;
import org.json.*;
import java.io.*;

public class ConfigShuffler {

	private final String dir_path = "./config/";
	String[] filenames;
	Random rand;
	int top, len;

	public ConfigShuffler() {
		File dir_node = new File(dir_path);
    	rand = new Random();
   		filenames = dir_node.list();
   		len = filenames.length;
   		top = len;
	}

	private void shuffle() {
		int k;
		String swap;
		for (int i = 0; i < len; i++) {
			k = i + (int)(Math.random() * (len - i));
			swap = filenames[k];
			filenames[k] = filenames[i];
			filenames[i] = swap;
		}
	}

	public String getNext() {
		if (top == len) {
			top = 0;
			shuffle();
		}
		return new String(dir_path + filenames[top++]);
	}
}