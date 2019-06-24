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
    public boolean close(){
        JniGsmClose();
        return true;
    }

    @Override
    public byte[] encode(byte data[], int offset, int size){

        return JniGsmEncodeB(data, offset, size);
    }

    @Override
    public byte[] decode(byte data[], int offset, int size){
        return JniGsmDecodeB(data, offset, size);
    }

    @Override
    public int  getFrameLength(){
        return 320;
    }


    public static native int JniGsmOpen();

    public static native void JniGsmClose();

    public static native byte[] JniGsmDecodeB(byte data[], int offset, int size);

    public static native byte[] JniGsmEncodeB(byte data[], int offset, int size);
}
