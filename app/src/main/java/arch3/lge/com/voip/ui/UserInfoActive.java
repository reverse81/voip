package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.model.user.User;

public class UserInfoActive extends Activity {
    final String TAG = "UserInfo";

    private EditText mEmailView;
    private TextView mUserPhoneNumView;
    private TextView mCurEmailView;
    private EditText mPasswordView;
    private EditText mNewPasswordView;
    private EditText mNewPasswordReTypeView;
    private String EmailText;
    private String UserPhoneNumText;
    private String Password;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_account);


        Log.v("dhtest", "Create User Info");
        EmailText = User.getEmail(getApplicationContext());
        UserPhoneNumText = User.getPhoneNumber(getApplicationContext());


        Log.v("dhtest", "email: "+EmailText+" phone:"+ UserPhoneNumText);//dhtest

        mEmailView = (EditText) findViewById(R.id.user_info_email);
        mEmailView.setText(EmailText);

        mCurEmailView = (TextView)findViewById(R.id.user_info_cur_email);
        mCurEmailView.setText(EmailText);

        mUserPhoneNumView = (TextView) findViewById(R.id.user_info_cur_phone);
        mUserPhoneNumView.setText(UserPhoneNumText);

        mPasswordView = (EditText)findViewById(R.id.user_info_password);
        mNewPasswordView = (EditText)findViewById(R.id.user_info_new_password);
        mNewPasswordReTypeView = (EditText)findViewById(R.id.user_info_new_password_retype);

        Log.v("dhtest", "Done user info!!");

        Button registerButton = (Button) findViewById(R.id.update_user_info);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attemptUpdate();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.update_user_info_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void attemptUpdate(){
        boolean cancel = false;
        View focusView = null;
        String StrPassword = mNewPasswordView.getText().toString();
        String StrPasswordRetype = mNewPasswordReTypeView.getText().toString();
        EmailText = mEmailView.getText().toString();
        Password = mPasswordView.getText().toString();

        // Reset errors.
        mPasswordView.setError(null);
        mNewPasswordView.setError(null);
        mNewPasswordReTypeView.setError(null);

        // Check for a valid password, if the user entered one.
        if (mPasswordView.length() < 8) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (mPasswordView.length() == 0) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check New password
        if (mNewPasswordView.length() > 0) {
            if (mNewPasswordView.length() < 8) {
                mNewPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mNewPasswordView;
                cancel = true;
            }
            else if (!StrPassword.equals(StrPasswordRetype)){
                mNewPasswordReTypeView.setError(getString(R.string.error_mismatch_retype_password));
                focusView = mNewPasswordReTypeView;
                cancel = true;
            }
        }
        else{
            StrPassword = null;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            Log.v("dhtest", "Fail User account update!!");
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //@TODO
            // Request udpate user information
            ApiParamBuilder param = new ApiParamBuilder();
            ServerApi server = new ServerApi();
            JSONObject object = param.updateAccountInfo(UserPhoneNumText, EmailText, Password, StrPassword);
            server.update(this, object, EmailText);

            Log.v("dhtest", "User account update.. email:"+EmailText+" pw : "+Password+" newPW: "+StrPassword);

        }
    }


}
