package liardice;

import java.io.Serializable;

public class Dice implements Serializable {

	public final int value;
	private final int MAX = 6;
	private final int MIN = 1;

	public Dice() {
		this.value = MIN + (int)Math.floor( Math.random() * (MAX - MIN + 1) );
	}

	public String toString() {
		return new String(Integer.toString(value));
	}
}