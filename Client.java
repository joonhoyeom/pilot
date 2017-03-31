import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import message.Command;
import message.MessageHeader;

public class Client {
	private SocketChannel socketChannel;
	private String ip;
	

	public ByteBuffer recvBuffer;
	public Object recvBufferMutex;
	
	public ByteBuffer sendBuffer;
	public Object sendBufferMutex;
	
	private final int BUFFERSIZE = 0x2000; // typical socket buffer size 8k-16k

	public Client(String ip, SocketChannel socketChannel) {
		this.ip = ip;
		this.socketChannel = socketChannel;

		recvBuffer = ByteBuffer.allocate(BUFFERSIZE);
		sendBuffer = ByteBuffer.allocate(BUFFERSIZE);
		
		recvBufferMutex = new Object();
		sendBufferMutex = new Object();

	}

	public String getIP() {
		return ip;
	}

	public int readFromSocket() throws IOException{
		
		//TODO set read size
		int readCount = 0;		
		readCount = socketChannel.read(recvBuffer);	
		
		return readCount;
	}
	
	public int writeToSocket() throws IOException{
		
		int writeCount = 0;
		writeCount = socketChannel.write(sendBuffer);
		sendBuffer.clear();
		return writeCount;
	}
	
	public boolean pushMessage(int command, byte[] messageBody){
		if(Command.isValidCommand(command) == false)
			return false;
		
		MessageHeader header = new MessageHeader(command, messageBody.length);
		
		int pushPos = sendBuffer.position();		
		try{
			sendBuffer.put(header.getBytes());
			sendBuffer.put(messageBody);
		} catch(BufferOverflowException e){
			sendBuffer.position(pushPos); //eliminate pushed message
			System.err.println( ip +" : send buffer overflow");
			return false;
		}
		return true;
	}
			
	public void closeSocketChannel() throws IOException{ 
		if(socketChannel.isOpen()) 
			socketChannel.close();
	}

}
