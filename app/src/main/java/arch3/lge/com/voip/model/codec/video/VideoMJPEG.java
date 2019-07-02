package arch3.lge.com.voip.model.codec.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

import arch3.lge.com.voip.model.codec.VideoCodec;

public class VideoMJPEG extends VideoCodec {

    @Override
    public boolean open(){
        return true;
    }

//    private Bitmap rotateBitmap(YuvImage yuvImage, int orientation, Rect rectangle)
//    {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(rectangle, 100, os);
//
//        Matrix matrix = new Matrix();
//        matrix.postRotate(orientation);
//        matrix.postScale((float)0.5, (float)0.5);
//        byte[] bytes = os.toByteArray();
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//        return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }


    @Override
    public byte [] encode(byte[] data, int format, int width, int height, int mode){
        // Get the YuV image
        YuvImage yuv_image = new YuvImage(data, format, width, height, null);

        // Convert YuV to Jpeg
        Rect rect = new Rect(0, 0, width, height);
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();

        yuv_image.compressToJpeg(rect, 100, output_stream);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        if (mode ==1 ) {
            matrix.postScale((float) 0.5, (float) 0.5);
        } else if (mode == 2) {
            matrix.postScale((float) 0.2, (float) 0.2);
        } else {

        }

        byte[] bytes = output_stream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        ByteArrayOutputStream returnStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, returnStream);
        return returnStream.toByteArray();
     //   return Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


        //eturn
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
