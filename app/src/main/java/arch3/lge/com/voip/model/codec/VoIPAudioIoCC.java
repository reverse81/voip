package arch3.lge.com.voip.model.codec;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.UDPnetwork.UserDatagramSocket;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.ui.ConferenceCallingActivity;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

import static java.util.Arrays.copyOf;

public class VoIPAudioIoCC {

    private static final String LOG_TAG = "VoIPAudioIo";
    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 10;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private static final int GSM_BUFFER_SIZE = 33;
    private int mSimVoice;
    private ConferenceCallingActivity mContext;
    private Thread AudioIoThread = null;
    private boolean IsRunning = false;
    private boolean AudioIoThreadThreadRun = false;
   // private LinkedBlockingQueue<byte[]> IncommingpacketQueue1;
   //private LinkedBlockingQueue<byte[]> IncommingpacketQueue;
    private AudioCodec mCodec;

    private DatagramSocket sendUdpSocket;
   // private ArrayList<DatagramSocket> receiveUDPSocketList = new ArrayList<>();
  private ArrayList<LinkedBlockingQueue<byte[]>> mQueueList = new ArrayList<>();

    // private boolean mBoostAudio = false;
   // private UserDatagramSocket mSock = new UserDatagramSocket(NetworkConstants.VOIP_AUDIO_UDP_PORT);

    private VoIPAudioIoCC(ConferenceCallingActivity context) {
        mContext = context;
        mCodec = CodecFacotry.createAudio(CodecFacotry.AudioCodecType.G729B);
    }

    private ArrayList<InetAddress> remoteIPList = new ArrayList<>();
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

    private ArrayList<ImageView> imageList;
    public void attachImageView (ArrayList<ImageView> list) {
        imageList = list;
    }

    private static VoIPAudioIoCC mVoIPAudioIo;
    public static VoIPAudioIoCC getInstance(ConferenceCallingActivity context) {
        if (mVoIPAudioIo == null) {
            mVoIPAudioIo = new VoIPAudioIoCC(context);
        }
        return mVoIPAudioIo;
    }

    public synchronized boolean StartAudio() {
        if (IsRunning) return (true);
        if (mCodec.open() == true)
            Log.i(LOG_TAG, "JniGsmOpen() Success");

        try {
            sendUdpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        StartAudioIoThread();
        StartReceiveDataThread();
        IsRunning = true;
        return (false);
    }

    public synchronized boolean EndAudio() {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending VoIp Audio");

        sendUdpSocket.disconnect();
        Util.safetyClose(sendUdpSocket);

        Log.i(LOG_TAG, "Ending VoIp Audio");
        if (AudioIoThread != null && AudioIoThread.isAlive()) {
            AudioIoThreadThreadRun = false;
            Log.i(LOG_TAG, "Audio Thread Join started");

            try {
                AudioIoThread.join();
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "Audio Thread Join interruped");
            }
            Log.i(LOG_TAG, "Audio Thread Join successs");
        }

        AudioIoThread = null;

        for (LinkedBlockingQueue queue : mQueueList) {
            queue.clear();
        }            mQueueList.clear();

        mCodec.close();
        IsRunning = false;
        return (false);
    }

