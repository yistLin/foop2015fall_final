package liardice;

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
    public final double ENTROPY;

    private Random rand;
   	private JSONObject jsonObj;

    public ChatRobot(String filename) {
    	long epoch = System.currentTimeMillis();
    	long seed = epoch * (long)(filename.length());
    	rand = new Random(seed);

    	StringBuilder sb = new StringBuilder();
      BufferedReader br = null;
      try {
      	br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
      } catch(FileNotFoundException e) {
      } catch(UnsupportedEncodingException e) {
      }
    	String line;

    	try {
	    	while ((line = br.readLine()) != null) {
	    		sb.append(line);
	    	}
	    } catch (IOException e) {}

   		jsonObj = new JSONObject(sb.toString());
   		NICKNAME = jsonObj.getString("name");
        ENTROPY = Double.parseDouble(jsonObj.getString("entropy"));
    }

    // ROUND_START, DO_CATCH, NO_CATCH, YES_CATCH, RANDOM_TALK, GET_DICE
    public String talk(int status) {
    	String objName = "";
    	switch (status) {
    		case ROUND_START:	objName = "round_start"; break;
    		case RANDOM_TALK:	objName = "random_talk"; break;
    		case DO_CATCH:		objName = "do_catch"; break;
    		case NO_CATCH:		objName = "no_catch"; break;
    		case YES_CATCH:		objName = "yes_catch"; break;
    		case GET_DICE:		objName = "get_dice"; break;
    		default: break;
    	}
    	JSONArray talkArray = jsonObj.getJSONArray(objName);
		int randIndex = rand.nextInt(talkArray.length());
		JSONObject talkObj = talkArray.getJSONObject(randIndex);
		double probability = Double.parseDouble(talkObj.getString("probability"));
		return (Math.random() < probability) ? talkObj.getString("message") : null;
    }

    // ROUND_END
    public String talk(int status, boolean imLoser) {
    	JSONObject obj = jsonObj.getJSONObject("round_end");
    	String state = (imLoser) ? "lose" : "win";
    	JSONArray talkArray = obj.getJSONArray(state);
		int randIndex = rand.nextInt(talkArray.length());
		JSONObject talkObj = talkArray.getJSONObject(randIndex);
		double probability = Double.parseDouble(talkObj.getString("probability"));
		return (Math.random() < probability) ? talkObj.getString("message") : null;
    }

    // DO_BID, OTHER_TALK
    public String talk(int status, String message) {
    	if (status == DO_BID) {
	    	JSONObject obj = jsonObj.getJSONObject("do_bid");
	    	JSONArray talkArray = obj.getJSONArray(message);
			int randIndex = rand.nextInt(talkArray.length());
			JSONObject talkObj = talkArray.getJSONObject(randIndex);
			double probability = Double.parseDouble(talkObj.getString("probability"));
			return (Math.random() < probability) ? talkObj.getString("message") : null;
		}
		else if (status == DO_CATCH) {
			JSONObject obj = jsonObj.getJSONObject("do_catch");
	    	JSONArray talkArray = obj.getJSONArray(message);
			int randIndex = rand.nextInt(talkArray.length());
			JSONObject talkObj = talkArray.getJSONObject(randIndex);
			double probability = Double.parseDouble(talkObj.getString("probability"));
			return (Math.random() < probability) ? talkObj.getString("message") : null;
		}
		else {
			return null;
		}
    }
}
