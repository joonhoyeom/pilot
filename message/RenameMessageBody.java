package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by joonho on 2017-04-06.
 */
public class RenameMessageBody extends MessageBody{

    private Charset charset = Charset.forName("UTF-8");

    private String targ;
    private String newName;

    public RenameMessageBody(String targ, String newName){ this.targ = targ; this.newName = newName; }

    @Override
    public int getSerializedSize() {
        return this.serialize().length;
    }

    @Override
    public byte[] serialize() {
        return charset.encode(targ + "\n" + newName).array();
    }

    @Override
    public MessageBody deserialize(byte[] serializedData) {

        String []strs = charset.decode(ByteBuffer.wrap(serializedData)).toString().split("\n");

        if(strs.length < 2)
            return null;

        return new RenameMessageBody( strs[0], strs[1] );
    }
}
