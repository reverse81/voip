package arch3.lge.com.voip.controller;

import android.content.Context;
import android.content.Intent;

import java.util.Observable;
import java.util.Observer;

import arch3.lge.com.voip.model.UDPnetwork.UDPCmd;
import arch3.lge.com.voip.ui.RequestCallActivity;

public class RequestCallObserver implements Observer {
    Context context;

    RequestCallObserver(Context context) {
        this.context = context;
    }

    @Override
    public void update(Observable observable, Object o) {
        //connect UDP

        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), UDPCmd.class.getName());
        intent.putExtra("message", "/CALLIP/");
        intent.putExtra("sender", "IPAFASDFAdf");
        context.startService(intent);
    }
}
