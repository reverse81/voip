package arch3.lge.com.voip.model.codec;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class AdaptiveBuffering {
    private final static String LOG_TAG = "AdaptiveBuffering";

    private final static int HDR_SIZE = Integer.BYTES + Long.BYTES;
    private int mSequenceNumber = 0;
    private LinkedBlockingQueue<byte[]> mPacketQueue = new LinkedBlockingQueue<>(1024);
    private int mQueueCapacity = 16;

    byte [] writeHeader(byte [] data){
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
        long delay = System.currentTimeMillis() - byteBuffer.getLong();
        mQueueCapacity = calcBufferSize(index, delay);
        return HDR_SIZE;
    }

    private int calcBufferSize(int index, long delay){
        Log.d(LOG_TAG, "indx ="+ index+  " delay ="+ delay);
        return 20;
    }

    void addQueue(byte [] data){
        mPacketQueue.add(data);
        if(mPacketQueue.size( )> mQueueCapacity){
            mPacketQueue.remove();
        }
    }

    byte [] getQueue(){
        if (mPacketQueue.size() > 0) {
            return mPacketQueue.remove();
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
