package message;

import java.io.Serializable;

public abstract class Message implements Serializable{
	
	/**
	 * process message and return response Message
	 * if there are no response message, return null
	 * */
	abstract int getMessageLen();
	abstract Object respond();
}

