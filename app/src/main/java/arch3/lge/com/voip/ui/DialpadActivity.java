package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.listener.TCPListenerService;
import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIoCC;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIoCC;

public class DialpadActivity  extends MainTabActivity {
    final String TAG = "Dialpad";
    TextView    mNumberView;
    String      mNumberString = new String();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tapSelect = DIAL_SELECT;

        setContentView(R.layout.activity_dialpad);
        Log.e("sss", "enter");
        mNumberView = (TextView)findViewById(R.id.dialInput);
        mNumberView.setText(mNumberString);

        Intent intent = getIntent(); /*데이터 수신*/
        String PhoneNum = intent.getStringExtra("phone"); /*String형*/
        if (PhoneNum != null)
            mNumberString = PhoneNum;
        Log.i("dhtest", "Phone : "+PhoneNum);

        mNumberView = (TextView)findViewById(R.id.dialInput);
        mNumberView.setText(mNumberString);

        {
            Intent serviceIntent = new Intent(this, TCPListenerService.class);
            this.startService(serviceIntent);
            Log.i("dhtest", "Started TCPListenerService.class");
        }
    }

    public void onClickDigit(View v)
    {
        for(int idx = 0; idx < mDialNumId.length; idx++)
        {
            if(mDialNumId[idx] == v.getId())
            {
     //           Log.e(TAG, "idx = "+idx);
                mNumberString += idx;
                mNumberView.setText(mNumberString);
                break;
            }
        }
    }
    public void onClickAsterisk(View v)
    {
    //    Log.e(TAG, "onClickAsterisk = "+v.getId());
        mNumberString += "*";
        mNumberView.setText(mNumberString);
    }
    public void onClickSharp(View v)
    {
    //    Log.e(TAG, "onClickSharp = "+v.getId());
        mNumberString += "#";
        mNumberView.setText(mNumberString);
    }
    public void onClickAdd(View v)
    {
        Intent intent = new Intent(DialpadActivity.this, ContactListAddActivity.class);
        intent.putExtra("PhoneNum", mNumberString);
        startActivity(intent);
        //Log.e(TAG, "onClickAdd = "+v.getId());
       // Log.v("dae", "Click Add btn, Phone Number : "+mNumberString);

    }
    public void onClickCall(View v)
    {
        String phone = mNumberView.getText().toString();
        Intent screen = new Intent();

        if (phone == null || phone.isEmpty()) {
            return;
        }

        if (phone.startsWith("070")) {
            screen.setClassName(this.getPackageName(), ConferenceCallingActivity.class.getName());
            screen.putExtra("phoneNumber",  phone);
            this.startActivity(screen);
        } else {
            screen.setClassName(this.getPackageName(), RequestCallActivity.class.getName());
            screen.putExtra("phoneNumber",  phone);
            this.startActivity(screen);

//            {
//                VoIPVideoIo io = VoIPVideoIo.getInstance(this);
//                String ip = "10.0.1.4";
//                io.attachIP(ip);
//                Intent intent = new Intent();
//                intent.setClassName(this.getPackageName(), TCPCmd.class.getName());
//                intent.setAction(TCPCmd.GUI_VOIP_CTRL);
//                intent.putExtra("message", "/CALL_BUTTON/");
//                intent.putExtra("sender", ip);
//                this.startService(intent);
//            }
        }
        Log.e(TAG, "onClickCall = "+phone);


        //finish();
    }
    public void onClickDell(View v)
    {
       // Log.e(TAG, "onClickDell = "+v.getId());
        if(mNumberString.isEmpty())
            return;
        mNumberString = mNumberString.substring(0, mNumberString.length() - 1);
        mNumberView.setText(mNumberString);
    }

    public Integer[] mDialNumId = {
            R.id.dialNum0,
            R.id.dialNum1,
            R.id.dialNum2,
            R.id.dialNum3,
            R.id.dialNum4,
            R.id.dialNum5,
            R.id.dialNum6,
            R.id.dialNum7,
            R.id.dialNum8,
            R.id.dialNum9,
    };

}
