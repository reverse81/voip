package arch3.lge.com.voip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.AdaptiveBuffering;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;
import cz.msebera.android.httpclient.conn.HttpHostConnectException;


public class BaseCallActivity extends AppCompatActivity {
    public final static String LOG_TAG = "VoIP:BaseCallActivity";
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getSupportActionBar().hide();

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

    static private ImageView imageViewVideo;
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

                        if (!packet.getAddress().getHostAddress().equals(PhoneState.getInstance().getRemoteIP())) {
                            //Log.i(LOG_TAG, "Skipppppped"+packet.getAddress().getHostAddress()  + " vs "+PhoneState.getInstance().getRemoteIP());
                            continue;
                        }
                       // Log.i(LOG_TAG, ":"+packet.getLength());


                        if (packet.getLength() >0) {

//                            byte[] decrypt = encipher.decrypt(packet.getData(),0, packet.getLength());
//                            if (decrypt == null || decrypt.length == 0) {
//                                continue;
//                            }
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                          //  final Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
//                            final Matrix mtx = new Matrix();
//                           // mtx.postRotate(-90);
//                            final Bitmap rotator = Bitmap.createBitmap(bitmap, 0, 0,
//                                    bitmap.getWidth(), bitmap.getHeight(), mtx,
//                                    true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (imageViewVideo!= null) {
                                        imageViewVideo.setImageBitmap(bitmap);
                                    }
                                }
                            });

                            //Log.i(LOG_TAG, "Video Packet received: " + packet.getLength());
                        } else
                            Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());

                        if (!VoIPVideoIo.getInstance(BaseCallActivity.this).isBanned() ) {
                            if (wl !=null && !wl.isHeld()) {
                                if (AdaptiveBuffering.getPacketLoss() < AdaptiveBuffering.MIN_PACKET_LOSS) {
                                    VoIPVideoIo.getInstance(BaseCallActivity.this).restartVideo();
                                }
                            }
                        }


                    }
                    // close socket

                } catch (SocketException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " ,e);
                } catch (IOException e) {
                    UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " ,e);
                } finally {
                    if (RecvVideoUdpSocket!=null) {
                        RecvVideoUdpSocket.disconnect();
                        RecvVideoUdpSocket.close();
                    }
                    RecvVideoUdpSocket = null;
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

    public void StopReceiveVideoThread() {
        PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.VIDEO_STOPPED);
        if (!UdpVoipReceiveVideoThreadRun) return;
        if (UdpReceiveVideoThread != null && UdpReceiveVideoThread.isAlive()) {
            UdpVoipReceiveVideoThreadRun = false;

            //RecvVideoUdpSocket.close();
            Log.i(LOG_TAG, "UdpReceiveDataThread Thread Join started");
            UdpVoipReceiveVideoThreadRun = false;
                UdpReceiveVideoThread.interrupt();
            Log.i(LOG_TAG, " UdpReceiveDataThread Join successs");
        }

        UdpReceiveVideoThread = null;
      //  RecvVideoUdpSocket = null;
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
                    if (wl !=null && !wl.isHeld()) {
                        wl.acquire();
                        VoIPVideoIo.getInstance(BaseCallActivity.this).EndVideo();
                    }
                } else {
                    if (wl !=null && wl.isHeld()) {
                        wl.release();
                        if (AdaptiveBuffering.getPacketLoss() < AdaptiveBuffering.MIN_PACKET_LOSS) {
                            if (!VoIPVideoIo.getInstance(BaseCallActivity.this).isBanned()) {
                                VoIPVideoIo.getInstance(BaseCallActivity.this).restartVideo();
                            }
                        }
                    }
                }
            }
        }
    };
}
