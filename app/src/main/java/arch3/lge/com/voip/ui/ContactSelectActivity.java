package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import arch3.lge.com.voip.R;

public class ContactSelectActivity extends Activity {
    ContactArrayAdapter mAdapter = new ContactArrayAdapter();
    final int CONTACT_NORMAL = 0;
    final int CONTACT_DELETE = 1;
    final int CONTACT_SELECT = 2;
    final int CONTACT_EDIT = 3;
    int mContactType = CONTACT_NORMAL;
    String mUserStr;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("dhtest", "Contact Resume");
        mAdapter.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ListView listView;

        Intent intent = getIntent(); /*데이터 수신*/
        String contactType = intent.getExtras().getString("type");

        Log.i("dhtest","Create Contact Select type :"+contactType);

        if (contactType.equals("normal")) {
            mContactType = CONTACT_NORMAL;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView, contactType);
        }
        else if (contactType.equals("delete")) {
            mContactType = CONTACT_DELETE;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView, contactType);
        }
        else if (contactType.equals("select")){
            Log.i("dhtest", "contact select type");
            mContactType = CONTACT_SELECT;
            setContentView(R.layout.activity_contact_select);
            listView = (ListView) findViewById(R.id.contact_list_select);
            mAdapter.onCreate(this, listView, contactType);
            mUserStr = intent.getExtras().getString("user");
        }
        else if (contactType.equals("edit")){
            mContactType = CONTACT_EDIT;
            setContentView(R.layout.activity_contact_select);
            listView = (ListView) findViewById(R.id.contact_list_select);
            mAdapter.onCreate(this, listView, contactType);
        }
        else{
            mContactType = CONTACT_NORMAL;
            setContentView(R.layout.activity_contact);
            listView = (ListView) findViewById(R.id.contact_list);
            mAdapter.onCreate(this, listView, contactType);
        }

        //Click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("dhtest", "Position:"+position+" id:"+id);


                if (mContactType == CONTACT_SELECT){
                    String phoneString = ((TextView) view.findViewById(R.id.list_item_phone)).getText().toString();
                    String userString = ((TextView) view.findViewById(R.id.list_item_name)).getText().toString();
                    Log.i("dhtest", "phone:"+phoneString);

                    Intent intent = new Intent(getApplicationContext(), ConferenceRegisterActivity.class);
                    intent.putExtra("phone",phoneString); /*송신*/
                    intent.putExtra("user",mUserStr); /*송신*/
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                else if(mContactType == CONTACT_NORMAL){
                    String phoneString = ((TextView) view.findViewById(R.id.list_item_phone)).getText().toString();
                    String userString = ((TextView) view.findViewById(R.id.list_item_name)).getText().toString();
                    Log.i("dhtest", "phone:"+phoneString);

                    Intent intent = new Intent(getApplicationContext(), DialpadActivity.class);
                    intent.putExtra("phone",phoneString); /*송신*/
                    intent.putExtra("user",userString); /*송신*/
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                else if(mContactType == CONTACT_EDIT){
                    String phoneString = ((TextView) view.findViewById(R.id.list_edit_item_phone)).getText().toString();
                    String userString = ((TextView) view.findViewById(R.id.list_edit_item_name)).getText().toString();
                    Log.i("dhtest", "phone:"+phoneString);

                    Intent intent = new Intent(getApplicationContext(), ContactListEditActivity.class);
                    intent.putExtra("phone",phoneString); /*송신*/
                    intent.putExtra("user",userString); /*송신*/
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    //finish();
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
