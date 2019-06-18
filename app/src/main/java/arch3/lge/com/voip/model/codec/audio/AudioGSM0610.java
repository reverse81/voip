package arch3.lge.com.voip.model.codec.audio;

import arch3.lge.com.voip.model.codec.AudioCodec;

public class AudioGSM0610 extends AudioCodec {
    static {
        System.loadLibrary("native-gsm0610-lib");
    }
    @Override
    public boolean open(){
        int ret = JniGsmOpen();
        return(ret == 0);
    }

    @Override
    public boolean decode(byte encoded[], byte lin[]){
        JniGsmDecodeB(encoded, lin);
        return true;
    }

    @Override
    public boolean encode(byte lin[], byte encoded[]){
        JniGsmEncodeB(lin, encoded);
        return true;
    }

    @Override
    public boolean close(){
        JniGsmClose();
        return true;
    }

    public static native int JniGsmOpen();
    // Not Used uncomment to enable
    //public static native int JniGsmDecode(byte encoded[], short lin[]);
    // Not Used uncomment to enable
    //public static native int JniGsmEncode(short lin[], byte encoded[]);

    public static native int JniGsmDecodeB(byte encoded[], byte lin[]);

    public static native int JniGsmEncodeB(byte lin[], byte encoded[]);

    public static native void JniGsmClose();
}
