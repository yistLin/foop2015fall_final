package liardice;

import liardice.message.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;
import java.io.*;

public class ChatRobot {

    public static final int GET_DICE = 0;
    public static final int ROUND_START = 1;
    public static final int DO_BID = 2;
    public static final int DO_CATCH = 3;
    public static final int NO_CATCH = 4;
    public static final int YES_CATCH = 5;
    public static final int ROUND_END = 6;
    public static final int OTHER_TALK = 7;
    public static final int RANDOM_TALK = 8;

    public final String NICKNAME;

    private Random rand;
   	private JSONObject jsonObj;

   	public static void main(String args[]) {
   		int index = Integer.parseInt(args[0]);
   		new ChatRobot(index);
   	}

    public ChatRobot(int index) {
    	long epoch = System.currentTimeMillis();
    	long seed = epoch * (long)index;
    	rand = new Random(seed);
    	String dir_path = "./config";
   		File dir_node = new File(dir_path);
   		String[] filenames = dir_node.list();
    	int nameIndex = rand.nextInt(filenames.length);
    	String filename = filenames[nameIndex];

    	StringBuilder sb = new StringBuilder();
    	FileReader in = null;
    	try {
    		in = new FileReader(dir_path + "/" + filename);
    	} catch (FileNotFoundException e) {}
    	BufferedReader br = new BufferedReader(in);
    	String line;

    	try {
	    	while ((line = br.readLine()) != null) {
	    		sb.append(line);
	    	}
	    } catch (IOException e) {}

   		jsonObj = new JSONObject(sb.toString());
   		NICKNAME = jsonObj.getString("name");
    }

    // GET_DICE
    public String talk(int status, Dice[] dice) {
    	return null;
    }

    // ROUND_START, DO_BID, DO_CATCH, NO_CATCH, YES_CATCH, RANDOM_TALK
    public String talk(int status) {
    	switch (status) {
    		case RANDOM_TALK:
    			JSONArray talkArray = jsonObj.getJSONArray("random_talk");
    			int randIndex = rand.nextInt(talkArray.length());
    			JSONObject talkObj = talkArray.getJSONObject(randIndex);
    			return talkObj.getString("message");
    		default:
    			break;
    	}
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