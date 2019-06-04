package arch3.lge.com.voip.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

public class Util {
    public final static String LOGTAG = "Util";

    static public void safetyClose(Closeable socket) {
        if (socket!=null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.w(LOGTAG, "Socket close error",e);
            }
        }

    }
}
