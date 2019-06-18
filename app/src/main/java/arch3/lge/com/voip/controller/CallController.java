package arch3.lge.com.voip.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import arch3.lge.com.voip.model.UDPnetwork.ICallController;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;

public class CallController implements ICallController {

    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    static public void requestCall(Context context, String phoneNumber, ImageView self) {
        JSONObject object = param.getIP(phoneNumber);
        VoIPVideoIo io = new VoIPVideoIo();
        io.StartVideo(self);
        serverApi.getIP(context, object,io);
    }

    static public void acceptCall(String phonenumber) {


    }

    static public void rejectCall(String phonenumber) {

    }

    static public void terminateCall(String phonenumber) {

    }

    static public void incomingCall(String phonenumber) {

    }

    static public void endCall(String phonenumber) {

    }

    static public void startCall(String phonenumber) {

    }

    public void finish () {
        CallController.endCall("AAAA");
    }
}
