package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.model.database.ContactListDataHelper;

public class ContactListEditActivity extends AppCompatActivity {

    private EditText mUserName;
    private EditText mPhoneNum;
    private String UserName;
    private String PhoneNum;
    private String originalUserName;
    private String originalPhoneNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list_edit);
        Intent intent = getIntent();
        originalPhoneNum = intent.getStringExtra("phone");
        originalUserName = intent.getStringExtra("user");

        mUserName = (EditText)findViewById(R.id.contact_edit_user);
        mPhoneNum = (EditText)findViewById(R.id.contact_edit_phone);

        if (originalPhoneNum != null){
            mPhoneNum.setText(originalPhoneNum);
        }

        if (originalUserName != null){
            mUserName.setText(originalUserName);
        }

        Button mAddContactList = (Button)findViewById(R.id.contact_edit_update);
        mAddContactList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                UserName = mUserName.getText().toString();
                PhoneNum = mPhoneNum.getText().toString();
                Log.v("dhtest", "Click Add button of contact List");
                Log.v("dhtest", "Name : "+UserName+" Phone : "+PhoneNum);

                ContactListDataHelper ContactDB = new ContactListDataHelper(getApplicationContext());
                ContactDB.updateContextList(originalUserName, UserName, PhoneNum);
                ContactDB.showList();
                Log.v("dhtest", "data : "+ContactDB.personList.toString());

                finish();
            }
        });
    }
}
