import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import message.MessageHeader;
import messageResponder.MessageResponder;

public class Communicator {
	
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
							synchronized (client.recvBufferMutex) {
								recvCount= client.readFromSocket();
							}
							if(recvCount == -1){
								clientList.remove(client);
								key.cancel();
								client.closeSocketChannel();
								System.out.println("[" + client.getIP() +"] : closed" );
							}else
								key.interestOps(SelectionKey.OP_WRITE);
														
						} else if (key.isWritable()) {
							Client client = (Client)key.attachment();
							
							int writeCount = 0;
							synchronized (client.sendBufferMutex) {
								writeCount = client.writeToSocket();
							}							
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
		
		//parse recvBuffer into messages
		private void processMessages(Client client){
		
			String recvBufferString = null;
			
			//fetch messages from recvBuffer
			synchronized (client.recvBufferMutex) {
				
				if(client.recvBuffer.hasRemaining()){
					
					//buffer -> string and clear buffer
					Charset charset = Charset.forName("UTF-8");
					recvBufferString = charset.decode(client.recvBuffer).toString();
					client.recvBuffer.clear();
					
					int lastHeaderPos = recvBufferString.lastIndexOf(MessageHeader.messageStart);
					
					//last header is not arrived yet
					if(recvBufferString.substring(lastHeaderPos).length() < MessageHeader.serializedSize){
						//put last header into buffer back 
						client.recvBuffer.put(recvBufferString.substring(lastHeaderPos).getBytes());
						//fetch valid messages
						recvBufferString = recvBufferString.substring(0, lastHeaderPos);
					} else {
						MessageHeader lastHeader = new MessageHeader( (recvBufferString.substring(lastHeaderPos, lastHeaderPos+ MessageHeader.serializedSize)).getBytes() );
						int lastMessageEnd = lastHeaderPos + MessageHeader.serializedSize + lastHeader.getMessageBodySize();
						
						//put unanimous data back
						client.recvBuffer.put(recvBufferString.substring(lastMessageEnd + 1).getBytes() );
						//fetch valid messages
						recvBufferString = recvBufferString.substring(0, lastMessageEnd);
					}
				}
				else //there are no messages
					return;
			}
			
			//extract MessageHeader and Body , 
			if(recvBufferString != null){
				for (int headerPos = recvBufferString.indexOf(MessageHeader.messageStart) ; headerPos < recvBufferString.length(); ) {
					
					MessageHeader messageHeader = new MessageHeader(
							recvBufferString.substring(headerPos, headerPos + MessageHeader.serializedSize).getBytes());
					
					int messageBodyStart = headerPos + MessageHeader.serializedSize;
					int messageBodyEnd   = headerPos + MessageHeader.serializedSize + messageHeader.getMessageBodySize();
					
					String messageBody = recvBufferString.substring(messageBodyStart , messageBodyEnd);
					
					headerPos = messageBodyEnd;
					MessageResponder mr = MessageResponder.newMessageResponder(messageHeader);
					if(mr != null){
						mr.respond(messageBody);
					} else {
						System.err.println("Invalid message header");
					}
				}		
			}	
		}
				
		
		@Override
		public void run() {
			while(true){				
				for(Client client : clientList){
					processMessages(client);	
				}
				if(stop)
					break;
			}
		}
	}	
		
	public Communicator(){
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
