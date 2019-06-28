package arch3.lge.com.voip.model.codec;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
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
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.ui.ConferenceCallingActivity;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

public class VoIPAudioIoCC {

    private static final String LOG_TAG = "VoIPAudioIo";
    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 10;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private ConferenceCallingActivity mContext;
    private Thread AudioIoThread = null;
    private boolean IsRunning = false;
    private boolean AudioIoThreadThreadRun = false;
    private AudioCodec mCodec;

    private DatagramSocket sendUdpSocket;
  private ArrayList<LinkedBlockingQueue<byte[]>> mQueueList = new ArrayList<>();

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

    private Thread player1;
    private Thread player2;
    private Thread player3;
    private Thread player4;

    private int audioSessionId;
    private void StartAudioIoThread() {
        // Creates the thread for capturing and transmitting audio
        AudioIoThreadThreadRun = true;

        AudioIoThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                int PreviousAudioManagerMode = 0;
                if (audioManager != null) {
                    PreviousAudioManagerMode = audioManager.getMode();
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION); //Enable AEC
                }

                // Create an instance of the AudioRecord class
                Log.i(LOG_TAG, "Audio Thread started. Thread id: " + Thread.currentThread().getId());

                AudioRecord Recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
                audioSessionId = Recorder.getAudioSessionId();
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

                player1 = new Thread(new AudioPlayer(mQueueList.get(0), imageList.get(0) , 0 == (PhoneState.getInstance().myIndex(mContext) -1) ) );
                player2 = new Thread(new AudioPlayer(mQueueList.get(1) , imageList.get(1) , 1 == (PhoneState.getInstance().myIndex(mContext) -1) ));
                player3 = new Thread(new AudioPlayer(mQueueList.get(2), imageList.get(2),  2== (PhoneState.getInstance().myIndex(mContext) -1) ));
                player4 = new Thread(new AudioPlayer(mQueueList.get(3) , imageList.get(3), 3 == (PhoneState.getInstance().myIndex(mContext) -1)));

                player1.start();
                player2.start();
                player3.start();
                player4.start();

                int BytesRead;
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                //byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                try {
                    // Create a socket and start recording
                    // DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                    while (AudioIoThreadThreadRun) {
                        // Capture audio from microphone and send
                        BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);

                        if (BytesRead == RAW_BUFFER_SIZE) {
                            byte[] gsmbuf = mCodec.encode(rawbuf, 0, rawbuf.length);
                            for (InetAddress remoteIp : remoteIPList) {
                                    DatagramPacket packet = new DatagramPacket(gsmbuf, gsmbuf.length, remoteIp, NetworkConstants.VOIP_AUDIO_UDP_PORT);
                                    sendUdpSocket.send(packet);
                            }
                        }
                    }
                    Recorder.stop();
                    Recorder.release();

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

    class AudioPlayer implements Runnable {

        private LinkedBlockingQueue<byte[]> IncommingpacketQueue;
        AudioTrack outTrack;
        ImageView mImageView;
        boolean mSelf;

        public AudioPlayer(LinkedBlockingQueue<byte[]> queue,ImageView imageView,boolean self) {
            IncommingpacketQueue = queue;
            mImageView = imageView;
            mSelf = self;
        }

        @Override
        public void run() {
            // Create an instance of AudioTrack, used for playing back audio
            Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
            try {
                // Setup socket to receive the audio data
                outTrack = new AudioTrack.Builder()
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
                        .setSessionId(audioSessionId)
                        .build();

                outTrack.play();
                IncommingpacketQueue.clear();
                while (AudioIoThreadThreadRun) {
                    if (IncommingpacketQueue.size() >0) {
                        byte[] AudioOutputBufferBytes = IncommingpacketQueue.remove();
                        if (AudioOutputBufferBytes.length > 160) {
                            AudioOutputBufferBytes = Arrays.copyOf(AudioOutputBufferBytes, AudioOutputBufferBytes.length - 1);
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        if (!mSelf) {
                            outTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                        }
                    }
                }
            } finally {
                outTrack.stop();
                outTrack.flush();
                outTrack.release();
            }
        }
    }

    class CCRunnable implements Runnable {
        DatagramSocket recvAudioUdpSocket;
        //private LinkedBlockingQueue<byte[]> IncommingpacketQueue;

        public CCRunnable() {
            for (int i=0;i<4 ;i++) {
                LinkedBlockingQueue<byte[]> IncommingpacketQueue = new LinkedBlockingQueue<>(200);
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
                    try {
                        LinkedBlockingQueue<byte[]> IncommingpacketQueue = mQueueList.get(index);
                        //Log.i("SSSSSSSSSSSSSSSSS",index +" aaaaaaaaaaaaaaa "+ IncommingpacketQueue.size());
                        if (IncommingpacketQueue.remainingCapacity() > 1) {
                            IncommingpacketQueue.add(rawbuf);

                        } else {
                            IncommingpacketQueue.remove();
                            IncommingpacketQueue.add(rawbuf);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(LOG_TAG, "IndexError: " ,e);
                    }
                }
            } catch (SocketException e) {
                Log.e(LOG_TAG, "SocketException: " ,e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException: " ,e);
            } finally {
                recvAudioUdpSocket.disconnect();
                recvAudioUdpSocket.close();
            }
        }
    }

}


