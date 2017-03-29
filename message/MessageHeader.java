package message;


import java.nio.ByteBuffer;

public class MessageHeader {
	public final static String messageStart = "%start";
	public final static int serializedSize = 14;
	private int command;
	private int messageBodySize;
	
	public MessageHeader(int command, int messageBodySize){
		if(command > Command.LASTCOMMAND || command < 0){
			command = -1; // INVALID COMMAND
			messageBodySize = 0;
		}
		else{
			this.command = command;
			this.messageBodySize = messageBodySize;
		}
	}
	
	public MessageHeader(byte [] bytes){
		
		//Invalid MessageHeader size.
		if(bytes.length < serializedSize){
			command = -1;
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(serializedSize);
		buffer.put(bytes);
		command = buffer.getInt(6);
		messageBodySize = buffer.getInt(10);
	}
	
	public int getMessageBodySize(){
		return messageBodySize;
	}
	
	public void setMessageBodySize(int messageBodySize){
		this.messageBodySize = messageBodySize;
	}
	
	public int getCommand() { return command; }
	
	public byte [] getBytes(){
		ByteBuffer buffer = ByteBuffer.allocate(serializedSize);
		buffer.put(messageStart.getBytes());
		buffer.putInt(command);
		buffer.putInt(messageBodySize);
		return buffer.array();
	}
		
	public boolean isValid(){
		if(command >= 0 && command <= Command.LASTCOMMAND )
			return true;
		else
			return false;
	}
	
	@Override
	public String toString() {
		return "MessageHeader [messageStart=" + messageStart + ", command=" + command + ", messageBodySize="
				+ messageBodySize + "]";
	}
}