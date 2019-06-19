package arch3.lge.com.voip.model.codec.audio;

import arch3.lge.com.voip.model.codec.AudioCodec;

public class AudioG729b extends AudioCodec {
    static {
        System.loadLibrary("native-g729-enc");
        System.loadLibrary("native-g729-dec");
    }

    @Override
    public boolean open(){
        JniG729EncodeInit();
        JniG729DecodeInit();
        return true;
    }

    @Override
    public boolean close(){
        return true;
    }

    @Override
    public byte[] encode(byte data[], int offset, int size){
        return JniG729Encode(data, offset, size);
    }

    @Override
    public byte[] decode(byte data[], int offset, int size){
        return JniG729Decode(data, offset, size);
    }


    public static native boolean JniG729DecodeInit();
    public static native byte [] JniG729Decode(byte data[], int offset, int size);

    public static native boolean JniG729EncodeInit();
    public static native byte [] JniG729Encode(byte data[], int offset, int size);
}
