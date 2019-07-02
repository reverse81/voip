package arch3.lge.com.voip.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIo;
import arch3.lge.com.voip.model.codec.VoIPAudioIoCC;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIoCC;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.ui.BaseCallActivity;
import arch3.lge.com.voip.ui.ConferenceCallingActivity;
import arch3.lge.com.voip.ui.RequestCallActivity;

public class CallController {
    public final static String LOG_TAG = "VoIP:CallController";
    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    static public void requestCall(Context context, String phoneNumber, ImageView self) {
        JSONObject object = param.getPhoneParam(phoneNumber);
        VoIPVideoIo io = VoIPVideoIo.getInstance(context);
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

        mCurrent.StopReceiveVideoThread();
        VoIPVideoIo.getInstance(context).EndVideo();
    }

    static public void endCall(Context context) {
        if (PhoneState.getInstance().getCallState() != PhoneState.CallState.BUSY) {
            Intent intent = new Intent();
            intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
            intent.setAction(TCPCmd.GUI_VOIP_CTRL);
            intent.putExtra("message", "/END_CALL_BUTTON/");
            intent.putExtra("sender", PhoneState.getInstance().getRemoteIP());
            context.startService(intent);
        }
        mCurrent.StopReceiveVideoThread();
        VoIPVideoIo.getInstance(context).EndVideo();
    }

    static public void startCCCall(ConferenceCallingActivity context, String phoneNumber) {
        PhoneState.getInstance().setCallState(PhoneState.CallState.CALLING);
        JSONObject object = param.getPhoneParam(phoneNumber);
        serverApi.getIPforCC(context, object);
    }

    static public void endCCCall(ConferenceCallingActivity ccActivity) {
        PhoneState.getInstance().setCallState(PhoneState.CallState.LISTENING);
        ccActivity.StopReceiveVideoThread();
        VoIPVideoIoCC.getInstance(ccActivity).EndVideo();
        VoIPAudioIoCC.getInstance(ccActivity).EndAudio();
        ccActivity.finish();

    }

    static public void busy () {
        if (mCurrent!=null && mCurrent instanceof RequestCallActivity) {
            ((RequestCallActivity)mCurrent).changeTone();
        }
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
        PhoneState.getInstance().setCallState(PhoneState.CallState.LISTENING);
        if (mCurrent != null) {
            VoIPVideoIo.getInstance(mCurrent).setBanned(false);
            DeviceContorller.initDevice(mCurrent);
            mCurrent.StopReceiveVideoThread();
            VoIPVideoIo.getInstance(mCurrent).EndVideo();
            VoIPAudioIo.getInstance(mCurrent).EndAudio();
            mCurrent.finish();
        }
        mCurrent = null;
    }
}
