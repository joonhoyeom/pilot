package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by joonho on 2017-04-06.
 */
public class CopyMessageBody extends MessageBody{

    private Charset charset = Charset.forName("UTF-8");

    private String src;
    private String dest;

    public CopyMessageBody(String src, String dest){ this.src = src; this.dest = dest; }

    @Override
    public int getSerializedSize() {
        return this.serialize().length;
    }

    @Override
    public byte[] serialize() {
        return charset.encode(src + "\n" + dest).array();
    }

    @Override
    public MessageBody deserialize(byte[] serializedData) {

        String []strs = charset.decode(ByteBuffer.wrap(serializedData)).toString().split("\n");

        if(strs.length < 2)
            return new RenameMessageBody("","");

        return new RenameMessageBody( strs[0], strs[1] );
    }
}
