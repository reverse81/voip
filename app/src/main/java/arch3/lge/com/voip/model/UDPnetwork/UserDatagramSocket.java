package arch3.lge.com.voip.model.UDPnetwork;


import android.util.Log;

import java.io.*;
import java.net.*;
public class UserDatagramSocket {

    private static final String LOG_TAG = "VoIPAudioIo:SOCKET";
    private DatagramSocket mSendSock;
    private DatagramSocket mRecvSock;
    private int mPort = 0;
    private InetAddress mAddress;
    private Thread mThread = null;
    private boolean mThreadRun = true;
    private byte [] mBuffer = new byte[8*1024];

    public interface PacketDataHandler {
        public void onReceive(DatagramPacket packet);
    }
    PacketDataHandler mHander = null;

    public UserDatagramSocket(int port){
        mPort = port;
    }

    public void setAddress(InetAddress address){
        mAddress = address;
    }

    public void setAddress(String address){
        try {
            mAddress = InetAddress.getByName(address);
        }catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
        }
    }

    public void send(byte[] buffer)
    {
        try {
            if (mSendSock == null) {
                mSendSock = new DatagramSocket();
            }
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, mAddress, mPort);
            mSendSock.send(request);

            //Log.e(TAG, "send = ok"+ mAddress);
        }catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public  void closeSendSocket() {
        if (mSendSock != null) {
            mSendSock.disconnect();
            mSendSock.close();
            mSendSock = null;
        }

    }

    public boolean setListener(PacketDataHandler handler ) {
        mHander = handler;
        if(handler == null)
            return stopReceive();
        return startReceive();
    }

    private boolean startReceive(){
        if(mPort <= 0)
            return  false;
        if(mThread != null && mThread.isAlive())
            return false;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    mRecvSock = new DatagramSocket(null);
                    mRecvSock.setReuseAddress(true);
                    mRecvSock.bind(new InetSocketAddress(mPort));
                    long systemTime = System.currentTimeMillis();
                    int count=0;

                    while (mThreadRun) {
                        DatagramPacket packet = new DatagramPacket(mBuffer, mBuffer.length);
                        mRecvSock.receive(packet);

                        if (System.currentTimeMillis() - systemTime > 10000) {
                            Log.i(LOG_TAG, "Check voice received frame : "+ count/10 );
                            systemTime = System.currentTimeMillis();
                            count= 0;
                        } else {
                            count++;
                        }

                        if(mHander != null){
                            mHander.onReceive(packet);
                        }
                    }
                } catch (SocketException e) {
                    mThreadRun = false;
                    Log.e(LOG_TAG, "SocketException: ",e);
                } catch (IOException e) {
                    mThreadRun = false;
                    Log.e(LOG_TAG, "IOException: ",e);
                } finally {
                    mRecvSock.disconnect();
                    mRecvSock.close();
                }
            }
        });
        mThread.start();
        return true;
    }
    private boolean stopReceive(){
        if(mThread == null)
            return true;
        mThread.interrupt();
        mThread = null;
        return true;
    }
}
