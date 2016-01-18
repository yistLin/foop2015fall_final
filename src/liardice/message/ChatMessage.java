package liardice.message;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public final String message;
    public final String id;
    
    public ChatMessage(String i, String m) {
        this.id = i;
        this.message = m;
    }
}