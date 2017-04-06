package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by joonho on 2017-04-06.
 */
public class DeleteMessageBody extends MessageBody{

    private Charset charset = Charset.forName("UTF-8");

    private String targ;

    public DeleteMessageBody(String targ){ this.targ = targ;}

    @Override
    public int getSerializedSize() {
        return this.serialize().length;
    }

    @Override
    public byte[] serialize() {
        return charset.encode(targ).array();
    }

    @Override
    public MessageBody deserialize(byte[] serializedData) {
        return new DeleteMessageBody( charset.decode(ByteBuffer.wrap(serializedData)).toString() );
    }
}
