package arch3.lge.com.voip.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.Locale;

import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.model.user.User;


public class WifiReceiver  extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent ==null) {
            return;
        }

        if ("".equals(User.getLogin(context))) {
            return;
        }

        Log.i("REeeee","RRRRRRRRRRRRRRRRRRRRRRr " + intent.toString());


        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            int LocalIpAddressBin = 0;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            LocalIpAddressBin = wifiInfo.getIpAddress();
            if (LocalIpAddressBin != 0) {
                String ip = String.format(Locale.US, "%d.%d.%d.%d", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff), (LocalIpAddressBin >> 24 & 0xff));
                Log.i("REeeee","we have ip "+ip +"   "  + wifiInfo.toString());
                if (!PhoneState.getPreviousIP(context).equals(ip)) {
                    if (PhoneState.getUpdatingIP() != LocalIpAddressBin ) {
                        Log.i("REeeee","we try update once ");
                        PhoneState.setUpdatingIP(LocalIpAddressBin);
                        ApiParamBuilder param = new ApiParamBuilder();
                        ServerApi serverApi = new ServerApi();
                        String phoneNumber = User.getPhoneNumber(context);
                        JSONObject object = param.setIP(ip, phoneNumber);
                        serverApi.setIP(context, object, ip);
                    }

                }
            } else {
                Log.i("REeeee","No ip");
            }
        }

    }
}
