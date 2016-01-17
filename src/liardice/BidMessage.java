package liardice;

import java.io.Serializable;

public class BidMessage implements Serializable {
    public final String id;
    public final int numberOfDice;
    public final int valueOfDice;
    
    public BidMessage(String id, int numberOfDice, int valueOfDice) {
        this.id = id;
        this.numberOfDice = numberOfDice;
        this.valueOfDice = valueOfDice;
    }
}