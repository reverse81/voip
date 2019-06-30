package arch3.lge.com.voip.listener;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.database.ConferenceDatabaseHelper;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.model.user.User;
import arch3.lge.com.voip.ui.BaseCallActivity;
import arch3.lge.com.voip.ui.CallingActivity;
import arch3.lge.com.voip.ui.ReceivedCallActivity;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

public class TCPListenerService extends Service {
    private static final String LOG_TAG = "VoIP:TCPListenerService";
    private static final int BUFFER_SIZE = 128;
    private boolean UdpListenerThreadRun = false;

    private ServerSocket serverSocket;
    private  static ApiParamBuilder param = new ApiParamBuilder();
    private static ServerApi serverApi = new ServerApi();

    private void startListenerForTCP() {
        UdpListenerThreadRun = true;
        Thread UDPListenThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket socket = null;
                    // Setup the socket to receive incoming messages
                   // byte[] buffer = new byte[BUFFER_SIZE];
                    serverSocket = new ServerSocket(NetworkConstants.CONTROL_DATA_PORT);
                    serverSocket.setReuseAddress(true);
//                    serverSocket.bind(new InetSocketAddress(NetworkContstants.CONTROL_DATA_PORT));
                   // DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                    Log.i(LOG_TAG, "Incoming call listener started");
                    while (UdpListenerThreadRun) {
                        // Listen for incoming call requests
                        Log.i(LOG_TAG, "Listening for incoming calls");

                        socket = serverSocket.accept();
                        socket.setReuseAddress(true);

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

    // {"phoneNumber":"08055461638","schedule":{"from":"2019-06-25T16:30:00.000Z","to":"2019-06-25T16:50:00.000Z"}}
    private void ProcessReceivedUdpMessage(final String sender, String message) {

        MyEncrypt encrypt = new MyEncrypt();
        String messageIn = encrypt.decrypt(message);

        Intent intent = new Intent();
        Log.w(LOG_TAG, sender + " sent message: " +message + ":"+messageIn);

        switch (messageIn) {

            case "/CALLIP/":
                // Receives Call Requests
                if (PhoneState.getInstance().getCallState() == PhoneState.CallState.LISTENING) {
                    PhoneState.getInstance().setRemoteIP(sender);
                    PhoneState.getInstance().setCallState(PhoneState.CallState.CALLING);
                    intent.setClass(this, ReceivedCallActivity.class);
                    this.startActivity(intent);
                } else {
                    intent.setClassName(this.getPackageName(), TCPCmd.class.getName());
                    intent.setAction(TCPCmd.GUI_VOIP_CTRL);
                    intent.putExtra("message", "/BUSY_SIGNAL/");
                    intent.putExtra("sender", sender);
                    this.startService(intent);
                }

                break;
            case "/ANSWER/":
                // Accept notification received. Start call
                //finish activity??
                PhoneState.getInstance().setCallState(PhoneState.CallState.INCALL);
                BaseCallActivity current =  CallController.getCurrent();
                intent.setClass(this, CallingActivity.class);
                this.startActivity(intent);
                current.finish();

               // CallController.startCall("AAA");
                break;

            case "/REFUSE/":
            case "/ENDCALL/":
                if (sender.equals(PhoneState.getInstance().getRemoteIP())) {
                    CallController.finish();
                }
                break;
            case "/BUSY/":
                CallController.busy();
                //CallController.finish();
                break;

            default:
                // Invalid notification received
                if (messageIn.startsWith("{"))
                {
                    //{"phoneNumber":"07038557462","schedule":{"from":"2019-06-27T16:03:00.000Z","to":"2019-06-27T17:03:00.000Z"}}
                    try {
                        JSONObject object = new JSONObject(messageIn);
                        String phoneNumber = object.getString("phone");
                        JSONObject schedule = object.getJSONObject("schedule");
                        String from = schedule.getString("from");
                        String to = schedule.getString("to");
                        String startTimeDB = from.substring(0, 10) + " " + from.substring(11, 16);
                        String endTimeDB = to.substring(0, 10) + " " + to.substring(11, 16);
                        //dhtest
                        Log.i("dhtest","TCP listener phone : "+phoneNumber+" from : "+startTimeDB+" to : "+endTimeDB);

                        ConferenceDatabaseHelper ConferenceDB = new ConferenceDatabaseHelper(getApplicationContext());
                        ConferenceDB.insert(startTimeDB, endTimeDB, phoneNumber);
                        ConferenceDB.showList();

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, sender + " sent invalid message: " + messageIn,e);
                    }
                } else {
                    Log.w(LOG_TAG, sender + " sent invalid message: " + messageIn);
                }
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
            String ip =String.format(Locale.US, "%d.%d.%d.%d", (LocalIpAddressBin & 0xff), (LocalIpAddressBin >> 8 & 0xff), (LocalIpAddressBin >> 16 & 0xff), (LocalIpAddressBin >> 24 & 0xff));
          //  if (!PhoneState.getInstance().getPreviousIP(this).equals(ip)) {
                if (PhoneState.getUpdatingIP() != LocalIpAddressBin ) {
                    PhoneState.setUpdatingIP(LocalIpAddressBin);
                    String phoneNumber = User.getPhoneNumber(this);
                    JSONObject object = param.setIP(ip, phoneNumber);
                    serverApi.setIP(this, object, ip);
                }
               // PhoneState.setCurrentIP(this, ip);
        //    }
            startListenerForTCP();


            Log.i(LOG_TAG, "Service started");
        } else {
            startListenerForTCP();
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
        private BufferedWriter output;
        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException",e);
            }
        }

        public void run() {
            while (true) {
                try {
                    String read = input.readLine();
                    if (read == null ){
                        break;
                    }else{
                        String senderIP = clientSocket.getInetAddress().getHostAddress();
                        ProcessReceivedUdpMessage(senderIP, read);
                        Log.i(LOG_TAG, "send Result");
//                        output.write("{\"result\",\"ok\"}\n");
//                        output.flush();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException",e);
                }catch (Exception e) {
                    Log.e(LOG_TAG, "Exception",e);
                }
            }
            Util.safetyClose(clientSocket);
        }

    }


    //@TODO
    // job 스케쥴러를 쓸까 말까 고민 중
}