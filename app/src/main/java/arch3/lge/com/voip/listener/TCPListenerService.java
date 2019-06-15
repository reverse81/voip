package arch3.lge.com.voip.listener;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.controller.DeviceContorller;
import arch3.lge.com.voip.ui.CallingActivity;
import arch3.lge.com.voip.ui.ReceivedCallActivity;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

public class TCPListenerService extends Service {
    private static final String LOG_TAG = "UDPListenerService";
    private static final int BUFFER_SIZE = 128;
    private boolean UdpListenerThreadRun = false;

    private ServerSocket serverSocket;


    private void startListenerForTCP() {
        UdpListenerThreadRun = true;
        Thread UDPListenThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket socket = null;
                    // Setup the socket to receive incoming messages
                    byte[] buffer = new byte[BUFFER_SIZE];
                    serverSocket = new ServerSocket(NetworkConstants.CONTROL_DATA_PORT);
//                    serverSocket.setReuseAddress(true);
//                    serverSocket.bind(new InetSocketAddress(NetworkContstants.CONTROL_DATA_PORT));
                   // DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                    Log.i(LOG_TAG, "Incoming call listener started");
                    while (UdpListenerThreadRun) {
                        // Listen for incoming call requests
                        Log.i(LOG_TAG, "Listening for incoming calls");

                        socket = serverSocket.accept();

                        new CommunicationThread(socket).run();
                    }
                    Log.e(LOG_TAG, "Call Listener ending");


                } catch (Exception e) {
                    UdpListenerThreadRun = false;
                    Log.e(LOG_TAG, "no longer listening for UDP messages due to error " + e.getMessage());
                }
            }
        });
        UDPListenThread.start();
    }

    private void ProcessReceivedUdpMessage(final String Sender, String MessageIn) {
        Intent intent = new Intent();
        switch (MessageIn) {

            case "/CALLIP/":
                // Receives Call Requests

                intent.setClassName(this.getPackageName(), ReceivedCallActivity.class.getName());
                this.startService(intent);

                CallController.incomingCall("AAA");
                break;
            case "/ANSWER/":
                // Accept notification received. Start call
                //finish activity??
                intent.setClassName(this.getPackageName(), CallingActivity.class.getName());
                this.startService(intent);

                CallController.startCall("AAA");
                break;

            case "/REFUSE/":
            case "/ENDCALL/":

                //finish activity??
//                intent.setClassName(this.getPackageName(), CallingActivity.class.getName());
//                this.startService(intent);
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
        Util.safetyClose(serverSocket);
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
            startListenerForTCP();
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

    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (read == null ){
                        Thread.currentThread().interrupt();
                    }else{
                        String senderIP = clientSocket.getInetAddress().getHostAddress();
                        ProcessReceivedUdpMessage(senderIP, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Util.safetyClose(clientSocket);
        }

    }
}