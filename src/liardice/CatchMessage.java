package liardice;

import java.io.Serializable;

public class CatchMessage implements Serializable {
	public final boolean doCatch;
    public final String id;
    
	public ChatMessage(String id, boolean doCatch) {
        this.id = id;
        this.doCatch = doCatch;
    }
}