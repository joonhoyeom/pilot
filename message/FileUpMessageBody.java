package message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Serialized Format
 *   pathLength / filePath / byte
 *
 * if byte section is null, then consider it as eof
 */
public class FileUpMessageBody extends MessageBody {
    private int pathLength;
    private String filePath;
    private byte [] data;
    private static final Charset charset = Charset.forName("UTF-8");

    public FileUpMessageBody(String filePath, byte[] data){
        this.filePath = filePath;
        this.data = data;
        this.pathLength = charset.encode(filePath).array().length;
    }

    @Override
    public int getSerializedSize() {
        return 4 + pathLength + (data == null ? 0 : data.length);
    }

    @Override
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + pathLength + (data == null ? 0 : data.length));
        buffer.putInt(pathLength);
        buffer.put(charset.encode(filePath).array());
        if(data != null)
            buffer.put(data);

        return buffer.array();
    }

    @Override
    public MessageBody deserialize(byte[] serializedData) {

        ByteBuffer buffer = ByteBuffer.allocate(serializedData.length);
        buffer.put(serializedData);

        buffer.flip();
        int len = buffer.getInt();
        byte [] arrPath = new byte[len];
        buffer.get(arrPath, 0, len);

        byte [] _data = null;
        if(len + 4 < serializedData.length){
            _data = new byte[serializedData.length - 4 - len];
            buffer.get(_data);
        }
        String _filePath = charset.decode(ByteBuffer.wrap(arrPath)).toString();

        return new FileUpMessageBody(_filePath, _data);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
