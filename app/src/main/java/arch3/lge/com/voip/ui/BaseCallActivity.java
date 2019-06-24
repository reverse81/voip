package arch3.lge.com.voip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;


public class BaseCallActivity extends AppCompatActivity {
    public arch3.lge.com.voip.model.codec.VoIPAudioIo mVoIPAudioIo;

    protected void attachImageView(ImageView view) {
        imageViewVideo = view;
    }
    SensorManager mySensorManager;
    Sensor myProximitySensor;
    PowerManager manager;
    PowerManager.WakeLock wl;

    @Override
    protected void onDestroy() {
        Log.i("TAG","onDestroy");
//        StopReceiveVideoThread();
//        VoIPVideoIo.getInstance().EndVideo();

        super.onDestroy();
        mySensorManager.unregisterListener(proximitySensorEventListener,myProximitySensor);
        if (wl !=null && wl.isHeld()) {
            Log.i("TAG","RELEASE");
            wl.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CallController.setCurrent(this);

         manager = (PowerManager) getSystemService(Context.POWER_SERVICE);

         if (manager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)){
             wl = manager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,"VOIP:LOCK");
         }

//
        mySensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);

        if (myProximitySensor == null) {

            //
        } else {
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    static  private ImageView imageViewVideo;
    static private Thread UdpReceiveVideoThread = null;
    static private boolean UdpVoipReceiveVideoThreadRun = false;
    static private DatagramSocket RecvVideoUdpSocket;
    MyEncrypt encipher = new MyEncrypt();
    protected void StartReceiveVideoThread() {
        // Create thread for receiving audio data
        PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.RECEIVING_VIDEO);
        if ( UdpVoipReceiveVideoThreadRun) return;
        UdpVoipReceiveVideoThreadRun = true;
        UdpReceiveVideoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    RecvVideoUdpSocket = new DatagramSocket(null);
                    RecvVideoUdpSocket.setReuseAddress(true);
                    RecvVideoUdpSocket.bind(new InetSocketAddress(NetworkConstants.VOIP_VIDEO_UDP_PORT));

                    while (UdpVoipReceiveVideoThreadRun) {
                        byte[] jpegbuf = new byte[NetworkConstants.VIDEO_BUFFER_SIZE];
                        DatagramPacket packet = new DatagramPacket(jpegbuf, NetworkConstants.VIDEO_BUFFER_SIZE);

                        RecvVideoUdpSocket.receive(packet);

                        Log.i(LOG_TAG, ":"+packet.getLength());


                        if (packet.getLength() >0) {

                            byte[] decrypt = encipher.decrypt(packet.getData(),0, packet.getLength());
                            if (decrypt == null || decrypt.length == 0) {
                                continue;
                            }
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(decrypt, 0, decrypt.length);
                          //  final Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                            final Matrix mtx = new Matrix();
                           // mtx.postRotate(-90);
                            final Bitmap rotator = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(), mtx,
                                    true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (imageViewVideo!= null) {
                                        imageViewVideo.setImageBitmap(rotator);
                                    }
                                }
                            });

                            //Log.i(LOG_TAG, "Video Packet received: " + packet.getLength());
                        } else
                            Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());

                    }
                    // close socket
                    RecvVideoUdpSocket.disconnect();
                    RecvVideoUdpSocket.close();
                } catch (SocketException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Clear Video Frame
                        if (imageViewVideo!= null) {
                            imageViewVideo.setImageBitmap(null);
                        }
                        Log.i(LOG_TAG, "Clear video Frame");

                    }
                });
            }

        });
        UdpReceiveVideoThread.start();

    }

    protected void StopReceiveVideoThread() {
        PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.VIDEO_STOPPED);
        if (!UdpVoipReceiveVideoThreadRun) return;
        if (UdpReceiveVideoThread != null && UdpReceiveVideoThread.isAlive()) {
            UdpVoipReceiveVideoThreadRun = false;
            RecvVideoUdpSocket.close();
            Log.i(LOG_TAG, "UdpReceiveDataThread Thread Join started");
            UdpVoipReceiveVideoThreadRun = false;
            try {
                UdpReceiveVideoThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "UdpReceiveDataThread Join interruped");
            }
            Log.i(LOG_TAG, " UdpReceiveDataThread Join successs");
        }

        UdpReceiveVideoThread = null;
        RecvVideoUdpSocket = null;
    }

    SensorEventListener proximitySensorEventListener
            = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    Log.i("Sensor", "nEEEEEEEEEEEEEEEEEEEr");
                    if (wl !=null && !wl.isHeld()) {
                        wl.acquire();
                    }
                } else {
                    Log.i("Sensor", "FAAAAAAAAAAAAAAAAAr");
                    if (wl !=null && wl.isHeld()) {
                        wl.release();
                    }
                }
            }
        }
    };
}
