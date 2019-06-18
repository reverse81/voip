package arch3.lge.com.voip.model.UDPnetwork;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;

import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.utils.NetworkConstants;
import arch3.lge.com.voip.utils.Util;

public class TCPCmd extends IntentService {

    public final static String GUI_VOIP_CTRL = "GuiVoIpControl";
    private static final String LOG_TAG = "UDPListenerService";
    private static final int BUFFER_SIZE = 128;

    private static ICallController controller;

    public TCPCmd() {
        super(GUI_VOIP_CTRL);
    }

    public TCPCmd(String name) {
        super(name);
    }

    // private DatagramSocket socket;

    public static void setController ( ICallController controller) {
        TCPCmd.controller = controller;
    }

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

                PhoneState.getInstance().SetRemoteIP(Sender);
                PhoneState.getInstance().SetCmdIP(Sender);
                PhoneState.getInstance().SetPhoneState(PhoneState.CallState.CALLING);
                TCPSend(Sender, NetworkConstants.CONTROL_DATA_PORT, "/CALLIP/");
                PhoneState.getInstance().NotifyUpdate();
                Log.i("CALL", "AAAAAAAAAAAAAAAAAAAAAAAAA");
                break;
            case "/END_CALL_BUTTON/":
                EndCall();
                PhoneState.getInstance().NotifyUpdate();
                break;

            case "/ANSWER_CALL_BUTTON/":
                try {
                //    EndRinger();
                    TCPSend(PhoneState.getInstance().GetRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/ANSWER/");
                    InetAddress address = InetAddress.getByName(PhoneState.getInstance().GetRemoteIP());
                    PhoneState.getInstance().SetInComingIP(PhoneState.getInstance().GetRemoteIP());
                    PhoneState.getInstance().SetPhoneState(PhoneState.CallState.INCALL);
//                    if (Audio.StartAudio(address, MainActivity.SimVoice))
//                        Log.e(LOG_TAG, "Audio Already started (Answer Button)");
//                    if (Video.StartVideo(address))
//                        Log.e(LOG_TAG, "Video Already started (Answer Button)");
                    PhoneState.getInstance().SetRecvVideoState(PhoneState.VideoState.START_VIDEO);
                    Log.i(LOG_TAG, "Answered " + address.toString());
                } catch (UnknownHostException e) {

                    Log.e(LOG_TAG, "UnknownHostException Answer Button: " + e);
                } catch (Exception e) {

                    Log.e(LOG_TAG, "Exception Answer Button: " + e);
                }
                PhoneState.getInstance().NotifyUpdate();
                break;
            case "/REFUSE_CALL_BUTTON/":
                TCPSend(PhoneState.getInstance().GetRemoteIP(), NetworkConstants.CONTROL_DATA_PORT, "/REFUSE/");
                EndCall();
                PhoneState.getInstance().NotifyUpdate();
                break;
//            case "/Audio_Output_Menu_Button/":
//                if (MainActivity.AudioOutputTarget == MainActivity.EOutputTarget.SPEAKER) {
//                    audioManager.setBluetoothScoOn(false);
//                    audioManager.stopBluetoothSco();
//                    audioManager.setSpeakerphoneOn(true);
//                } else if (MainActivity.AudioOutputTarget == MainActivity.EOutputTarget.EARPIECE) {
//                    audioManager.setBluetoothScoOn(false);
//                    audioManager.stopBluetoothSco();
//                    audioManager.setSpeakerphoneOn(false);
//                } else if (MainActivity.AudioOutputTarget == MainActivity.EOutputTarget.BLUETOOTH) {
//                    audioManager.setSpeakerphoneOn(false);
//                    audioManager.setBluetoothScoOn(true);
//                    audioManager.startBluetoothSco();
//                }
//                PhoneState.getInstance().NotifyUpdate();
//                break;
            case "/Sim_Voice_Menu_Button/":
                PhoneState.getInstance().NotifyUpdate();
                break;
//            case "/TOGGLE_MIC_BUTTON/":
//                if (Sender.equals("true")) {
//                    PhoneState.getInstance().SetMic(true);
//                    audioManager.setMicrophoneMute(false);
//                } else if (Sender.equals("false")) {
//                    PhoneState.getInstance().SetMic(false);
//                    audioManager.setMicrophoneMute(true);
//                }
//                PhoneState.getInstance().NotifyUpdate();
//                break;
            case "/TOGGLE_BOOST_BUTTON/":
                if (Sender.equals("true")) {
                    PhoneState.getInstance().SetBoost(true);
                } else if (Sender.equals("false")) {
                    PhoneState.getInstance().SetBoost(false);
                }
                PhoneState.getInstance().NotifyUpdate();
                break;
            case "/TOGGLE_RINGER_BUTTON/":
                if (Sender.equals("true")) {
                    PhoneState.getInstance().SetRinger(true);
                } else if (Sender.equals("false")) {
                    PhoneState.getInstance().SetRinger(false);
                }
                PhoneState.getInstance().NotifyUpdate();
                break;
            default:
                // Invalid notification received
                Log.w(LOG_TAG, Sender + " sent invalid message: " + MessageIn);
                break;
        }
    }

    private synchronized void EndCall() {
        controller.finish();
    }


    private void TCPSend(final String RemoteIp, final int port, final String message) {
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Socket socket=null;
                OutputStream outputStream=null;
                try {

                    InetAddress address = InetAddress.getByName(RemoteIp);
                    MyEncrypt encrypt = new MyEncrypt();
                    String messageOut = encrypt.encrypt(message);

                    byte[] buffer = messageOut.getBytes();

                    //ia = InetAddress.getByName("서버 주소 입력");    //서버로 접속
                    socket = new Socket(address, NetworkConstants.CONTROL_DATA_PORT);

                    outputStream = socket.getOutputStream();
                    outputStream.write(buffer);
                    outputStream.flush();;

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