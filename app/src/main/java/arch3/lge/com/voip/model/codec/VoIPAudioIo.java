package arch3.lge.com.voip.model.codec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;
import android.os.Process;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.UDPnetwork.UserDatagramSocket;
import arch3.lge.com.voip.utils.NetworkConstants;

public class VoIPAudioIo {

    private static final String LOG_TAG = "VoIPAudioIo";
    private static final int MILLISECONDS_IN_A_SECOND = 1000;
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 10;   // Milliseconds
    private static final int BYTES_PER_SAMPLE = 2;    // Bytes Per Sampl;e
    private static final int RAW_BUFFER_SIZE = SAMPLE_RATE / (MILLISECONDS_IN_A_SECOND / SAMPLE_INTERVAL) * BYTES_PER_SAMPLE;
    private static final int GSM_BUFFER_SIZE = 33;
    private int mSimVoice;
    private Context mContext;
    private Thread AudioIoThread = null;
    private boolean IsRunning = false;
    private boolean AudioIoThreadThreadRun = false;
    private AudioCodec mCodec;
    // private boolean mBoostAudio = false;
    private UserDatagramSocket mSock = new UserDatagramSocket(NetworkConstants.VOIP_AUDIO_UDP_PORT);
    private AdaptiveBuffering mAdaptiveBuffer = new AdaptiveBuffering();

    private VoIPAudioIo(Context context) {
        mContext = context;
        mCodec = CodecFacotry.createAudio(CodecFacotry.AudioCodecType.G729B);
    }

    private static VoIPAudioIo mVoIPAudioIo;
    public static VoIPAudioIo getInstance(Context context) {
        if (mVoIPAudioIo == null) {
            mVoIPAudioIo = new VoIPAudioIo(context);
        }
        return mVoIPAudioIo;
    }

    public synchronized boolean StartAudio(String ip, int SimVoice) {
        if (IsRunning) return (true);
        if (mCodec.open() == true)
            Log.i(LOG_TAG, "JniGsmOpen() Success");
        mAdaptiveBuffer.reset();
        mSimVoice = SimVoice;
        mSock.setAddress(ip);
        StartAudioIoThread();
        StartReceiveDataThread();
        IsRunning = true;
        return (false);
    }

    public synchronized boolean EndAudio() {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending VoIp Audio");

        mSock.closeSendSocket();
        mSock.setListener(null);


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
                //byte[] gsmbuf = new byte[GSM_BUFFER_SIZE];
                try {
                    // Create a socket and start recording
                    // DatagramSocket socket = new DatagramSocket();
                    Recorder.startRecording();
                    OutputTrack.play();
                    mAdaptiveBuffer.reset();
                    while (AudioIoThreadThreadRun) {
                        if (mAdaptiveBuffer.isEmpty() == false) {
                            byte[] AudioOutputBufferBytes = mAdaptiveBuffer.getQueue();
                            OutputTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                        }
                        // Capture audio from microphone and send
                        BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);
                        if (BytesRead == RAW_BUFFER_SIZE) {
                            byte[] gsmbuf = mCodec.encode(rawbuf, 0, rawbuf.length);
                            byte [] pktData = mAdaptiveBuffer.writeHeader(gsmbuf);
                            mSock.send(pktData);
                        }
                    }
                    Recorder.stop();
                    Recorder.release();
                    OutputTrack.stop();
                    OutputTrack.flush();
                    OutputTrack.release();
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

            boolean logged = true;
            @Override
            public void onReceive(DatagramPacket packet) {

                byte[] pktData = packet.getData();
                int offset = mAdaptiveBuffer.readHeader(pktData);
                byte[] rawbuf = mCodec.decode(pktData, offset, packet.getLength()-offset);
                mAdaptiveBuffer.addQueue(rawbuf);
            }
        });
    }

}


