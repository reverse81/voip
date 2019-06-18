package arch3.lge.com.voip.model.UDPnetwork;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import java.io.*;
import java.net.*;
public class UserDatagramSocket {
    private static final String TAG = "UDPSocket";
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

            Log.e(TAG, "send = ok"+ mAddress);
        }catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
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
                Log.i(TAG, "Receive Data Thread Started. Thread id: " + Thread.currentThread().getId());
                try {
                    // Setup socket to receive the audio data
                    mRecvSock = new DatagramSocket(null);
                    mRecvSock.setReuseAddress(true);
                    mRecvSock.bind(new InetSocketAddress(mPort));

                    while (mThreadRun) {
                        DatagramPacket packet = new DatagramPacket(mBuffer, mBuffer.length);
                        mRecvSock.receive(packet);
                        if(mHander != null){
                            mHander.onReceive(packet);
                        }
                    }
                    // close socket
                    mRecvSock.disconnect();
                    mRecvSock.close();
                } catch (SocketException e) {
                    mThreadRun = false;
                    Log.e(TAG, "SocketException: " + e.toString());
                } catch (IOException e) {
                    mThreadRun = false;
                    Log.e(TAG, "IOException: " + e.toString());
                }
            }
        });
        mThread.start();
        return true;
    }
    private boolean stopReceive(){
        if(mThread == null)
            return true;
        mThreadRun = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "UdpReceiveDataThread Join interruped");
        }
        mThread = null;
        return true;
    }
}
