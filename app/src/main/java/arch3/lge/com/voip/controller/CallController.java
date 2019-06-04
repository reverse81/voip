package arch3.lge.com.voip.controller;

import arch3.lge.com.voip.model.UDPnetwork.ICallController;

public class CallController implements ICallController {

    static public void requestCall(String phonenumber) {

        //get IP address

        //connect UDP

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
