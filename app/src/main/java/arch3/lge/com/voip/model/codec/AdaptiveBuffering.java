package arch3.lge.com.voip.model.codec;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public class AdaptiveBuffering {
    private final static String LOG_TAG = "AdaptiveBuffering";

    private final static int HDR_SIZE = Short.BYTES + Integer.BYTES;
    private static short mSequenceNumber = 0;
    private LinkedBlockingQueue<byte[]> mPacketQueue = new LinkedBlockingQueue<>(1024);
    private int mQueueCapacity = 16;
    private int mLastSequence = 0;
    private byte [] mLastPacket;

    private final static long COEF_A = 100;
    private final static long COEF_B = 4;
    private long mAveDi = 0;
    private long mAveVi = 0;
    private final int CAPACITY_MIN = 10;
    private final int CAPACITY_MAX = 20;
    private long mPacketLoss = 0;
    private static long mPacketLossGlobal = 0;
    private final static long PACKET_LOSS_SAMPLING = 1000;
    public final static long MIN_PACKET_LOSS = 80;
    public final static long MAX_PACKET_LOSS = 120;

    static public long getPacketLoss() { return mPacketLossGlobal; }

    static byte [] writeHeader(byte [] data){
        ByteBuffer byteBuffer = ByteBuffer.allocate(HDR_SIZE + data.length);
        byteBuffer.putShort(mSequenceNumber);
        mSequenceNumber ++;
        byteBuffer.putInt((int)System.currentTimeMillis());
        byteBuffer.put(data);
        return byteBuffer.array();
    }

    int readHeader(byte [] data){
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, 0, HDR_SIZE);

        short index = byteBuffer.getShort();

        int delay = (int)System.currentTimeMillis() - byteBuffer.getInt();
//        Log.d(LOG_TAG, "indx ="+ index+  " delay ="+ delay);
        calculateBufferSize(delay);
        calculatePacketLoss(index);
        mLastSequence = index;
        Log.d(LOG_TAG, "indx ="+ index+  " delay ="+ delay+" aveDi ="+ mAveDi+  " aveVi ="+ mAveVi + " => "+ mPacketLoss);
        return HDR_SIZE;
    }

    private void calculateBufferSize(int delay){
        mAveDi = (mAveDi*(COEF_A-1) + delay) / COEF_A;
        mAveVi = (mAveVi*(COEF_A-1) + Math.abs(mAveDi - delay)) / COEF_A;
        int nBuffering = (int)(40 + COEF_B*mAveVi)/10;
        if((mLastSequence % 1000) == 0){
            mQueueCapacity = nBuffering;
            if (mQueueCapacity >= CAPACITY_MAX)
                mQueueCapacity = CAPACITY_MAX;
            else if(mQueueCapacity < CAPACITY_MIN)
                mQueueCapacity = CAPACITY_MIN;
            Log.d(LOG_TAG, "Capacity  update ="+ mQueueCapacity);
        }
    }

    private void calculatePacketLoss(short index){
        if(index - mLastSequence <= 0 ){
            Log.d(LOG_TAG, "Revert sequence : index = "+ index + ", last = "+mLastSequence);
            return;
        }
        if(index - mLastSequence > 10){
            Log.d(LOG_TAG, "Too big sequence gap : index = "+ index + ", last = "+mLastSequence);
            return;
        }

        int iCurr = mLastSequence + 1;
        while(iCurr < index){
            mPacketLoss = (mPacketLoss*(PACKET_LOSS_SAMPLING-1) + 10000) / PACKET_LOSS_SAMPLING;

            if(mPacketLoss < 100){    //1%
                Log.d(LOG_TAG, "packet missing  => replace to last");
                mPacketQueue.add(mLastPacket);
            }
            else if(mPacketLoss < 200){
                Log.d(LOG_TAG, "packet missing  => replace to silent");
               // mPacketQueue.add(mLastPacket);
            }
            iCurr++;
        }
        mPacketLoss = (mPacketLoss*(PACKET_LOSS_SAMPLING-1) + 0) / PACKET_LOSS_SAMPLING;
        mPacketLossGlobal = ((mPacketLossGlobal * 2) + mPacketLoss) / 3;
    }

    void addQueue(byte [] data){
        mLastPacket = data;
        mPacketQueue.add(data);
        if(mPacketQueue.size( )> mQueueCapacity){
            getQueue();
        }
    }

    byte [] getQueue(){
        try{
            if(mPacketQueue.isEmpty())
                return null;
            return mPacketQueue.remove();
        }
        catch(NoSuchElementException e){
            //e.printStackTrace();
        }
        return null;
    }
    byte [] take() throws InterruptedException
    { return getQueue();}

    boolean isEmpty(){
        return mPacketQueue.isEmpty();
    }
    void reset(){
        mPacketQueue.clear();
    }
    void clear(){
        mPacketQueue.clear();
    }
    int size() { return mPacketQueue.size(); }
}
