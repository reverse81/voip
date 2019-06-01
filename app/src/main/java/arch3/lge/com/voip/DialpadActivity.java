package arch3.lge.com.voip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
        Log.e(TAG, "onClickCall = "+v.getId());
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
