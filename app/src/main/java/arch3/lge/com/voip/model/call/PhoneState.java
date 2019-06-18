package arch3.lge.com.voip.model.call;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Observable;

public class PhoneState extends Observable {
    public enum CallState {LISTENING, CALLING, INCOMMING, INCALL}
    public enum VideoState {START_VIDEO, RECEIVING_VIDEO, STOP_VIDEO,VIDEO_STOPPED}
    private CallState CallStatel = CallState.LISTENING;
    private VideoState  RecVideoState = VideoState.VIDEO_STOPPED;
    private String RemoteIP;
    private String LocalIP;
    private String InComingIP;
    private String CmdIP;
    private Boolean RingerEnabled;
    private Boolean BoostEnabled;
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

    public void SetPhoneState(CallState callstate) {
        CallStatel = callstate;
    }

    public  CallState GetPhoneState() {
        return CallStatel;
    }

    public void SetRecvVideoState(VideoState videostate) { RecVideoState = videostate; }

    public VideoState GetRecvVideoState() { return RecVideoState; }

    public void SetInComingIP(String value) {
        InComingIP = value;
    }

    public String GetInComingIP() {
        return InComingIP;
    }

    public void SetCmdIP(String value) {
        CmdIP = value;
    }

    public  String GetCmdIP() {
        return CmdIP;
    }

    public void SetRemoteIP(String value) {
        RemoteIP = value;
    }

    public String GetRemoteIP() {
        return RemoteIP;
    }

    public  void SetLocallP(String value) {
        LocalIP = value;
    }

    public  String GetLocalIP() {
        return LocalIP;
    }

    public   void SetRinger(Boolean value) {
        RingerEnabled = value;
    }

    public  Boolean GetRinger() {
        return RingerEnabled;
    }

    public  void SetMic(Boolean value) {
        MicEnabled = value;
    }

    public  Boolean GetMic() {
        return MicEnabled;
    }

    public void SetBoost(Boolean value) {
        BoostEnabled = value;
    }

    public   Boolean GetBoost() {
        return BoostEnabled;
    }

    public  void NotifyUpdate() {
        setChanged();
        notifyObservers();
    }

    public static void setCurrentIP(Context context, String ip) {
        SharedPreferences.Editor editor = context.getSharedPreferences("network", Context.MODE_PRIVATE).edit();
        editor.putString("ip", ip);
        editor.commit();
    }

    public static String getPreviousIP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("network", Context.MODE_PRIVATE);
        return sharedPreferences.getString("ip","");

    }
}