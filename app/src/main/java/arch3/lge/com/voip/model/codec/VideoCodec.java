package arch3.lge.com.voip.model.codec;

import android.graphics.Bitmap;

public abstract class VideoCodec {

    public abstract boolean open();

    public abstract byte [] encode(byte[] data, int format, int width, int height, int  mode);

    public abstract Bitmap decode(byte[] data, int offset, int length);

    public Bitmap decode(byte[] data){
        return decode(data, 0, data.length);
    }

    public abstract boolean close();
}
