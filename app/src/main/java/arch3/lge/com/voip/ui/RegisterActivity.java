package arch3.lge.com.voip.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.serverApi.ApiParamBuilder;
import arch3.lge.com.voip.model.serverApi.ServerApi;
import arch3.lge.com.voip.model.user.User;

public class RegisterActivity extends AppCompatActivity {

    private AutoCompleteTextView emailText;
    private AutoCompleteTextView nameText;
    private EditText passwordText;
    private EditText retypeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = new Intent(this.getIntent());
        String email = intent.getStringExtra("email");
        emailText = (AutoCompleteTextView)findViewById(R.id.email);
        emailText.setText(email);

        nameText = (AutoCompleteTextView)findViewById(R.id.name);

        passwordText = (EditText)findViewById(R.id.password);

        retypeText = (EditText)findViewById(R.id.retype);

        Button registerButton = (Button) findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
    }

    private void attemptRegister() {

        // Reset errors.
        emailText.setError(null);
        passwordText.setError(null);
        retypeText.setError(null);

        // Store values at the time of the login attempt.
        User user = new User(emailText.getText().toString(), passwordText.getText().toString());
        user.setRetypedPassword(retypeText.getText().toString());

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (emailText.length() < 4) {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (emailText.length() == 0) {
            emailText.setError(getString(R.string.error_field_required));
            focusView = emailText;
            cancel = true;
        } else if (!user.isEmailValid()) {
            emailText.setError(getString(R.string.error_invalid_email));
            focusView = emailText;
            cancel = true;
        }

        if (!user.isSamePassword()) {
            retypeText.setError(getString(R.string.error_mismatch_retype_password));
            focusView = retypeText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //@TODO
            // Request register using controller
            Log.v("dae", "Transmit email : "+user.getEmail()+" password:"+user.getPassword());//dhtest
            ApiParamBuilder createParam = new ApiParamBuilder();
            JSONObject sendJsonObject = createParam.getCreate(user.getEmail(), user.getPassword());

            ServerApi server = new ServerApi();
            server.create(this, sendJsonObject);

            //finish();
        }
    }

    public void successReister(){
        Log.v("dae", "finish..");//dhtest

    }


}
