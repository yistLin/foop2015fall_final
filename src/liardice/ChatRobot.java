package liardice;

import liardice.message.ChatMessage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.Date;

public class ChatRobot {

    public static final int demo = 0;

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
}