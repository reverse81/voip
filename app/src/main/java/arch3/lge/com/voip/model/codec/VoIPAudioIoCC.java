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

public class VoIPAudioIoCC {

    private static final String LOG_TAG = "VoIPAudioIoCCC";
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

   // private DatagramSocket sendUdpSocket;
    private ArrayList<LinkedBlockingQueue<byte[]>> mQueueList = new ArrayList<>();

    private VoIPAudioIoCC(ConferenceCallingActivity context) {
        mContext = context;
        mCodec = CodecFacotry.createAudio(CodecFacotry.AudioCodecType.G729B);
    }

    private ArrayList<InetAddress> remoteIPList = new ArrayList<>();
    private ArrayList<DatagramSocket> sendSocketList = new ArrayList<>();
    public void attachIP () {
        for (String RemoteIP : PhoneState.getInstance().getRemoteIPs() ) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(RemoteIP);
                remoteIPList.add(address);
                DatagramSocket socket = new DatagramSocket();
                sendSocketList.add(socket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
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

//        try {
//            sendUdpSocket = new DatagramSocket();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }

        StartAudioIoThread();
        StartReceiveDataThread();
        IsRunning = true;
        return (false);
    }

    public synchronized boolean EndAudio() {
        if (!IsRunning) return (true);
        Log.i(LOG_TAG, "Ending VoIp Audio");

        for (DatagramSocket socket :sendSocketList ){
            if (socket !=null) {
                socket.disconnect();
                socket.close();
            }
        }sendSocketList.clear();

//        sendUdpS1ocket.disconnect();
//        Util.safetyClose(sendUdpSocket);

        UdpVoipReceiveDataThreadRun = false;
        if (UdpReceiveAudioThread1 !=null&& UdpReceiveAudioThread1.isAlive()) {
            UdpReceiveAudioThread1.interrupt();
        }
        if (UdpReceiveAudioThread2 !=null&& UdpReceiveAudioThread2.isAlive()) {
            UdpReceiveAudioThread2.interrupt();
        }
        if (UdpReceiveAudioThread3 !=null&& UdpReceiveAudioThread3.isAlive()) {
            UdpReceiveAudioThread3.interrupt();
        }
        if (UdpReceiveAudioThread4 !=null&& UdpReceiveAudioThread4.isAlive()) {
            UdpReceiveAudioThread4.interrupt();
        }



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

        for (Thread player : playerList)
        {
            if (player!=null) {
                player.interrupt();
            }
        } playerList.clear();

        AudioIoThread = null;

        for (LinkedBlockingQueue queue : mQueueList) {
            queue.clear();
        }            mQueueList.clear();

        mCodec.close();
        IsRunning = false;
        return (false);
    }

    private ArrayList<Thread> playerList=new ArrayList<>();


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

                for (int i =0 ; i < PhoneState.getInstance().getRemoteIPs().size() ;i ++)
                {
                    Thread player =  new Thread(new AudioPlayer(mQueueList.get(i), imageList.get(i)  ,i));
                    playerList.add(player);
                    if (! (i == (PhoneState.getInstance().myIndex(mContext) -1))) {
                        player.start();
                    }
                }

                int BytesRead;
                byte[] rawbuf = new byte[RAW_BUFFER_SIZE];

                Recorder.startRecording();
                long systemTime = System.currentTimeMillis();
                int count=0;
                while (AudioIoThreadThreadRun) {
                    // Capture audio from microphone and send

                    //   Log.i(LOG_TAG, "start record " + systemTime);
                    BytesRead = Recorder.read(rawbuf, 0, RAW_BUFFER_SIZE);
                    if (System.currentTimeMillis() - systemTime > 10000) {
                        Log.i(LOG_TAG, "Check voice recorder frame : "+ (count /10));
                        systemTime = System.currentTimeMillis();
                        count= 0;
                    } else {
                        count++;
                    }
                    if (BytesRead == RAW_BUFFER_SIZE) {
                        byte[] gsmbuf = mCodec.encode(rawbuf, 0, rawbuf.length);

                        //    Log.i(LOG_TAG, "end record " + (System.currentTimeMillis() -systemTime));
                        try {
                            for (int i = 0; i < remoteIPList.size(); i++) {
                                InetAddress remoteIp = remoteIPList.get(i);
                                DatagramSocket socket = sendSocketList.get(i);
                                if (gsmbuf.length > 40) {
                                    udpSend(rawbuf, remoteIp, socket);
                                } else {
                                    udpSend(gsmbuf, remoteIp, socket);
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            ///////////////////////////////////////
                        }

//                        for (InetAddress remoteIp : remoteIPList) {
//
////                                    DatagramPacket packet = new DatagramPacket(gsmbuf, gsmbuf.length, remoteIp, NetworkConstants.VOIP_AUDIO_UDP_PORT);
////                                    sendUdpSocket.send(packet);
//                        }
                        //  Log.i(LOG_TAG, "end record " + (System.currentTimeMillis() -systemTime));
                    }
                }
                Recorder.stop();
                Recorder.release();

                if (audioManager != null) audioManager.setMode(PreviousAudioManagerMode);
                Log.i(LOG_TAG, "Audio Thread Stopped");
            }
        });
        AudioIoThread.start();
    }

    static private Thread UdpReceiveAudioThread1;
    static private Thread UdpReceiveAudioThread2;
    static private Thread UdpReceiveAudioThread3;
    static private Thread UdpReceiveAudioThread4;

    private boolean UdpVoipReceiveDataThreadRun = false;
    private void StartReceiveDataThread() {
        if ( UdpVoipReceiveDataThreadRun) return;

        UdpVoipReceiveDataThreadRun = true;

        int count = PhoneState.getInstance().getRemoteIPs().size();
        if (count >0) {
            UdpReceiveAudioThread1 = new Thread(new CCRunnable(0));
            UdpReceiveAudioThread1.start();
            count --;
        }
        if (count >0) {
            UdpReceiveAudioThread2 = new Thread(new CCRunnable(1));
            UdpReceiveAudioThread2.start();
            count --;
        }
        if (count >0) {
            UdpReceiveAudioThread3 = new Thread(new CCRunnable(2));
            UdpReceiveAudioThread3.start();
            count --;
        }
        if (count >0) {
            UdpReceiveAudioThread4 = new Thread(new CCRunnable(3));
            UdpReceiveAudioThread4.start();
            count --;
        }
    }

    int key  = -1;

    class AudioPlayer implements Runnable {

        LinkedBlockingQueue<byte[]> IncommingpacketQueue;
        AudioTrack outTrack;
        ImageView mImageView;
        int mID;
        public AudioPlayer(LinkedBlockingQueue<byte[]> queue,ImageView imageView, int ID) {
            IncommingpacketQueue = queue;
            mImageView = imageView;
            mID = ID;
        }

        @Override
        public void run() {
            // Create an instance of AudioTrack, used for playing back audio
            Log.i(LOG_TAG, "Player Thread Started. Thread id: " + Thread.currentThread().getId());
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
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
                int frame = 0;
                InputStream InputPlayFile = OpenSimVoice(mID);
                long systemTime = System.currentTimeMillis();
                int count = 0;
                while (AudioIoThreadThreadRun) {
                    frame++;
                    if (System.currentTimeMillis() - systemTime > 10000) {
                        Log.i(LOG_TAG, "Check play frame(" + mID + ")" + (count / 10) + " remaining - " + IncommingpacketQueue.size());
                        systemTime = System.currentTimeMillis();
                        count = 0;
                    } else {
                        count++;
                    }
//                    try {
//                        byte[] rawbuf = new byte[RAW_BUFFER_SIZE];
//                        int BytesRead;
//                        if (InputPlayFile != null) {
//                            BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
//                            if (BytesRead != RAW_BUFFER_SIZE) {
//                                InputPlayFile.close();
//                                InputPlayFile = OpenSimVoice(mID);
//                                BytesRead = InputPlayFile.read(rawbuf, 0, RAW_BUFFER_SIZE);
//                            }
//                        }
//                        outTrack.write(rawbuf, 0, RAW_BUFFER_SIZE);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    //    if (IncommingpacketQueue.size() > 0) {
                    byte[] AudioOutputBufferBytes = new byte[0];
                    AudioOutputBufferBytes = IncommingpacketQueue.take();
                    if (AudioOutputBufferBytes.length > 40) {
                        if (frame % 100 == 0) {
                            frame = 0;
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else {
                        if (frame % 100 == 0) {
                            frame = 0;
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setVisibility(View.INVISIBLE);

                                }
                            });
                        }
                    }
                    if (AudioOutputBufferBytes.length != RAW_BUFFER_SIZE) {
                        byte[] rawbuf =decodeAudio(AudioOutputBufferBytes, 0, AudioOutputBufferBytes.length);
                        outTrack.write(rawbuf, 0, RAW_BUFFER_SIZE);
                    } else  {
                        outTrack.write(AudioOutputBufferBytes, 0, RAW_BUFFER_SIZE);
                    }

                }
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "InterruptedException", e);
                    } finally {
                        outTrack.stop();
                        outTrack.flush();
                        outTrack.release();
                    }
            }
        }

            private InputStream OpenSimVoice(int SimVoice) {
                InputStream VoiceFile = null;
                switch (SimVoice) {
//            case 0:
//                break;
                    case 0:
                        VoiceFile = mContext.getResources().openRawResource(R.raw.t18k16bit);
                        break;
                    case 1:
                        VoiceFile = mContext.getResources().openRawResource(R.raw.t28k16bit);
                        break;
                    case 2:
                        VoiceFile = mContext.getResources().openRawResource(R.raw.t38k16bit);
                        break;
                    case 3:
                        VoiceFile = mContext.getResources().openRawResource(R.raw.t48k16bit);
                        break;
                    default:
                        break;
                }
                return VoiceFile;
            }
