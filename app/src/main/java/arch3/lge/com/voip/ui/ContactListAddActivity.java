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

public class ContactListAddActivity extends AppCompatActivity {
    private String UserName;
    private String PhoneNum;
    private EditText mUserName;
    private EditText mPhoneNum;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list_add);

        Intent intent = getIntent();
        String dialInputStr = intent.getStringExtra("PhoneNum");

        mUserName = (EditText)findViewById(R.id.contact_mgr_user);
        mPhoneNum = (EditText)findViewById(R.id.contact_mgr_phone);


        if (dialInputStr != null){
            mPhoneNum.setText(dialInputStr);
        }

        Button mAddContactList = (Button)findViewById(R.id.contact_mgr_add);
        mAddContactList.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                UserName = mUserName.getText().toString();
                PhoneNum = mPhoneNum.getText().toString();
                Log.v("dae", "Click Add button of contact List");
                Log.v("dae", "Name : "+UserName+" Phone : "+PhoneNum);

                ContactListDataHelper ContactDB = new ContactListDataHelper(getApplicationContext());
                ContactDB.insertContextList(UserName, PhoneNum);
                ContactDB.showList();
                Log.v("dae", "data : "+ContactDB.personList.toString());

                finish();
            }
        });
    }




}
