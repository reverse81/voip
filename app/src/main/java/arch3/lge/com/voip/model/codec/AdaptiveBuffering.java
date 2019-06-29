package arch3.lge.com.voip.model.codec;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public class AdaptiveBuffering {
    private final static String LOG_TAG = "AdaptiveBuffering";

    private final static int HDR_SIZE = Integer.BYTES + Long.BYTES;
    private static int mSequenceNumber = 0;
    private LinkedBlockingQueue<byte[]> mPacketQueue = new LinkedBlockingQueue<>(1024);
    private int mQueueCapacity = 16;
    private int mLastSequence = 0;
    private byte [] mLastPacket;

    private final static long COEF_A = 100;
    private final static long COEF_B = 4;
    private long mAveDi = 0;
    private long mAveVi = 0;
    private final int CAPACITY_MIN = 4;
    private final int CAPACITY_MAX = 20;

    static byte [] writeHeader(byte [] data){
        ByteBuffer byteBuffer = ByteBuffer.allocate(HDR_SIZE + data.length);
        byteBuffer.putInt(mSequenceNumber);
        mSequenceNumber ++;
        byteBuffer.putLong(System.currentTimeMillis());
        byteBuffer.put(data);
        return byteBuffer.array();
    }

    int readHeader(byte [] data){
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, 0, HDR_SIZE);

        int index = byteBuffer.getInt();
        if(index == mLastSequence + 2){
            Log.d(LOG_TAG, "packet missing  ="+ (index-1));
            if(mLastPacket != null) {
                Log.d(LOG_TAG, "repleace to  last packet");
                mPacketQueue.add(mLastPacket);
                mLastPacket = null;
            }
        }
        mLastSequence = index;

        long delay = System.currentTimeMillis() - byteBuffer.getLong();
        calcBufferSize(index, delay);
        return HDR_SIZE;
    }

    private void calcBufferSize(int index, long delay){
        Log.d(LOG_TAG, "indx ="+ index+  " delay ="+ delay);
        mAveDi = (mAveDi*(COEF_A-1) + delay) / COEF_A;
        mAveVi = (mAveVi*(COEF_A-1) + Math.abs(mAveDi - delay)) / COEF_A;
        long Pi = mAveDi + COEF_B*mAveVi;
        Log.d(LOG_TAG, "Di ="+ mAveDi+  " Vi ="+ mAveVi + " => "+ Pi);
        if((mLastSequence % 1000) == 0){
            mQueueCapacity = (int)Pi / 10;
            if (mQueueCapacity >= CAPACITY_MAX)
                mQueueCapacity = CAPACITY_MAX;
            else if(mQueueCapacity < CAPACITY_MIN)
                mQueueCapacity = CAPACITY_MIN;
            Log.d(LOG_TAG, "Capacity  update ="+ mQueueCapacity);
        }
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

    boolean isEmpty(){
        return mPacketQueue.isEmpty();
    }
    void reset(){
        mPacketQueue.clear();
    }
}
