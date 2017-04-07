package messageResponder;

import message.Command;
import message.MessageHeader;

public abstract class MessageResponder {

	public static MessageResponder newMessageResponder(MessageHeader mh) {
		int command = mh.getCommand();

		switch (command) {
			
			case Command.DIRRES:
				return new DirResResponder();
			
			case Command.FILEUP:
				return new FileUpResponder();

			default:
				return null;
		}
	}

	public abstract Object respond(Object messageBody);
}

