package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DirMessageBody extends MessageBody {

	private String path;
	private Charset charset = Charset.forName("UTF-8");

	public DirMessageBody(String path){ this.path = path;}

	@Override
	public int getSerializedSize() {
		return this.serialize().length;
	}

	@Override
	public byte[] serialize() {
		if(path == null)
			path = "";
		return charset.encode(path).array();
	}

	@Override
	public MessageBody deserialize(byte[] serializedData) {
		return new DirMessageBody( charset.decode(ByteBuffer.wrap(serializedData)).toString() );
	}
}
