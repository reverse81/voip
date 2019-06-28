package arch3.lge.com.voip.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIoCC;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIoCC;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;

public class ConferenceCallingActivity extends AppCompatActivity {

    // start both io
    // start voice socket & video socket (each are 3)
    public final static String LOG_TAG = "VoIP:CCCalling";

    SensorManager mySensorManager;
    Sensor myProximitySensor;
    PowerManager manager;
    PowerManager.WakeLock wl;

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

        setContentView(R.layout.activity_conference_calling);

        ArrayList<ImageView> images = new ArrayList<>();
        images.add((ImageView)findViewById(R.id.cc1_back));
        images.add((ImageView)findViewById(R.id.cc2_back));
        images.add((ImageView)findViewById(R.id.cc3_back));
        images.add((ImageView)findViewById(R.id.cc4_back));
        VoIPAudioIoCC.getInstance(this).attachImageView(images);

        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        DeviceContorller.initDeviceForCC(this);
        CallController.startCCCall(this, phoneNumber);

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

        StartReceiveVideoThread();

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        final ImageButton speaker = (ImageButton)findViewById(R.id.speaker);
        final ImageButton bluetooth = (ImageButton)findViewById(R.id.bluetooth);
        final ImageButton mic = (ImageButton)findViewById(R.id.mic);

        if (audioManager.isMicrophoneMute()) {
            mic.setImageResource(R.drawable.mic_off);
        }
        if (audioManager.isBluetoothScoOn()) {
            bluetooth.setImageResource(R.drawable.bluetooth_on);
        }
        if (audioManager.isSpeakerphoneOn()) {
            speaker.setImageResource(R.drawable.speaker);
        }

        final ImageButton video = (ImageButton)findViewById(R.id.video_record);
        
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!VoIPVideoIo.getInstance().isBanned()) {
                    VoIPVideoIo.getInstance().EndVideo();
                    VoIPVideoIo.getInstance().setBanned(true);
                    video.setImageResource(R.drawable.video_off);

                } else {
                    VoIPVideoIo.getInstance().restartVideo();
                    VoIPVideoIo.getInstance().setBanned(false);
                    video.setImageResource(R.drawable.video_on);
                }
            }
        });

        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result = DeviceContorller.toggleSpeakerPhone(ConferenceCallingActivity.this);
                if (result) {
                    speaker.setImageResource(R.drawable.speaker);
                    bluetooth.setImageResource(R.drawable.bluetooth_disable);
                } else {
                    speaker.setImageResource(R.drawable.speaker_mute);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });


        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result =DeviceContorller.toggleBluetooth(ConferenceCallingActivity.this);
                if (result) {
                    speaker.setImageResource(R.drawable.speaker_mute);
                    bluetooth.setImageResource(R.drawable.bluetooth_on);
                } else {
                    bluetooth.setImageResource(R.drawable.bluetooth_disable);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean result =DeviceContorller.toggleMicMute(ConferenceCallingActivity.this);
                if (result) {
                    mic.setImageResource(R.drawable.mic_off);
                } else {
                    mic.setImageResource(R.drawable.mic_on);
                }
                //CallController.endCall(CallingActivity.this);
            }
        });
        ImageButton endCall = (ImageButton) findViewById(R.id.end_call);
        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //StopReceiveVideoThread();
