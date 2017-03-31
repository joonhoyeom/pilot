package messageResponder;

import message.Command;
import message.MessageHeader;

public abstract class MessageResponder {

	public static MessageResponder newMessageResponder(MessageHeader mh) {
		int command = mh.getCommand();

		switch (command) {
			
			case Command.DIRRES:
				return new DirResResponder();
			
			case Command.FILE:
				return new FileResponder();

			case Command.FILERES:
				return new FileresResponder();
				
			default:
				return null;
		}
	}

	public abstract Object respond(Object messageBody);
}

