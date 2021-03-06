package arch3.lge.com.voip.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver {

    static final String LOG_TAG = "VoIP:BootCompleted";

    @Override
    public void onReceive(Context context, Intent intent) {
        String ActionString = intent.getAction();
        if (ActionString != null) {
            if (ActionString.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Intent serviceIntent = new Intent(context, TCPListenerService.class);
                context.startService(serviceIntent);
                Log.e(LOG_TAG, "Started UDPListenerService.class");
            }
        }
    }
}
