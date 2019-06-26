package arch3.lge.com.voip.ui;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.user.User;

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void onClickContact(View v)
    {
        Log.e(TAG, "onClickContact = "+v.getId());
        Intent intent = new Intent(this, ContactActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("type","noraml"); /*송신*/
        startActivity(intent);

    }
    public void onClickConfirence(View v)
    {
        Log.e(TAG, "onClickConfirence = "+v.getId());
        Intent intent = new Intent(this, ConferenceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //dhtest 상단 Menu Test
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("dae", "Option Item select");
        switch(item.getItemId()){
            case R.id.accountSetting:
                Log.v("dae", "Menu option1");
                Intent intent = new Intent(this, UserInfoActive.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            case R.id.log_out:
                Log.v("dae", "log out");
                Intent intent2 = new Intent(this, LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent2);
                User.setLogout(getApplicationContext());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        //return super.onOptionsItemSelected(item);
        //return true;
    }
}