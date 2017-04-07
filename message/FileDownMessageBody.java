package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by joonho on 2017-04-07.
 */
public class FileDownMessageBody extends MessageBody {

    private String targPath;
    private Charset charset = Charset.forName("UTF-8");

    public FileDownMessageBody(String path){ this.targPath = path;}

    @Override
    public int getSerializedSize() {
        return this.serialize().length;
    }

    @Override
    public byte[] serialize() {
        if(targPath == null)
            targPath = "";
        return charset.encode(targPath).array();
    }

    @Override
    public MessageBody deserialize(byte[] serializedData) {
        return new FileDownMessageBody( charset.decode(ByteBuffer.wrap(serializedData)).toString() );
    }
}
