package arch3.lge.com.voip.model.call;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;

public class PhoneState { //extends Observable {

    public enum CallState {LISTENING, CALLING, INCALL, BUSY }
    public enum VideoState {START_VIDEO, RECEIVING_VIDEO, STOP_VIDEO,VIDEO_STOPPED}
    private CallState mCallState = CallState.LISTENING;
    private VideoState  RecVideoState = VideoState.VIDEO_STOPPED;
    private String RemoteIP;
    private ArrayList<String> RemoteIPs;
   // private String LocalIP;
    private String InComingIP;
    private String CmdIP;
   // private Boolean RingerEnabled;
   // private Boolean BoostEnabled;
    private Boolean MicEnabled;

    public static int getUpdatingIP() {
        return UpdatingIP;
    }

    public static void setUpdatingIP(int updatingIP) {
        UpdatingIP = updatingIP;
    }

    private static int UpdatingIP;


    private static PhoneState instance = new PhoneState();

    public static PhoneState getInstance() {
        return instance;
    }

    private PhoneState() {
    }

    public void setCallState(CallState callstate) {
        mCallState = callstate;
    }

    public  CallState getCallState() {
        return mCallState;
    }

    public void SetRecvVideoState(VideoState videostate) { RecVideoState = videostate; }

    public VideoState GetRecvVideoState() { return RecVideoState; }

    public void setRemoteIP(String value) {
        RemoteIP = value;
    }

    public String getRemoteIP() {
        return RemoteIP;
    }

    public void setRemoteIPs(ArrayList<String> value) {
        RemoteIPs = value;
    }

    public ArrayList<String> getRemoteIPs() {
        return RemoteIPs;
    }

    public int myIndex(Context context) {
        if (RemoteIPs == null) {
            return  0;
        }
        for (int i = 0 ; i<RemoteIPs.size() ;i++) {
            if (getPreviousIP(context).equals(RemoteIPs.get(i))) {
                return i+1;
            }
        }
        return  0;
    }

    public void setCurrentIP(Context context, String ip) {
        SharedPreferences.Editor editor = context.getSharedPreferences("network", Context.MODE_PRIVATE).edit();
        editor.putString("ip", ip);
        editor.commit();
    }

    public String getPreviousIP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("network", Context.MODE_PRIVATE);
        return sharedPreferences.getString("ip","");
    }

}