//                VoIPVideoIoCC.getInstance(ConferenceCallingActivity.this).EndVideo();
//                VoIPAudioIoCC.getInstance(ConferenceCallingActivity.this).EndAudio();
                CallController.endCCCall(ConferenceCallingActivity.this);
            }
        });
    }

    static private Thread UdpReceiveVideoThread1;
    static private Thread UdpReceiveVideoThread2;
    static private Thread UdpReceiveVideoThread3;
    static private Thread UdpReceiveVideoThread4;
    static private boolean UdpVoipReceiveVideoThreadRun = false;
    MyEncrypt encipher = new MyEncrypt();
    protected void StartReceiveVideoThread() {
        // Create thread for receiving audio data
        PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.RECEIVING_VIDEO);
        if ( UdpVoipReceiveVideoThreadRun) return;

        UdpVoipReceiveVideoThreadRun = true;
        UdpReceiveVideoThread1 = new Thread(new CCRunnable(NetworkConstants.VOIP_VIDEO_UDP_PORT+1 , (ImageView)findViewById(R.id.cc1)));
        UdpReceiveVideoThread1.start();
        UdpReceiveVideoThread2=new Thread(new CCRunnable(NetworkConstants.VOIP_VIDEO_UDP_PORT+2, (ImageView)findViewById(R.id.cc2)));
        UdpReceiveVideoThread2.start();
        UdpReceiveVideoThread3 = new Thread(new CCRunnable(NetworkConstants.VOIP_VIDEO_UDP_PORT+3, (ImageView)findViewById(R.id.cc3)));
        UdpReceiveVideoThread3.start();
        UdpReceiveVideoThread4 = new Thread(new CCRunnable(NetworkConstants.VOIP_VIDEO_UDP_PORT+4, (ImageView)findViewById(R.id.cc4)));
        UdpReceiveVideoThread4.start();

    }

    class CCRunnable implements Runnable {
        int mPort;
        ImageView mDisplay;
        DatagramSocket recvVideoUdpSocket;
            public CCRunnable(int port, ImageView display) {
                mPort = port;
                mDisplay = display;
            }

            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    recvVideoUdpSocket = new DatagramSocket(null);
                    recvVideoUdpSocket.setReuseAddress(true);
                    recvVideoUdpSocket.bind(new InetSocketAddress(mPort));

                    while (UdpVoipReceiveVideoThreadRun) {
                        byte[] jpegbuf = new byte[NetworkConstants.VIDEO_BUFFER_SIZE];
                        DatagramPacket packet = new DatagramPacket(jpegbuf, NetworkConstants.VIDEO_BUFFER_SIZE);

                        recvVideoUdpSocket.receive(packet);
                        if (packet.getLength() >0) {

                            byte[] decrypt = encipher.decrypt(packet.getData(),0, packet.getLength());
                            if (decrypt == null || decrypt.length == 0) {
                                continue;
                            }
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(decrypt, 0, decrypt.length);
                            //  final Bitmap bitmap = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
//                            final Matrix mtx = new Matrix();
//                           // mtx.postRotate(-90);
//                            final Bitmap rotator = Bitmap.createBitmap(bitmap, 0, 0,
//                                    bitmap.getWidth(), bitmap.getHeight(), mtx,
//                                    true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mDisplay!= null) {
                                        mDisplay.setImageBitmap(bitmap);
                                    }
                                }
                            });

                            //Log.i(LOG_TAG, "Video Packet received: " + packet.getLength());
                        } else
                            Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());

                    }
                    // close socket

                } catch (SocketException e) {
                   // UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                  //  UdpVoipReceiveVideoThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                } finally {
                    recvVideoUdpSocket.disconnect();
                    recvVideoUdpSocket.close();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Clear Video Frame
                        if (mDisplay!= null) {
                            mDisplay.setImageBitmap(null);
                        }
                        Log.i(LOG_TAG, "Clear video Frame");

                    }
                });
            }

        }

    public void StopReceiveVideoThread() {
        PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.VIDEO_STOPPED);
        if (!UdpVoipReceiveVideoThreadRun) return;

        UdpVoipReceiveVideoThreadRun = false;
        if (        UdpReceiveVideoThread1 != null &&         UdpReceiveVideoThread1.isAlive()) {
            UdpReceiveVideoThread1.interrupt();
            UdpReceiveVideoThread1 = null;
        }
        if (        UdpReceiveVideoThread2 != null &&         UdpReceiveVideoThread2.isAlive()) {
            UdpReceiveVideoThread2.interrupt();
            UdpReceiveVideoThread2 = null;
        }
        if (        UdpReceiveVideoThread3 != null &&         UdpReceiveVideoThread3.isAlive()) {
            UdpReceiveVideoThread3.interrupt();
            UdpReceiveVideoThread3 = null;
        }
        if (        UdpReceiveVideoThread4 != null &&         UdpReceiveVideoThread4.isAlive()) {
            UdpReceiveVideoThread4.interrupt();
            UdpReceiveVideoThread4 = null;
        }
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
                        VoIPVideoIoCC.getInstance(ConferenceCallingActivity.this).EndVideo();
                    }
                } else {
                    if (wl !=null && wl.isHeld()) {
                        wl.release();
                        VoIPVideoIoCC.getInstance(ConferenceCallingActivity.this).startVideo();
                    }
                }
            }
        }
    };
}
