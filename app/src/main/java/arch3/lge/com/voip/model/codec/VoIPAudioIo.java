package arch3.lge.com.voip.model.codec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.InetSocketAddress;
import java.io.InputStream;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import android.os.Process;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.UDPnetwork.UserDatagramSocket;

public class VoIPAudioIo {

    private static final String LOG_TAG = "VoIPAudioIo";
    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private static final int GSM_BUFFER_SIZE = 33;
    private static final int VOIP_DATA_UDP_PORT = 5124;
    private int mSimVoice;
    private Context mContext;
    private Thread AudioIoThread = null;
    private boolean IsRunning = false;
    private boolean AudioIoThreadThreadRun = false;
    private ConcurrentLinkedQueue<byte[]> IncommingpacketQueue;
    private AudioCodec mCodec;
    private boolean mBoostAudio = false;
    private UserDatagramSocket mSock = new UserDatagramSocket(VOIP_DATA_UDP_PORT);

    public VoIPAudioIo(Context context) {
        mContext = context;
        mCodec = CodecFacotry.createAudio(CodecFacotry.AudioCodecType.GSM0610);
    }

    public synchronized boolean StartAudio(InetAddress IP, int SimVoice) {
        if (IsRunning) return (true);
        if (mCodec.open() == true)
            Log.i(LOG_TAG, "JniGsmOpen() Success");
        IncommingpacketQueue = new ConcurrentLinkedQueue<>();
        mSimVoice = SimVoice;
        mSock.setAddress(IP);
        StartAudioIoThread();
        StartReceiveDataThread();
        IsRunning = true;
        return (false);
    }

    public synchronized boolean EndAudio() {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending Viop Audio");

        mSock.setListener(null);

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
        IncommingpacketQueue = null;
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
                AudioRecord Recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));

                AudioTrack OutputTrack = new AudioTrack.Builder()
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

                int BytesRead;
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                try {
                    // Create a socket and start recording
                    DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                    OutputTrack.play();
                    while (AudioIoThreadThreadRun) {
                        if (IncommingpacketQueue.size() > 0) {
                            byte[] AudioOutputBufferBytes = IncommingpacketQueue.remove();
                            if (mBoostAudio) {
                                OutputTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                            }
                            else {
                                short[] AudioOutputBufferShorts = new short[AudioOutputBufferBytes.length / 2];
                                // to turn bytes to shorts as either big endian or little endian.
                                ByteBuffer.wrap(AudioOutputBufferBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(AudioOutputBufferShorts);
                                for (int i = 0; i < AudioOutputBufferShorts.length; i++) { // 16bit sample size
                                    int value=AudioOutputBufferShorts[i]*10; //increase level by gain=20dB: Math.pow(10., dB/20.);  dB to gain factor
                                    if(value > 32767) {
                                        value = 32767;
                                    } else if(value < -32767) {
                                        value = -32767;
                                    }
                                    AudioOutputBufferShorts[i]=(short)value;
                                }
                                // to turn shorts back to bytes.
                                //byte[] AudioOutputBufferBytes2 = new byte[AudioOutputBufferShorts.length * 2];
                                //ByteBuffer.wrap(AudioOutputBufferBytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(AudioOutputBufferShorts);
                                //OutputTrack.write(AudioOutputBufferBytes2, 0, RAW_BUFFER_SIZE);
                                OutputTrack.write( AudioOutputBufferShorts, 0,  AudioOutputBufferShorts.length);
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
                            mCodec.encode(rawbuf, gsmbuf);
                            mSock.send(gsmbuf);
                        }
                    }
                    // Stop Audio Thread);
                    Recorder.stop();
                    Recorder.release();
                    OutputTrack.stop();
                    OutputTrack.flush();
                    OutputTrack.release();
                    socket.disconnect();
                    socket.close();
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

    private void StartReceiveDataThread() {
        mSock.setListener(new UserDatagramSocket.PacketDataHandler() {
            @Override
            public void onReceive(DatagramPacket packet) {
                if (packet.getLength() == GSM_BUFFER_SIZE) {
                    byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
                    mCodec.decode(packet.getData(), rawbuf);
                    IncommingpacketQueue.add(rawbuf);
                } else
                    Log.i(LOG_TAG, "Invalid Packet LengthReceived: " + packet.getLength());
            }
        });
    }

}


