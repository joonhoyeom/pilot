import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

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
		recvBuffer = ByteBuffer.allocateDirect(BUFFERSIZE).order(ByteOrder.nativeOrder());
		sendBuffer = ByteBuffer.allocateDirect(BUFFERSIZE).order(ByteOrder.nativeOrder());
		
		recvBufferMutex = new Object();
		sendBufferMutex = new Object();
	}

	public String getIP() {
		return ip;
	}

	public SocketChannel getSocketChannel(){ return socketChannel; }
	
	public int readFromSocket() throws IOException{
		
		//TODO set read size
		int readCount = 0;		
		readCount = socketChannel.read(recvBuffer);	
		
		return readCount;
	}
	
	public int writeToSocket() throws IOException{
		
		int writeCount = 0;
		writeCount = socketChannel.write(sendBuffer);
		
		return writeCount;
	}	
			
	void closeSocketChannel() throws IOException{ 
		if(socketChannel.isOpen()) 
			socketChannel.close();
	}

}
