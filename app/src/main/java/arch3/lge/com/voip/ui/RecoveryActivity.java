package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.model.user.User;

public class RecoveryActivity extends AppCompatActivity {
    private AutoCompleteTextView emailText;
    private AutoCompleteTextView passwordHintText;
    private AutoCompleteTextView phoneText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);
        getSupportActionBar().hide();

        Intent intent = new Intent(this.getIntent());
        String email = intent.getStringExtra("email");
        Log.v("dhtest", "Recovery create : "+email);
        emailText = (AutoCompleteTextView)findViewById(R.id.recovery_email);
        if (email != null)
            emailText.setText(email);
        phoneText = (AutoCompleteTextView)findViewById(R.id.recovery_phonenum);
        phoneText.setText(User.getPhoneNumber(this));
        passwordHintText = (AutoCompleteTextView)findViewById(R.id.recovery_hint);

        Button recoveryButton = (Button) findViewById(R.id.buttonRecovery);
        recoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPassword();
            }
        });
    }

    private void checkPassword(){
        boolean cancel = false;
        View focusView = null;
        String email = emailText.getText().toString();
        String reveryHint = passwordHintText.getText().toString();
        String phone = phoneText.getText().toString();

        emailText.setError(null);
        passwordHintText.setError(null);


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //@TODO
            // Request register using controller
            Log.v("dhtest", "Transmit email : "+email+" Hint:"+reveryHint);//dhtest
            ApiParamBuilder createParam = new ApiParamBuilder();
            JSONObject recovery = new JSONObject();
            try {
                recovery.put("question", "What kind of animal do you like?");
                recovery.put("answer", reveryHint);
            } catch (JSONException e) {
                Log.e("dhtest", "JSONException on getRetrieveApplistParam...", e);
            }

            JSONObject sendJsonObject = createParam.getRecovery(email, phone, recovery);

            ServerApi server = new ServerApi();
            server.recovery(this, sendJsonObject);

            //finish();
        }




    }
}
