package arch3.lge.com.voip.model.codec;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

@SuppressWarnings("deprecation")
public class VoIPVideoIoCC implements  Camera.PreviewCallback{
    private static final String LOG_TAG = "VoIPVideoIo";

    private static final int MAX_VIDEO_FRAME_SIZE =320*480*4;
    private DatagramSocket SendUdpSocket;
    private ArrayList<InetAddress> remoteIPList = new ArrayList<>();                   // Address to call
    private boolean IsRunning = false;
    @SuppressWarnings("FieldCanBeLocal")
    private SurfaceTexture mtexture;
    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private int frame;
    //private ImageView selfView;
    private VideoCodec mCodec;
    private Context mContext;

    private boolean banVideo = false;
    public boolean isBanned() {
        return banVideo;
    }

    public void setBanned(boolean ban) {
        banVideo = ban;
    }

    private VoIPVideoIoCC(Context context){
        mCodec = CodecFacotry.createVideo(CodecFacotry.VideoCodecType.MJPEG);
        mContext = context;
    }

    private static VoIPVideoIoCC mVoIPVideoIo;
    public static VoIPVideoIoCC getInstance(Context context) {
        if (mVoIPVideoIo ==null) {
            mVoIPVideoIo = new VoIPVideoIoCC(context);
        }
        return mVoIPVideoIo;
    }
    private ImageView selfView;
    public void attachView(ImageView view) {
        selfView = view;
    }
    public void attachIP () {
        for (String RemoteIP : PhoneState.getInstance().getRemoteIPs() ) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(RemoteIP);
                remoteIPList.add(address);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public  synchronized boolean startVideo() {
        if (IsRunning) {
            Log.i(LOG_TAG, "Already Start VoIP Video");
            return true;
        } else {
            Log.i(LOG_TAG, "Start VoIP Video");
            OpenCamera();
            IsRunning = true;
            return false;
        }
    }

    public synchronized boolean EndVideo() {
        Log.i(LOG_TAG, "Ending VoIp Video");
        if (!IsRunning) {
            return true;
        }
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
        params.setPreviewSize(320, 240);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
        mCamera.setPreviewCallbackWithBuffer(this);
        //mCamera.setDisplayOrientation(180);

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
        Util.safetyClose(SendUdpSocket);
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
            Bitmap image = mCodec.decode(imageBytes);
            if (selfView!= null) {
                selfView.setImageBitmap(image);
            }
            byte[] encryptedImageBytes = encipher.encrypt(imageBytes);

            if (remoteIPList != null) {
              //  Log.i(LOG_TAG, ":"+encryptedImageBytes.length + " vs "+ imageBytes.length);
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
                    int index = PhoneState.getInstance().myIndex(mContext);
                    if (index == 0 )
                    {
                        return;
                    }

                    for (InetAddress remoteIp : remoteIPList) {
                        if (remoteIp.getHostAddress().equals(PhoneState.getInstance().getPreviousIP(mContext))) {
                            continue;
                        }
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteIp, NetworkConstants.VOIP_VIDEO_UDP_PORT + index);
                        SendUdpSocket.send(packet);
                    }
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
