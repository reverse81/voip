package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class RecoveryPwdActivity extends Activity {

    private TextView mEmailView;
    private TextView mUserPhoneNumView;
    private EditText mNewPasswordView;
    private EditText mNewPasswordReTypeView;
    private String EmailText;
    private String UserPhoneNumText;
    private String Password;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recovery_pwd);

        Intent intent = getIntent(); /*데이터 수신*/
        Password = intent.getExtras().getString("pwd");

        Log.v("dhtest", "Change new password");
        EmailText = User.getEmail(getApplicationContext());
        UserPhoneNumText = User.getPhoneNumber(getApplicationContext());


        Log.v("dhtest", "email: "+EmailText+" phone:"+ UserPhoneNumText);//dhtest
        mEmailView = (TextView) findViewById(R.id.user_recovery_email);
        mEmailView.setText(EmailText);

        mUserPhoneNumView = (TextView) findViewById(R.id.user_recovery_phone);
        mUserPhoneNumView.setText(UserPhoneNumText);

        mNewPasswordView = (EditText)findViewById(R.id.user_recovery_new_password);
        mNewPasswordReTypeView = (EditText)findViewById(R.id.user_recovery_new_password_retype);

        Log.v("dhtest", "Done user info!!");

        Button registerButton = (Button) findViewById(R.id.update_recovery_pwd);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attemptUpdate();
            }
        });

    }

    private void attemptUpdate(){
        boolean cancel = false;
        View focusView = null;
        String StrPassword = mNewPasswordView.getText().toString();
        String StrPasswordRetype = mNewPasswordReTypeView.getText().toString();
        EmailText = mEmailView.getText().toString();

        // Reset errors.
        mNewPasswordView.setError(null);
        mNewPasswordReTypeView.setError(null);


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
            server.update_password(this, object, EmailText);

            Log.v("dhtest", "User new password update.. email:"+EmailText+" pw : "+Password+" newPW: "+StrPassword);

        }
    }


}
