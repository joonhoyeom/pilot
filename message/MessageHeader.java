package message;


import java.nio.ByteBuffer;

public class MessageHeader {
	public final static byte[] messageStart = {(byte) 0xF9, (byte) 0xBE, (byte) 0xB4, (byte) 0xD9 };
	public final static int serializedSize = 12;
	private int command;
	private int messageBodySize;
	
	public MessageHeader(int command, int messageBodySize){
		if(Command.isValidCommand(command) == false){
			command = -1; // INVALID COMMAND
			messageBodySize = 0;
		}
		else{
			this.command = command;
			this.messageBodySize = messageBodySize;
		}
	}
	
	public MessageHeader(byte [] bytes, int offset){
		
		//Invalid MessageHeader size.
		if(bytes.length < serializedSize){
			command = -1;
			return;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(serializedSize);
		buffer.put(bytes, offset, serializedSize);
		command = buffer.getInt(4);
		messageBodySize = buffer.getInt(8);
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
		buffer.put(messageStart);
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