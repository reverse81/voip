package arch3.lge.com.voip.controller;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class DeviceContorller {
    final static String LOG_TAG ="DeviceController";

    static public boolean  toggleSpeakerPhone(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
            return false;
        } else {
            audioManager.setSpeakerphoneOn(true);
            return true;
        }

    }

    static public boolean  toggleBluetooth(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }
        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(!audioManager.isBluetoothScoOn());
            audioManager.stopBluetoothSco();
            return false;
        } else {
            Log.i(LOG_TAG, "Bluetooth on");
            audioManager.setBluetoothScoOn(!audioManager.isBluetoothScoOn());
            audioManager.startBluetoothSco();
            return  true;
        }
    }

    static public boolean  toggleMicMute(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isMicrophoneMute()) {
            audioManager.setMicrophoneMute(false);
                    return false;
        } else {
            audioManager.setMicrophoneMute(true);
            return true;
        }
    }


    static public void initDevice(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setBluetoothScoOn(false);
        audioManager.stopBluetoothSco();
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMicrophoneMute(false);
    }

    static public void initDeviceForCC(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setBluetoothScoOn(false);
        audioManager.stopBluetoothSco();
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMicrophoneMute(true);
    }
}
