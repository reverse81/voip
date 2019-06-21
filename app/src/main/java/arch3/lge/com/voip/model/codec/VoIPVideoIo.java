package arch3.lge.com.voip.model.codec;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.* ; // avoid  deprecation warning import android.hardware.Camera;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import arch3.lge.com.voip.model.encrypt.MyEncrypt;

import static android.support.constraint.Constraints.TAG;
import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

@SuppressWarnings("deprecation")
public class VoIPVideoIo implements  Camera.PreviewCallback{
    private static final String LOG_TAG = "VoIPVideoIo";

    private static final int VOIP_VIDEO_UDP_PORT = 5125;
    private static final int MAX_VIDEO_FRAME_SIZE =640*480*4;
    private DatagramSocket SendUdpSocket;
    private InetAddress remoteIp;                   // Address to call
    private boolean IsRunning = false;
    @SuppressWarnings("FieldCanBeLocal")
    private SurfaceTexture mtexture;
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private int frame;
    private ImageView selfView;
    private VideoCodec mCodec;

    private VoIPVideoIo(){
        mCodec = CodecFacotry.createVideo(CodecFacotry.VideoCodecType.MJPEG);
    }

    private static VoIPVideoIo mVoIPVideoIo;
    public static VoIPVideoIo getInstance() {
        if (mVoIPVideoIo ==null) {
            mVoIPVideoIo = new VoIPVideoIo();
        }
        return mVoIPVideoIo;
    }

    public void attachIP (String RemoteIP) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(RemoteIP);
            this.remoteIp = address;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public  synchronized boolean StartVideo(ImageView view) {
        if (IsRunning) return (true);
        selfView = view;
        OpenCamera();
        IsRunning = true;
        return (false);
    }

    public synchronized boolean EndVideo() {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending Viop Audio");
        IsRunning = false;
        CloseCamera();
        return (false);
    }


    MyEncrypt encipher;
    private void OpenCamera()  {

        encipher = new MyEncrypt();

        if (mCamera!=null) return;
        try {
            SendUdpSocket = new DatagramSocket();
        }
       catch (SocketException e){

            Log.e(LOG_TAG, "SocketException: " + e.toString());
        }
        frame=0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    mCamera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        mtexture = new SurfaceTexture(10);
        try {
            mCamera.setPreviewTexture(mtexture);
        } catch (IOException e1) {
            Log.e(LOG_TAG, e1.getMessage());
        }

        Camera.Parameters params = mCamera.getParameters();

        params.setPreviewSize(480, 640);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        mCamera.setPreviewCallbackWithBuffer(this);

        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.addCallbackBuffer(new byte[MAX_VIDEO_FRAME_SIZE]);
        mCamera.startPreview();

    }

    private void CloseCamera()  {
        if (mCamera==null) return;
        mCamera.stopPreview();
        mCamera.setPreviewCallbackWithBuffer(null);
        mCamera.release();
        mCamera=null;
        SendUdpSocket.disconnect();
        SendUdpSocket.close();
        SendUdpSocket=null;
    }
    
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        frame++;
        if ((frame%2)!=0) {//only process every other frame;
            camera.addCallbackBuffer(data);
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        int format = parameters.getPreviewFormat();
        //YUV formats require more conversion
        if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {

            byte[] imageBytes = mCodec.encode(data, format, parameters.getPreviewSize().width, parameters.getPreviewSize().height);

            byte[] encryptedImageBytes = encipher.encrypt(imageBytes);

            //byte[] decrypt = encipher.decrypt(imageBytes);

            Bitmap image = mCodec.decode(imageBytes);
            selfView.setImageBitmap(image);

            if (remoteIp != null) {
                Log.i(LOG_TAG, ":"+encryptedImageBytes.length + " vs "+ imageBytes.length);
                UdpSend(encryptedImageBytes);
               // UdpSend(imageBytes);
            }
        }
        camera.addCallbackBuffer(data);
    }
    private void UdpSend(final byte[] bytes) {
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteIp, VOIP_VIDEO_UDP_PORT);
                        SendUdpSocket.send(packet);
                } catch (SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in UdpSend: " + e);
                } catch (IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in UdpSend: " + e);
                }
            }
        });
        replyThread.start();
    }

}
