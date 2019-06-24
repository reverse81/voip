package arch3.lge.com.voip.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import org.json.JSONObject;

import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;

public class CallController {

    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    static public void requestCall(Context context, String phoneNumber, ImageView self) {
        JSONObject object = param.getPhoneParam(phoneNumber);
        VoIPVideoIo io = VoIPVideoIo.getInstance();
        io.StartVideo(self);
        serverApi.getIP(context, object,io);
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
    }

    static public void endCall(Context context) {
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
        intent.setAction(TCPCmd.GUI_VOIP_CTRL);
        intent.putExtra("message", "/END_CALL_BUTTON/");
        intent.putExtra("sender", PhoneState.getInstance().getRemoteIP());
        context.startService(intent);
    }

    static private Activity mCurrent;

    static  public void setCurrent(Activity current) {
        mCurrent = current;
    }

    static public void finish () {
        if (mCurrent != null) {
            mCurrent.finish();
        }
        mCurrent = null;
    }
}
