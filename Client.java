import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
	private SocketChannel socketChannel;
	private String ip;
	private ByteBuffer buffer;
	private final int BUFFERSIZE = 0x2000; // typical socket buffer size 8k-16k

	public Client(String ip, SocketChannel socketChannel) {
		this.ip = ip;
		this.socketChannel = socketChannel;
		buffer = ByteBuffer.allocate(BUFFERSIZE);
	}

	public String getIP() {
		return ip;
	}

	int receive() {
		int readCount = 0;
		try {
			buffer.clear();
			readCount = socketChannel.read(buffer);

			// server closes socket.
			if (readCount == -1)
				return -1;

			buffer.flip();
			System.out.println("recv data : " + buffer.toString());

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return readCount;
	}

	int send() {
		int sendCount = 0;
		try {
			socketChannel.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sendCount;
	}

	void closeChannel() {
		try {
			if (socketChannel.isOpen())
				socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
