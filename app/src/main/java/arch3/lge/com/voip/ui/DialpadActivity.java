package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;


import java.text.SimpleDateFormat;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;

public class DialpadActivity  extends MainTabActivity {
    final String TAG = "Dialpad";
    TextView    mNumberView;
    String      mNumberString = new String();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialpad);
        Log.e("sss", "enter");
        mNumberView = (TextView)findViewById(R.id.dialInput);
        mNumberView.setText(mNumberString);

        ApiParamBuilder param = new ApiParamBuilder();
        ServerApi server = new ServerApi();
        JSONObject object = param.getLogin("reverse81@naver.com","1111");
        server.login(getApplicationContext(), object.toString(), "reverse81@naver.com");


//
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm");
//
//        String[] memebers = {"11111","2222","33333","44444"};
//
//        JSONObject object = param.requestCC(memebers,"2019-11-11 11:22","2019-11-11 11:22"
//        );


//        Log.e("sss", object.toString());
//        JSONObject object = param.getLogin("ddd@naver.com","1111");
//                server.login(getApplicationContext(), object.toString(), "ddd@naver.com");



//        server.create(getApplicationContext(),object);

//        ApiParamBuilder param = new ApiParamBuilder();
//        ServerApi server = new ServerApi();

//        JSONObject object = param.getIP("117782905");
//        server.getIP(getApplicationContext(), object);
//1234567892

//        JSONObject object = param.setIP("1.1.1.1","117782905");
//        server.setIP(getApplicationContext(), object);
   //     server.login(getApplicationContext(), object.toString());

    //    JSONObject object2 = param.getRecovery("ddd@naver.com","117782905");
     //   server.recovery(getApplicationContext(), object2);
//
//        ServerApi server = new ServerApi();
//        server.login(getApplicationContext(), object.toString());
    }








    public void onClickDigit(View v)
    {
        for(int idx = 0; idx < mDialNumId.length; idx++)
        {
            if(mDialNumId[idx] == v.getId())
            {
                Log.e(TAG, "idx = "+idx);
                mNumberString += idx;
                mNumberView.setText(mNumberString);
                break;
            }
        }
    }
    public void onClickAsterisk(View v)
    {
        Log.e(TAG, "onClickAsterisk = "+v.getId());
        mNumberString += "*";
        mNumberView.setText(mNumberString);
    }
    public void onClickSharp(View v)
    {
        Log.e(TAG, "onClickSharp = "+v.getId());
        mNumberString += "#";
        mNumberView.setText(mNumberString);
    }
    public void onClickAdd(View v)
    {
        Log.e(TAG, "onClickAdd = "+v.getId());
    }
    public void onClickCall(View v)
    {
        String phone = mNumberView.getText().toString();
        Intent screen = new Intent();

        if (phone.startsWith("xxxx")) {
            screen.setClassName(this.getPackageName(), ConferenceCallingActivity.class.getName());
            screen.putExtra("phoneNumber",  phone);
            this.startActivity(screen);
        } else {
            screen.setClassName(this.getPackageName(), RequestCallActivity.class.getName());
            screen.putExtra("phoneNumber",  phone);
            this.startActivity(screen);
        }


        Log.e(TAG, "onClickCall = "+v.getId());


        //finish();
    }
    public void onClickDell(View v)
    {
        Log.e(TAG, "onClickDell = "+v.getId());
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
