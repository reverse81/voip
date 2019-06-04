package arch3.lge.com.voip.listener;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Locale;

import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.utils.UDPContstants;
import arch3.lge.com.voip.utils.Util;

public class UDPListenerService extends Service {
    private static final String LOG_TAG = "UDPListenerService";
    private static final int BUFFER_SIZE = 128;
    private boolean UdpListenerThreadRun = false;

    private DatagramSocket socket;


    private void startListenerForUDP() {
        UdpListenerThreadRun = true;
        Thread UDPListenThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // Setup the socket to receive incoming messages
                    byte[] buffer = new byte[BUFFER_SIZE];
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(UDPContstants.CONTROL_DATA_PORT));
                    DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                    Log.i(LOG_TAG, "Incoming call listener started");
                    while (UdpListenerThreadRun) {
                        // Listen for incoming call requests
                        Log.i(LOG_TAG, "Listening for incoming calls");
                        socket.receive(packet);
                        String senderIP = packet.getAddress().getHostAddress();
                        String message = new String(buffer, 0, packet.getLength());
                        Log.i(LOG_TAG, "Got UDP message from " + senderIP + ", message: " + message);
                        ProcessReceivedUdpMessage(senderIP, message);
                        if (message.equals("/CALLIP/")) {
                            Log.e(LOG_TAG, "Main activity may not be running so kick it");
                            Intent i = new Intent();
                            i.setClassName("lg.dplakosh.lgvoipdemo", "lg.dplakosh.lgvoipdemo.MainActivity");
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }
                    Log.e(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();

                } catch (Exception e) {
                    UdpListenerThreadRun = false;
                    Log.e(LOG_TAG, "no longer listening for UDP messages due to error " + e.getMessage());
                }
            }
        });
        UDPListenThread.start();
    }

    private void ProcessReceivedUdpMessage(final String Sender, String MessageIn) {

        switch (MessageIn) {

            case "/CALLIP/":
                // Receives Call Requests
                CallController.incomingCall("AAA");
                break;
            case "/ANSWER/":
                // Accept notification received. Start call
                CallController.startCall("AAA");
                break;

            case "/REFUSE/":
            case "/ENDCALL/":
                CallController.endCall("AAA");
                break;

            default:
                // Invalid notification received
                Log.w(LOG_TAG, Sender + " sent invalid message: " + MessageIn);
                break;
        }
    }

    private void stopListen() {
        UdpListenerThreadRun = false;
        Util.safetyClose(socket);
    }

    @Override
    public void onCreate() {
        super.onCreate();


        int LocalIpAddressBin = 0;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            LocalIpAddressBin = wifiInfo.getIpAddress();
            DeviceContorller.setLocalIP(String.format(Locale.US, "%d.%d.%d.%d", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff), (LocalIpAddressBin >> 24 & 0xff)));
            startListenerForUDP();
            Log.i(LOG_TAG, "Service started");
        } else {

            //start volte
            Log.i(LOG_TAG, "Service started");
        }
    }

    @Override
    public void onDestroy() {
        stopListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}