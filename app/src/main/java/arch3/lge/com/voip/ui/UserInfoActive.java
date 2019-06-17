package arch3.lge.com.voip.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.user.User;

public class UserInfoActive extends AppCompatActivity {
    final String TAG = "UserInfo";

    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUserNameView;
    private EditText PasswordView;
    private String EmailText;
    private String UserNameText;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_account);


        Log.v("dae", "Create User Info");
        EmailText = User.readEmail(getApplicationContext());
        UserNameText = User.readUserName(getApplicationContext());


        if (EmailText.isEmpty())
            EmailText ="daehyun@lge.com";
        if (UserNameText.isEmpty())
            UserNameText = "daehyun";

        Log.v("dae", "email: "+EmailText+" name:"+UserNameText);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.user_info_email);
        mEmailView.setText(EmailText);

        mUserNameView = (AutoCompleteTextView) findViewById(R.id.user_info_name);
        mUserNameView.setText(UserNameText);

        Log.v("dae", "Done user info!!");

    }


}
