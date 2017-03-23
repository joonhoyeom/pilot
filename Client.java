import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class Client {
	private SocketChannel socketChannel;
	private String ip;
	
	private ByteBuffer recvBuffer;
	public Object recvBufferMutex;
	
	private ByteBuffer sendBuffer;
	public Object sendBufferMutex;
	
	private final int BUFFERSIZE = 0x2000; // typical socket buffer size 8k-16k

	public Client(String ip, SocketChannel socketChannel) {
		this.ip = ip;
		this.socketChannel = socketChannel;
		recvBuffer = ByteBuffer.allocate(BUFFERSIZE);
		sendBuffer = ByteBuffer.allocate(BUFFERSIZE);	
	}

	public String getIP() {
		return ip;
	}

	public SocketChannel getSocketChannel(){ return socketChannel; }
	
	int readFromSocket() throws IOException{
		
		//TODO set read size
		int readCount = socketChannel.read(recvBuffer);
		
		return readCount;
	}
	
	int writeToSocket() throws IOException{
		
		int writeCount = socketChannel.write(sendBuffer);
		
		return writeCount;
	}
	
	ByteBuffer getRecvBuffer(){ return recvBuffer ; }
	ByteBuffer getSendBuffer(){ return sendBuffer ; }
			
	void closeSocketChannel() throws IOException{ 
		if(socketChannel.isOpen()) 
			socketChannel.close();
	}	
}
