package message;

public class Command {

	final public static int DIR 			= 0;
	final public static int RENAME 		= 1;
	final public static int MOVE 		= 2;
	final public static int COPY 		= 3;
	final public static int PASTE 		= 4;

	final public static int DELETE 		= 5;
	final public static int DIRRES 		= 6;
	final public static int FILEUP 		= 7;
	final public static int FILEDOWN 	= 8;


	final public static int LASTCOMMAND = FILEDOWN;

	public static boolean isValidCommand(int command){ return command >= 0 && command <= LASTCOMMAND ? true : false;}
}
