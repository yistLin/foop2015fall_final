package liardice.message;

import java.io.Serializable;

public class BidMessage implements Serializable {
	public final int numberOfDice;
	public final int valueOfDice;

	public BidMessage(int numberOfDice, int valueOfDice) {
        this.numberOfDice = numberOfDice;
        this.valueOfDice = valueOfDice;
    }
}
