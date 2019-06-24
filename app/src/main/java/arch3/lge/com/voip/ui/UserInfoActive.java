package arch3.lge.com.voip.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.user.User;

public class UserInfoActive extends AppCompatActivity {
    final String TAG = "UserInfo";

    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUserPhoneNumView;
    private EditText PasswordView;
    private String EmailText;
    private String UserPhoneNumText;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_account);


        Log.v("dae", "Create User Info");
        EmailText = User.getEmail(getApplicationContext());
        UserPhoneNumText = User.getPhoneNumber(getApplicationContext());


        if (EmailText.isEmpty())
            EmailText ="daehyun@lge.com";
        if (UserPhoneNumText.isEmpty())
            UserPhoneNumText = "daehyun";

        Log.v("dae", "email: "+EmailText+" name:"+ UserPhoneNumText);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.user_info_email);
        mEmailView.setText(EmailText);

        mUserPhoneNumView = (AutoCompleteTextView) findViewById(R.id.user_info_phonenum);
        mUserPhoneNumView.setText(UserPhoneNumText);

        Log.v("dae", "Done user info!!");

    }


}
