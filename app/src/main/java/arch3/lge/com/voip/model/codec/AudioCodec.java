package arch3.lge.com.voip.model.codec;

public abstract class AudioCodec {
    public abstract boolean open();
    public abstract boolean close();
    public abstract byte[] encode(byte data[], int offset, int size);
    public abstract byte[] decode(byte data[], int offset, int size);
}
