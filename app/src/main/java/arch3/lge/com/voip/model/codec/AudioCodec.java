package arch3.lge.com.voip.model.codec;

public abstract class AudioCodec {
    public abstract boolean open();

    public abstract boolean decode(byte encoded[], byte lin[]);

    public abstract boolean encode(byte lin[], byte encoded[]);

    public abstract boolean close();
}
