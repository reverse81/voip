package arch3.lge.com.voip;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class MainTabActivity extends AppCompatActivity {
    final String TAG = "MainTab";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClickDialpad(View v)
    {
        Log.e(TAG, "onClickDialpad = "+v.getId());
        Intent intent = new Intent(this, DialpadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
    public void onClickContact(View v)
    {
        Log.e(TAG, "onClickContact = "+v.getId());
        Intent intent = new Intent(this, ContactActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
    public void onClickConfirence(View v)
    {
        Log.e(TAG, "onClickConfirence = "+v.getId());
        Intent intent = new Intent(this, ConferenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}