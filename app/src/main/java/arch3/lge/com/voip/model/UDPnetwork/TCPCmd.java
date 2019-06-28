package arch3.lge.com.voip.model.UDPnetwork;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

public class TCPCmd extends IntentService {

    public final static String GUI_VOIP_CTRL = "GuiVoIpControl";
    private static final String LOG_TAG = "VoIP:TCPCmd";
    private static final int BUFFER_SIZE = 128;

    public TCPCmd() {
        super(GUI_VOIP_CTRL);
    }

    public TCPCmd(String name) {
        super(name);
    }

    // private DatagramSocket socket;



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "onReceive : "+ intent.getAction());
        if (intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(TCPCmd.GUI_VOIP_CTRL)) {
            Log.i(LOG_TAG, "onHandleIntent");
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            ProcessReceivedGUIMessage(sender, message);
        }
    }

    private void ProcessReceivedGUIMessage(final String Sender, String MessageIn) {

        switch (MessageIn) {

            case "/CALL_BUTTON/":

                PhoneState.getInstance().setRemoteIP(Sender);
                PhoneState.getInstance().setCallState(PhoneState.CallState.CALLING);
                TCPSend(Sender, NetworkConstants.CONTROL_DATA_PORT, "/CALLIP/");
                //PhoneState.getInstance().NotifyUpdate();
               // Log.i("CALL", "AAAAAAAAAAAAAAAAAAAAAAAAA");
                break;
            case "/END_CALL_BUTTON/":
                TCPSend(PhoneState.getInstance().getRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/ENDCALL/");

                CallController.finish();
                //PhoneState.getInstance().NotifyUpdate();
                break;

            case "/ANSWER_CALL_BUTTON/":
                try {
                //    EndRinger();
                    TCPSend(PhoneState.getInstance().getRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/ANSWER/");

                    PhoneState.getInstance().setCallState(PhoneState.CallState.INCALL);
                    PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.START_VIDEO);
                    Log.i(LOG_TAG, "Answered " + PhoneState.getInstance().getRemoteIP());
                } catch (Exception e) {

                    Log.e(LOG_TAG, "Exception Answer Button: " + e);
                }
               // PhoneState.getInstance().NotifyUpdate();
                break;
            case "/REFUSE_CALL_BUTTON/":
                TCPSend(PhoneState.getInstance().getRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/REFUSE/");
                CallController.finish();
            case "/BUSY_SIGNAL/":
                TCPSend(PhoneState.getInstance().getRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/BUSY/");
             //   PhoneState.getInstance().NotifyUpdate();
                break;
            default:
                // Invalid notification received
                Log.w(LOG_TAG, Sender + " sent invalid message: " + MessageIn);
                break;
        }
    }


    private void TCPSend(final String RemoteIp, final int port, final String message) {
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Socket socket=null;
                OutputStream outputStream=null;
                try {

                    Log.i(LOG_TAG, " we will send to "+ RemoteIp + ":" + message);
                    InetAddress address = InetAddress.getByName(RemoteIp);
                    MyEncrypt encrypt = new MyEncrypt();
                    String messageOut = encrypt.encrypt(message);

                    byte[] buffer = messageOut.getBytes();

                    //ia = InetAddress.getByName("서버 주소 입력");    //서버로 접속
                    socket = new Socket(address, NetworkConstants.CONTROL_DATA_PORT);

                    outputStream = socket.getOutputStream();
                    outputStream.write(buffer);
                    outputStream.flush();

                    Log.i(LOG_TAG, "UdpSend( " + messageOut + " ) to " + RemoteIp);


                } catch (SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in UdpSend: " + e);
                } catch (IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in UdpSend: " + e);
                } finally {
                    Util.safetyClose(socket);
                    Util.safetyClose(outputStream);
                }
            }
        });
        replyThread.start();
    }
}