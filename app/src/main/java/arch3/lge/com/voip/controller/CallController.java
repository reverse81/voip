package arch3.lge.com.voip.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.ui.BaseCallActivity;

public class CallController {
    public final static String LOG_TAG = "VoIP:CallController";
    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    static public void requestCall(Context context, String phoneNumber, ImageView self) {
        JSONObject object = param.getPhoneParam(phoneNumber);
        VoIPVideoIo io = VoIPVideoIo.getInstance();
        io.StartVideo(self);
        //serverApi.getIP(context, object,io);
        {
            String ip = "10.0.1.2";
            io.attachIP(ip);
            Intent intent = new Intent();
            intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
            intent.setAction(TCPCmd.GUI_VOIP_CTRL);
            intent.putExtra("message", "/CALL_BUTTON/");
            intent.putExtra("sender", ip);
            context.startService(intent);
        }
    }

    static public void acceptCall(Context context) {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
        intent.setAction(TCPCmd.GUI_VOIP_CTRL);
        intent.putExtra("message", "/ANSWER_CALL_BUTTON/");
        intent.putExtra("sender", PhoneState.getInstance().getRemoteIP());
        context.startService(intent);
    }

    static public void rejectCall(Context context) {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
        intent.setAction(TCPCmd.GUI_VOIP_CTRL);
        intent.putExtra("message", "/REFUSE_CALL_BUTTON/");
        intent.putExtra("sender", PhoneState.getInstance().getRemoteIP());
        context.startService(intent);

        mCurrent.StopReceiveVideoThread();
        VoIPVideoIo.getInstance().EndVideo();
    }

    static public void endCall(Context context) {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
        intent.setAction(TCPCmd.GUI_VOIP_CTRL);
        intent.putExtra("message", "/END_CALL_BUTTON/");
        intent.putExtra("sender", PhoneState.getInstance().getRemoteIP());
        context.startService(intent);

        mCurrent.StopReceiveVideoThread();
        VoIPVideoIo.getInstance().EndVideo();
    }

    static private BaseCallActivity mCurrent;

    static  public void setCurrent(BaseCallActivity current) {
        Log.i(LOG_TAG,"changed current");
        mCurrent = current;
    }
    static  public BaseCallActivity getCurrent() {
        return mCurrent;
    }

    static public void finish () {
        if (mCurrent != null) {
            DeviceContorller.initDevice(mCurrent);

            Log.i(LOG_TAG,"WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
            mCurrent.StopReceiveVideoThread();
            VoIPVideoIo.getInstance().EndVideo();
            Log.i(LOG_TAG,"WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
            VoIPAudioIo.getInstance(mCurrent).EndAudio();
            Log.i(LOG_TAG,"WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
            mCurrent.finish();
        }
        mCurrent = null;
    }
}
