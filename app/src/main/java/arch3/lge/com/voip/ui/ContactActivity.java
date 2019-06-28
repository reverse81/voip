package arch3.lge.com.voip.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import arch3.lge.com.voip.R;

public class ContactActivity extends MainTabActivity {
    ContactArrayAdapter mAdapter = new ContactArrayAdapter();
    final int CONTACT_NORMAL = 0;
    final int CONTACT_DELETE = 1;
    final int CONTACT_SELECT = 2;
    int mContactType = CONTACT_NORMAL;
    String mUserStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tapSelect = CONTACTLIST_SELECT;
        final ListView listView;

        Intent intent = getIntent(); /*데이터 수신*/
        String contactType = intent.getExtras().getString("type");

        if (contactType.equals("normal")) {
            mContactType = CONTACT_NORMAL;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView);
        }
        else if (contactType.equals("delete")) {
            mContactType = CONTACT_DELETE;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView);
        }
        else if (contactType.equals("select")){
            mContactType = CONTACT_SELECT;
            setContentView(R.layout.activity_conference_register_add_phone);
            listView = (ListView) findViewById(R.id.conference_register_add_phone);
            mAdapter.onCreate(this, listView);
            mUserStr = intent.getExtras().getString("user");
        }
        else{
            mContactType = CONTACT_NORMAL;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView);
        }

        //Click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("dhtest", "Position:"+position+" id:"+id);

                //String string = ((TextView)view).getText().toString();
                String string = ((TextView) view.findViewById(R.id.list_item_phone)).getText().toString();
                Log.i("dhtest", "phone:"+string);

                if (mContactType == CONTACT_SELECT){
                    Intent intent = new Intent(getApplicationContext(), ConferenceRegisterActivity.class);
                    intent.putExtra("phone",string); /*송신*/
                    intent.putExtra("user",mUserStr); /*송신*/
                    startActivity(intent);
                    finish();
                }
                else if(mContactType == CONTACT_NORMAL){
                    Intent intent = new Intent(getApplicationContext(), DialpadActivity.class);
                    intent.putExtra("phone",string); /*송신*/
                    intent.putExtra("user",mUserStr); /*송신*/
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

                // position이 클릭된 위치입니다.
                // 컬렉션에서 적절하게 꺼내서 사용하시면 됩니다.
                //Toast.makeText(activity, itemList.get(position).getSomethingColumn(), Toast.LENGTH_LONG).show();

                // 추가된 부분
                //InfoClass selectedInfoClass = weatherList.get(position);
                //selectedInfoClass.getXXX();
            }
        });
    }
}
