package liardice;

import java.io.Serializable;

public class ContinueMessage implements Serializable {
    public final boolean doContinue;
    public final String id;
    
    public ContinueMessage(String id, boolean doContinue) {
        this.id = id;
        this.doContinue = doContinue;
    }
}