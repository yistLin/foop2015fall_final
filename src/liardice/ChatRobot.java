package liardice;

import liardice.message.ChatMessage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Date;

public class ChatRobot {

    public static final int GET_DICE = 0;
    public static final int ROUND_START = 1;
    public static final int DO_BID = 2;
    public static final int DO_CATCH = 3;
    public static final int NO_CATCH = 4;
    public static final int YES_CATCH = 5;
    public static final int ROUND_END = 6;
    public static final int OTHER_TALK = 7;

    public final String NICKNAME;

   	private final String dir_path = "./config";
   	private File dir_node = new File(dir_path);
   	private String[] filenames = dir_node.list();

   	public static void main(String args[]) {
   		int index = Integer.parseInt(args[0]);
   		new ChatRobot(index);
   	}

    public ChatRobot(int index) {
    	long epoch = System.currentTimeMillis();
    	long seed = epoch * (long)index;
    	Random rnd = new Random(seed);
    	int nameIndex = rnd.nextInt(filenames.length);
    	NICKNAME = filenames[nameIndex];
    }

    // GET_DICE
    public String talk(int status, Dice[] dice) {
    	return null;
    }

    // ROUND_START, DO_BID, DO_CATCH, NO_CATCH, YES_CATCH
    public String talk(int status) {
    	return null;
    }

    // ROUND_END
    public String talk(int status, boolean imLoser) {
    	return null;
    }

    // OTHER_TALK
    public String talk(int status, String chat) {
    	return null;
    }
}