//
            protected synchronized byte[] decodeAudio(byte[] data, int offset, int length) {
                byte[] result = mCodec.decode(data, offset, length);
                return  result;
            }

            class CCRunnable implements Runnable {
                DatagramSocket recvAudioUdpSocket;
                byte [] mBuffer = new byte[200];
                int mIndex;
                boolean self;
                LinkedBlockingQueue<byte[]> IncommingpacketQueue = new LinkedBlockingQueue<>(20);
                public CCRunnable(int index) {
                    mQueueList.add(IncommingpacketQueue);
                    mIndex = index;
                    self = (PhoneState.getInstance().myIndex(mContext) -1 == mIndex);
                }

                @Override
                public void run() {
                    if (self) {
                        Log.i(LOG_TAG, "self finished receiver");
                        return;
                    }
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId() + " for "+ (NetworkConstants.VOIP_AUDIO_UDP_PORT+mIndex));
                    try {
                        // Setup socket to receive the audio data
                        recvAudioUdpSocket = new DatagramSocket(new InetSocketAddress(NetworkConstants.VOIP_AUDIO_UDP_PORT+mIndex));
                        recvAudioUdpSocket.setReuseAddress(true);
                        //recvAudioUdpSocket.bind();

                        long systemTime = System.currentTimeMillis();
                        int count=0;
                        while (UdpVoipReceiveDataThreadRun) {
                            if (System.currentTimeMillis() - systemTime > 10000) {
                                Log.i(LOG_TAG, "Check received frame("+mIndex+")"+ (count /10));
                                systemTime = System.currentTimeMillis();
                                count= 0;
                            } else {
                                count++;
                            }

                            DatagramPacket packet = new DatagramPacket(mBuffer, mBuffer.length);
                            recvAudioUdpSocket.receive(packet);

                            byte[] audio = Arrays.copyOf(packet.getData(), packet.getLength());

                            if (IncommingpacketQueue.remainingCapacity() > 1) {
                                IncommingpacketQueue.add(audio);
                            } else {
                                IncommingpacketQueue.remove();
                                IncommingpacketQueue.add(audio);
                            }
                        }
                    } catch (SocketException e) {
                        Log.e(LOG_TAG, "SocketException: " ,e);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "IOException: " ,e);
                    } finally {
                        if (recvAudioUdpSocket!=null) {
                            recvAudioUdpSocket.disconnect();
                            recvAudioUdpSocket.close();
                        }
                        recvAudioUdpSocket = null;
                    }
                }
            }

            private void udpSend(final byte[] bytes, final InetAddress remoteIp, DatagramSocket socket) {
                        if (remoteIp.getHostAddress().equals(PhoneState.getInstance().getPreviousIP(mContext))) {
                            return;
                        }
                        try {
                            int index = PhoneState.getInstance().myIndex(mContext)-1;
                            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteIp, (NetworkConstants.VOIP_AUDIO_UDP_PORT+index));
                            socket.send(packet);
                        } catch (SocketException e) {
                            Log.e(LOG_TAG, "Failure. SocketException in UdpSend: " + e);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failure. IOException in UdpSend: " + e);
                        }
                    }

        }


