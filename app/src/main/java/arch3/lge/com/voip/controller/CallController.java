package arch3.lge.com.voip.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import arch3.lge.com.voip.model.UDPnetwork.ICallController;
import arch3.lge.com.voip.model.UDPnetwork.UDPCmd;

public class CallController implements ICallController {



    static public void requestCall(Context context, String phonenumber) {
//
//        RequestCallObserver ob = new RequestCallObserver(context);
//        ServerApi serverApi = new ServerApi();
//        serverApi.addObserver(ob);
        //get IP address
        Log.i("AAAAAAAAAAAA","AAAAAAAAAAAAAAAA");
        String ip = phonenumber;
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), UDPCmd.class.getName());
        intent.setAction(UDPCmd.GUI_VOIP_CTRL);
        intent.putExtra("message", "/CALLIP/");
        intent.putExtra("sender", ip);
        context.startService(intent);
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
