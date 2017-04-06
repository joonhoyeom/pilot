package message;
/**
 * No MessageBody size exeeds 8k
 * TODO DirResMessage break policy
 * */
public abstract class MessageBody {
	
	/**
	 * process message and return response MessageBody
	 * if there are no response message, return null
	 * */
	abstract public int getSerializedSize();
	abstract public byte[] serialize();
	abstract public MessageBody deserialize(byte [] serializedData);
}