    private InputStream OpenSimVoice(int SimVoice) {
        InputStream VoiceFile = null;
        switch (SimVoice) {
            case 0:
                break;
            case 1:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t18k16bit);
                break;
            case 2:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t28k16bit);
                break;
            case 3:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t38k16bit);
                break;
            case 4:
                VoiceFile = mContext.getResources().openRawResource(R.raw.t48k16bit);
                break;
            default:
                break;
        }
        return VoiceFile;
    }


    private void StartAudioIoThread() {
        // Creates the thread for capturing and transmitting audio
        AudioIoThreadThreadRun = true;
        AudioIoThread = new Thread(new Runnable() {

            @Override
            public void run() {
                InputStream InputPlayFile;
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int PreviousAudioManagerMode = 0;
                if (audioManager != null) {
                    PreviousAudioManagerMode = audioManager.getMode();
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); //Enable AEC
                }

                // Create an instance of the AudioRecord class
                Log.i(LOG_TAG, "Audio Thread started. Thread id: " + Thread.currentThread().getId());
                InputPlayFile = OpenSimVoice(mSimVoice);
                AudioRecord Recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
                int audioSessionId = Recorder.getAudioSessionId();
                if(NoiseSuppressor.isAvailable())
                {
                    NoiseSuppressor ns = NoiseSuppressor.create(audioSessionId);
                    ns.setEnabled(true);
                    Log.i(LOG_TAG, "NoiseSuppressor : "+ ns.getEnabled() );
                }
                if(AcousticEchoCanceler.isAvailable()){

                    AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioSessionId);
                    aec.setEnabled(true);
                    Log.i(LOG_TAG, "AcousticEchoCanceler : "+ aec.getEnabled() );
                }

                ArrayList<AudioTrack> tracks = new ArrayList<>();
                for (LinkedBlockingQueue queue : mQueueList)
                {
                    AudioTrack outTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    //	.setFlags(AudioAttributes.FLAG_LOW_LATENCY) //This is Nougat+ only (API 25) comment if you have lower
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                            .setBufferSizeInBytes(RAW_BUFFER_SIZE)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            //.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) //Not until Api 26
                            .setSessionId(Recorder.getAudioSessionId())
                            .build();
                    tracks.add(outTrack);
                }


                int BytesRead;
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                //byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                try {
                    // Create a socket and start recording
                    // DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                     for (LinkedBlockingQueue queue : mQueueList) {
                        queue.clear();
                    }
                    for (AudioTrack track : tracks) {
                        track.play();
                    }
                    while (AudioIoThreadThreadRun) {
                        for(int i=0; i<mQueueList.size();i++) {
                            LinkedBlockingQueue<byte[]> queue = mQueueList.get(i);
                            AudioTrack track = tracks.get(i);
                            if (queue.size() > 0) {

                                byte[] AudioOutputBufferBytes = queue.remove();
                                if (AudioOutputBufferBytes.length > 160) {
                                    AudioOutputBufferBytes = Arrays.copyOf(AudioOutputBufferBytes, AudioOutputBufferBytes.length-1);
                                    final int index = i;
                                    mContext.runOnUiThread(new Runnable() {
                                                               @Override
                                                               public void run() {
                                                                   imageList.get(index).setVisibility(View.INVISIBLE);
                                                               }
                                                           });
                                } else {
                                    final int index = i;
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            imageList.get(index).setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                                if (i+1 != PhoneState.getInstance().myIndex(mContext)) {
                                    track.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                                }
                            }
                        }

                        // Capture audio from microphone and send
                        BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);
                        if (InputPlayFile != null) {
                            BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
                            if (BytesRead != RAW_BUFFER_SIZE) {
                                InputPlayFile.close();
                                InputPlayFile = OpenSimVoice(mSimVoice);
                                BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
                            }
                        }
                        if (BytesRead == RAW_BUFFER_SIZE) {
                            int index = PhoneState.getInstance().myIndex(mContext);
                            if (index == 0 )
                            {
                                continue;
                            }
                            byte[] gsmbuf = mCodec.encode(rawbuf, 0, rawbuf.length);
                            for (InetAddress remoteIp : remoteIPList) {
                                    DatagramPacket packet = new DatagramPacket(gsmbuf, gsmbuf.length, remoteIp, NetworkConstants.VOIP_AUDIO_UDP_PORT);
                                    sendUdpSocket.send(packet);
                            }
                        }
                    }
                    Recorder.stop();
                    Recorder.release();
                    for (AudioTrack track: tracks) {
                        track.stop();
                        track.flush();
                        track.release();
                    }

                    if (InputPlayFile != null) InputPlayFile.close();
                    if (audioManager != null) audioManager.setMode(PreviousAudioManagerMode);
                    Log.i(LOG_TAG, "Audio Thread Stopped");
                } catch (SocketException e) {
                    AudioIoThreadThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                } catch (UnknownHostException e) {
                    AudioIoThreadThreadRun = false;
                    Log.e(LOG_TAG, "UnknownHostException: " + e.toString());
                } catch (IOException e) {
                    AudioIoThreadThreadRun = false;
                    Log.e(LOG_TAG, "IOException: " + e.toString());
                }
            }
        });
        AudioIoThread.start();
    }

    static private Thread UdpReceiveAudioThread1;
    private boolean UdpVoipReceiveDataThreadRun = false;
    private void StartReceiveDataThread() {
        if ( UdpVoipReceiveDataThreadRun) return;

        UdpVoipReceiveDataThreadRun = true;
        UdpReceiveAudioThread1 = new Thread(new CCRunnable());
        UdpReceiveAudioThread1.start();
    }

    class CCRunnable implements Runnable {
        DatagramSocket recvAudioUdpSocket;
        //private LinkedBlockingQueue<byte[]> IncommingpacketQueue;

        public CCRunnable() {
            for (int i=0;i<4 ;i++) {
                LinkedBlockingQueue<byte[]> IncommingpacketQueue = new LinkedBlockingQueue<>(20);
                mQueueList.add(IncommingpacketQueue);
            }
        }

        @Override
        public void run() {
            // Create an instance of AudioTrack, used for playing back audio
            Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
            try {
                // Setup socket to receive the audio data
                recvAudioUdpSocket = new DatagramSocket(null);
                recvAudioUdpSocket.setReuseAddress(true);
                recvAudioUdpSocket.bind(new InetSocketAddress(NetworkConstants.VOIP_AUDIO_UDP_PORT));

                while (UdpVoipReceiveDataThreadRun) {
                    byte [] mBuffer = new byte[8*1024];
                    DatagramPacket packet = new DatagramPacket(mBuffer, mBuffer.length);
                    recvAudioUdpSocket.receive(packet);

                    byte[] rawbuf = mCodec.decode(packet.getData(), 0, packet.getLength());
                    if (packet.getLength() <40) {
                        rawbuf = Arrays.copyOf(rawbuf, rawbuf.length +1);

                    }

                    String senderIP = packet.getAddress().getHostAddress();
                    int index =0;
                    for (int i=0; i< PhoneState.getInstance().getRemoteIPs().size() ;i++) {
                        if (PhoneState.getInstance().getRemoteIPs().get(i).equals(senderIP)) {
                            index =i;
                            break;
                        }
                    }
                    LinkedBlockingQueue<byte[]> IncommingpacketQueue = mQueueList.get(index);
                    Log.i("SSSSSSSSSSSSSSSSS",index +" aaaaaaaaaaaaaaa "+ IncommingpacketQueue.size());
                    if (IncommingpacketQueue.remainingCapacity() >1)  {
                        IncommingpacketQueue.add(rawbuf);

                    } else {
                        IncommingpacketQueue.remove();
                        IncommingpacketQueue.add(rawbuf);
                    }
                }
            } catch (SocketException e) {
                Log.e(LOG_TAG, "SocketException: " + e.toString());
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException: " + e.toString());
            } finally {
                recvAudioUdpSocket.disconnect();
                recvAudioUdpSocket.close();
            }
        }
    }

}


