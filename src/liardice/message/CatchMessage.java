package liardice.message;

import java.io.Serializable;

public class CatchMessage implements Serializable {
    public final boolean doCatch;
    
    public CatchMessage(boolean doCatch) {
        this.doCatch = doCatch;
    }
}