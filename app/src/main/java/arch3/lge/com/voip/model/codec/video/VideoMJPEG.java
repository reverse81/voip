package arch3.lge.com.voip.model.codec.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

import arch3.lge.com.voip.model.codec.VideoCodec;

public class VideoMJPEG extends VideoCodec {

    @Override
    public boolean open(){
        return true;
    }

    @Override
    public byte [] encode(byte[] data, int format, int width, int height){
        // Get the YuV image
        YuvImage yuv_image = new YuvImage(data, format, width, height, null);

        // Convert YuV to Jpeg
        Rect rect = new Rect(0, 0, width, height);
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
        yuv_image.compressToJpeg(rect, 40, output_stream);
        return output_stream.toByteArray();
    }

    @Override
    public Bitmap decode(byte[] data, int offset, int length){
        return BitmapFactory.decodeByteArray(data, offset, length);
    }

    @Override
    public boolean close(){
        return true;
    }
}
