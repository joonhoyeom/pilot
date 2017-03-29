import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import message.MessageHeader;

public class Commnicator {
	
	//shared variable
	private List<Client> clientList;
	private boolean stop = false;
	
	
	private SocketHandler socketHandler;
	private MessageHandler messageHandler;
		
	class SocketHandler extends Thread{
		
		final private int PORT = 9999;
	
		private Selector selector;
		private ServerSocketChannel serverSocketChannel = null;
	
		public SocketHandler() {
			
			//Selector and server socket initialize
			
			try {
				selector = Selector.open();

				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.bind(new InetSocketAddress(PORT));
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		@Override
		public void run() {
			
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
							
							Client client = new Client( socketChannel.getRemoteAddress().toString().substring(1).split(":")[0], socketChannel);
							clientList.add(client);
							
							socketChannel.configureBlocking(false);
							SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
							selectionKey.attach(client);
							
							System.out.println("[" + client.getIP() + "]connected");
							
						} else if (key.isReadable()) {
							Client client = (Client)key.attachment();
							
							int recvCount = 0;							
							recvCount= client.readFromSocket();
							if(recvCount == -1){
								clientList.remove(client);
								client.closeSocketChannel();
								System.out.println("[" + client.getIP() +"] : closed" );
							}else
								key.interestOps(SelectionKey.OP_WRITE);
							
						} else if (key.isWritable()) {
							Client client = (Client)key.attachment();
							
							int writeCount = 0;
							writeCount = client.writeToSocket();
							
							key.interestOps(SelectionKey.OP_READ);							
						} else {
							System.out.println("Unknown Key Behavior");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				selector.wakeup();
			}

			if (serverSocketChannel.isOpen()) {
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					System.err.println("Socket close fail");
				}
			}
			//
		}
	}
	
	class MessageHandler extends Thread{
	
		private void processMessages(Client client) {
		
			synchronized(client.recvBufferMutex){
				while(client.getRecvBuffer().hasRemaining()){
					
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(client.getRecvBuffer().array());
					ObjectInputStream objectInputStream = null;
					
					try {
						objectInputStream = new ObjectInputStream(byteArrayInputStream);
						MessageHeader messageHeader = (MessageHeader)objectInputStream.readObject(); // if full MessageHeader doesn't arrive? 
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}

		@Override
		public void run() {
			while(true){				
				for(Client client : clientList){
					
					boolean remain = false;
					synchronized (client.recvBufferMutex) {
						remain = client.getRecvBuffer().hasRemaining();
					}					
					if(remain){
						processMessages(client);
					}						
					
				}
			}
		}
	}	
		
	public Commnicator(){
		clientList = new Vector<Client>();
		socketHandler = new SocketHandler();
		messageHandler = new MessageHandler();
	}
	
	public void communicatorRun() {		
		socketHandler.start();
		messageHandler.start();
	}

	public void setStop() {
		stop = true;
	}
	
	public boolean isAlive(){
		return ( socketHandler != null && socketHandler.isAlive() ) || ( messageHandler != null && messageHandler.isAlive() );
	}
	
	public void interrupt(){
		
		if(socketHandler != null)
			socketHandler.interrupt();
		
		if(messageHandler != null)
			messageHandler.interrupt();	
	
	}
	
	public void join() throws InterruptedException {
		
		if(socketHandler != null)
			socketHandler.join();
			
		if(messageHandler != null)
			messageHandler.join();		
	
	}
}
