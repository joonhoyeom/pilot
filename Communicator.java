import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import message.Command;
import message.MessageHeader;
import messageResponder.MessageResponder;
import utils.Utils;

public class Communicator {
	
	//shared variable
	private List<Client> clientList;
	private Object clientListMutex; 
	private boolean stop = false;
		
	private SocketHandler socketHandler;
	private MessageHandler messageHandler;
		
	class SocketHandler extends Thread{
		
		final private int PORT = 9998;
	
		private Selector selector;
		private ServerSocketChannel serverSocketChannel = null;
	
		public SocketHandler() {
			
			//Selector and server socket initialize
			
			try {
				selector = Selector.open();

				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
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
			while (!stop) {
				int nSelectedKey = 0;
				try {
					nSelectedKey = selector.select();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}

				if(nSelectedKey > 0){
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
								client.pushMessage(Command.DIR, "".getBytes());
								
								synchronized (clientListMutex) {
									clientList.add(client);
								}
								
							
								socketChannel.configureBlocking(false);
								SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
								selectionKey.attach(client);
														
								System.out.println("[" + client.getIP() + "]connected");
				
							
							} else if (key.isReadable()) {
//								System.out.println("###DEBUG key : READABLE");
							
								Client client = (Client)key.attachment();
							
								int recvCount = 0;							
								synchronized (client.recvBufferMutex) {
									recvCount= client.readFromSocket();
									if(recvCount == 0)
										key.interestOps(SelectionKey.OP_WRITE);
									else{
										System.out.println(recvCount + " : "+ new String(client.recvBuffer.array()));
								
									}
								}
								if(recvCount == -1){
									synchronized (clientListMutex) {
										clientList.remove(client);	
									}
									key.cancel();
									client.closeSocketChannel();
									System.out.println("[" + client.getIP() +"] : closed" );
								}							
							} else if (key.isWritable()) {
//								System.out.println("###DEBUG key : WRITABLE");
								Client client = (Client)key.attachment();
							
								int writeCount = 0;
								synchronized (client.sendBufferMutex) {
									if(client.sendBuffer.position() > 0){
										client.sendBuffer.flip();
										writeCount = client.writeToSocket();
									}
								}
								//Temporal code!!
								key.interestOps(SelectionKey.OP_READ);
							} else {
								System.out.println("Unknown Key Behavior");
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
		}
	}
	
	class MessageHandler extends Thread{
		
		//parse recvBuffer into messages
		private void processMessages(Client client){
		
			byte[] recvBufferCopy = null;
			ByteBuffer recvBuffer = client.recvBuffer;
	
			//fetch messages from recvBuffer
			synchronized (client.recvBufferMutex) {
								
				if(recvBuffer.position() > 0){
					recvBufferCopy = new byte[recvBuffer.position()];
					recvBuffer.flip();
					System.arraycopy(recvBuffer.array(), 0, recvBufferCopy, 0, recvBufferCopy.length);
					recvBuffer.clear();
					
					int i = 0;
					while(true){
						int headerPos = Utils.indexOf(recvBufferCopy, MessageHeader.messageStart, i);
						
						if(headerPos == -1){
							break;
						}
						
						//Header is not arrived yet
						if (recvBufferCopy.length - headerPos < MessageHeader.serializedSize) {
							// put last header into buffer back
							recvBuffer.put(recvBufferCopy, headerPos, recvBufferCopy.length - headerPos);
							break;
						}
						//Unserialize MessageHeader
						MessageHeader header = new MessageHeader(recvBufferCopy, headerPos);
						int messageBodyStart = headerPos + MessageHeader.serializedSize;
						int messageBodyEnd = headerPos + MessageHeader.serializedSize + header.getMessageBodySize();
						
						//MessageBody is not arrived yet
						if(messageBodyEnd > recvBufferCopy.length){
							recvBuffer.put(recvBufferCopy, headerPos, recvBufferCopy.length - headerPos);
							break;
						}
						
						byte []messageBody = new byte [header.getMessageBodySize()];
						
						System.arraycopy(recvBufferCopy, messageBodyStart, messageBody, 0, header.getMessageBodySize());
						
						//Process Message
						//Incomplete code
						{
							MessageResponder mr = MessageResponder.newMessageResponder(header);
							if(mr != null){
								mr.respond(messageBody);
							} else {
								System.err.println("Invalid message header");
							}							
						}
						i = messageBodyEnd;
					}
				}
				else //there are no messages
					return;
			}
				
		}
				
		
		@Override
		public void run() {
			while( !stop ){
				synchronized (clientListMutex) {
					for(Client client : clientList){
						processMessages(client);	
					}	
				}
				
			}
		}
	}	
		
	public Communicator(){
		clientList = new Vector<Client>();
		clientListMutex = new Object();
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
