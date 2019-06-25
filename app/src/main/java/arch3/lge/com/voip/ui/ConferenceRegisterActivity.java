package arch3.lge.com.voip.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import arch3.lge.com.voip.R;

public class ConferenceRegisterActivity extends Activity {

    private static int mYear, mMonth, mDay, mStartHour, mStartMinute, mEndHour, mEndMinute;
    private static TextView mTxtDate;
    private static TextView mTxtStartTime;
    private static TextView mTxtEndTime;

    private String mPhoneNum;
    private String mUserNum;
    private static String mPhoneNum1;
    private static String mPhoneNum2;
    private static String mPhoneNum3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_register);

        Intent intent = getIntent(); /*데이터 수신*/
        mPhoneNum = intent.getStringExtra("phone"); /*String형*/
        mUserNum = intent.getStringExtra("user"); /*String형*/

        Log.v("dae", "Create conference register");
        Log.v("dae", "Phone : "+mPhoneNum+" User : "+mUserNum);


        //텍스트뷰 2개 연결
        mTxtDate = (TextView)findViewById(R.id.conference_register_date_txt);
        mTxtStartTime = (TextView)findViewById(R.id.conference_register_starttime_txt);
        mTxtEndTime = (TextView)findViewById(R.id.conference_register_endtime_txt);

        //현재 날짜와 시간을 가져오기위한 Calendar 인스턴스 선언
        if (mPhoneNum == null) {
            Calendar cal = new GregorianCalendar();
            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);
            mStartHour = cal.get(Calendar.HOUR_OF_DAY);
            mStartMinute = cal.get(Calendar.MINUTE);
            mEndHour = cal.get(Calendar.HOUR_OF_DAY);
            mEndMinute = cal.get(Calendar.MINUTE);
            mPhoneNum1 = null;
            mPhoneNum2 = null;
            mPhoneNum3 = null;
        }
        UpdateNow();

        //Attendee info
        if (mPhoneNum != null) {
            if (mUserNum.equals("user1")) {
                mPhoneNum1 = mPhoneNum;
            } else if (mUserNum.equals("user2")) {
                mPhoneNum2 = mPhoneNum;
            } else if (mUserNum.equals("user3")) {
                mPhoneNum3 = mPhoneNum;
            }

            if(mPhoneNum1 != null) {
                TextView textview = (TextView) findViewById(R.id.conference_register_add1_txt);
                textview.setText(mPhoneNum1);
            }
            if(mPhoneNum2 != null) {
                TextView textview = (TextView) findViewById(R.id.conference_register_add2_txt);
                textview.setText(mPhoneNum2);
            }
            if(mPhoneNum3 != null) {
                TextView textview = (TextView) findViewById(R.id.conference_register_add3_txt);
                textview.setText(mPhoneNum3);
            }
        }
    }



    public void mOnClick(View v){
        switch(v.getId()){
            //날짜 대화상자 버튼이 눌리면 대화상자를 보여줌
            case R.id.conference_register_date_btn:
                //여기서 리스너도 등록함
                Log.v("dae", "onClick conference_register_date_btn");
                new DatePickerDialog(ConferenceRegisterActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
                break;

            //시간 대화상자 버튼이 눌리면 대화상자를 보여줌
            case R.id.conference_register_starttime_btn:
                //여기서 리스너도 등록함
                Log.v("dae", "onClick conference_register_time_btn");
                new TimePickerDialog(ConferenceRegisterActivity.this, mTimeSetListenerStart, mStartHour, mStartMinute, false).show();
                break;

            //시간 대화상자 버튼이 눌리면 대화상자를 보여줌
            case R.id.conference_register_endtime_btn:
                //여기서 리스너도 등록함
                Log.v("dae", "onClick conference_register_time_btn");
                new TimePickerDialog(ConferenceRegisterActivity.this, mTimeSetListenerEnd, mStartHour, mStartMinute, false).show();
                break;


            case R.id.conference_register_done_btn:
                Log.v("dae", "onClick conference_register_done_btn");
                Log.v("dae", "Date "+mYear+" "+mMonth+" "+mDay);
                Log.v("dae", "Time "+ mStartHour +" "+ mStartMinute);
                //ApiParamBuilder createParam = new ApiParamBuilder();
                //JSONObject sendJsonObject = createParam.requestCC(user.getEmail(), user.getPassword());

                Intent intent1 = new Intent(ConferenceRegisterActivity.this, ConferenceActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.conference_register_cancle_btn:
                Log.v("dae", "onClick conference_register_cancle_btn");
                Intent intent2 = new Intent(ConferenceRegisterActivity.this, ConferenceActivity.class);
                startActivity(intent2);
                finish();
                break;

            case R.id.conference_register_add1_btn:
                Log.v("dae", "onClick conference_register_cancle_btn");
                Intent intent3 = new Intent(this, ContactActivity.class);
                intent3.putExtra("type","select"); /*송신*/
                intent3.putExtra("user","user1"); /*송신*/
                startActivity(intent3);
                break;
            case R.id.conference_register_add2_btn:
                Log.v("dae", "onClick conference_register_cancle_btn");
                Intent intent4 = new Intent(this, ContactActivity.class);
                intent4.putExtra("type","select"); /*송신*/
                intent4.putExtra("user","user2"); /*송신*/
                startActivity(intent4);
                break;
            case R.id.conference_register_add3_btn:
                Log.v("dae", "onClick conference_register_cancle_btn");
                Intent intent5 = new Intent(this, ContactActivity.class);
                intent5.putExtra("type","select"); /*송신*/
                intent5.putExtra("user","user3"); /*송신*/
                startActivity(intent5);
                break;
        }
    }


    //날짜 대화상자 리스너 부분
    DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                //사용자가 입력한 값을 가져온뒤
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;

                Log.v("dae", "Date "+mYear+" "+mMonth+" "+mDay);

                //텍스트뷰의 값을 업데이트함
                UpdateNow();
            }
        };



    //시간 대화상자 리스너 부분
    TimePickerDialog.OnTimeSetListener mTimeSetListenerStart =
        new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // TODO Auto-generated method stub
                //사용자가 입력한 값을 가져온뒤
                mStartHour = hourOfDay;
                mStartMinute = minute;

                Log.v("dae", "Start Time "+ mStartHour +" "+ mStartMinute);

                //텍스트뷰의 값을 업데이트함
                UpdateNow();
            }
        };

    //시간 대화상자 리스너 부분
    TimePickerDialog.OnTimeSetListener mTimeSetListenerEnd =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    // TODO Auto-generated method stub
                    //사용자가 입력한 값을 가져온뒤
                    mEndHour = hourOfDay;
                    mEndMinute = minute;

                    Log.v("dae", "End Time "+ mEndHour +" "+ mEndMinute);

                    //텍스트뷰의 값을 업데이트함
                    UpdateNow();
                }
            };
    //텍스트뷰의 값을 업데이트 하는 메소드
    void UpdateNow(){
        mTxtDate.setText(String.format("%d/%d/%d", mYear, mMonth + 1, mDay));
        mTxtStartTime.setText(String.format("%d:%d", mStartHour, mStartMinute));
        mTxtEndTime.setText(String.format("%d:%d", mEndHour, mEndMinute));
    }

}
