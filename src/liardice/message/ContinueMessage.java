package liardice.message;

import java.io.Serializable;

public class ContinueMessage implements Serializable {
    public final boolean doContinue;
    
    public ContinueMessage(boolean doContinue) {
        this.doContinue = doContinue;
    }
}