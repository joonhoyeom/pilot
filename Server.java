import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {

	final static int PORT = 9999;
	
	public static void main(String[] args) {
		
		List<Client> clientList = new ArrayList<Client>();
		
		
		
		
		Selector selector;
		ServerSocketChannel serverSocketChannel = null;
		
		// socket initialize
		// register server socket to selector
		try {
			selector = Selector.open();

			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.bind(new InetSocketAddress(PORT));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// multiplexing request
		while (true) {
			try {
				selector.select();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();

			// Handle selected sockets
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						
						SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
						
						Client client = new Client( socketChannel.getRemoteAddress().toString().split(":")[0], socketChannel);
						clientList.add(client);
						
						socketChannel.configureBlocking(false);
						SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
						selectionKey.attach(client);
						
						System.out.println("[" + client.getIP() + "]connected");
						
					} else if (key.isReadable()) {
						Client client = (Client)key.attachment();
						int recvCount = client.receive();
						if(recvCount == -1){
							clientList.remove(client);
							client.closeChannel();
							System.out.println("[" + client.getIP() +"] : closed" );
						}
					} else if (key.isWritable()) {
						Client client = (Client)key.attachment();
						client.send();
						
					} else {
						System.out.println("Unknown Key Behavior");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (serverSocketChannel.isOpen()) {
			try {
				serverSocketChannel.close();
			} catch (IOException e) {
				System.err.println("Socket close fail");
			}
		}
		//

		// attaching web server
	}
}
