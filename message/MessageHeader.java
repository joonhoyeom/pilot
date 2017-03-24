package message;

import java.io.Serializable;

public class MessageHeader implements Serializable{
	public Command command;
	public int messageLen;
}
