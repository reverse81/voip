package arch3.lge.com.voip.model.serverApi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import arch3.lge.com.voip.R;
import arch3.lge.com.voip.controller.CallController;
import arch3.lge.com.voip.listener.TCPListenerService;
import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPAudioIoCC;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.codec.VoIPVideoIoCC;
import arch3.lge.com.voip.model.database.ConferenceDatabaseHelper;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.model.user.User;
import arch3.lge.com.voip.ui.ConferenceActivity;
import arch3.lge.com.voip.ui.ConferenceCallingActivity;
import arch3.lge.com.voip.ui.DialpadActivity;
import arch3.lge.com.voip.ui.LoginActivity;
import arch3.lge.com.voip.utils.NetworkConstants;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ServerApi {
    public final static String LOG_TAG = "VoIP:ServerApi";
    public final static String API_LOGIN = "auth/login";
    public final static String API_RECOVERY = "users/recovery"; // post
    public final static String API_GETIP = "users/ip";  //get
    public final static String API_SETIP = "users/ip";   //post
    public final static String API_REGISTER = "users/create"; // post
    public final static String API_CREATE_CC = "schedule/create";  // post
    public final static String API_GET_CC = "schedule/myschedule";  // get
    public final static String API_CREATE = "users/create";   //post
    public final static String API_UPDATE = "users/update";   //post
    public final static String API_GET_IP_CC= "schedule/IP";  // get

    public void login (final Context context, String source,final String email) {
        try {
            MyEncrypt encipher = new MyEncrypt();
            String text = encipher.encrypt(source);
            Log.i(LOG_TAG, encipher.decrypt(text));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");

            RequestParams params = new RequestParams();
            params.put("hashed_string", text);
            Log.i(LOG_TAG,             params.toString() );

            client.post(context,  NetworkConstants.serverAddress + API_LOGIN
                    , params,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "응답 RES = " + res);

                            try {
                                JSONObject object = new JSONObject(res);
                                String token = object.getString("token");
                                String phoneNumber = object.getString("phone");
                                User.saveLogin(context, token, email,phoneNumber);

                                Intent serviceIntent = new Intent(context, TCPListenerService.class);
                                context.startService(serviceIntent);
                                Log.e(LOG_TAG, "Started TCPListenerService.class");

                                Intent intent = new Intent(context, DialpadActivity.class);
                                context.startActivity(intent);
                                Log.i("dhtest", "Success Transmit res : "+res);//dhtest

                                ApiParamBuilder param = new ApiParamBuilder();
                                JSONObject SendObject =  param.requestConferenceInfo(phoneNumber);
                                getConference(context, SendObject);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //CharSequence sendTxt = res;
                            Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                            //Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();


                            //@TODO save login & post IP
                            //User.saveLogin();
                            //



                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "실패 : " + res);
                            //Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recovery (final Context context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            //client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.post(context,  NetworkConstants.serverAddress + API_RECOVERY
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "응답 RES = " + res);
                            try {
                                JSONObject object = new JSONObject(res);
                                String pwd = object.getString("pwd");
                                String showTxt = "Password : "+pwd;

                                //Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
                                Toast.makeText(context, showTxt, Toast.LENGTH_LONG).show();

                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "실패 : " + res);
                            //Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create (final Activity activity, JSONObject object, final String email) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
           // client.addHeader("Authorization ", "Bearer "+User.getLogin(activity));

            client.post(activity,  NetworkConstants.serverAddress + API_CREATE
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            String duplicated = "User duplicated.";

                            Log.e("tag", "응답 RES = " + res);

                            if (res.equals(duplicated))
                                //Toast.makeText(activity, "이메일 중복", Toast.LENGTH_SHORT).show();
                                Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                            else {
                                try {
                                    JSONObject object = new JSONObject(res);
                                    String phoneNumber = object.getString("phone");
                                    String showTxt = "Phone : " + phoneNumber;

                                    //Toast.makeText(activity, "생성완료", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(activity, showTxt, Toast.LENGTH_LONG).show();

                                    User.saveLogin(activity, null, email, null);
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    activity.startActivity(intent);
                                    activity.finish();

                                } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            String res = new String(responseBody);
                            Log.e("tag", "실패 : " + res);
                            //Toast.makeText(activity, "전송실패", Toast.LENGTH_SHORT).show();
                            Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update (final Activity activity, JSONObject object, final String email) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(activity));

            client.post(activity,  NetworkConstants.serverAddress + API_UPDATE
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            String duplicated = "User duplicated.";
                            Log.e("tag", "응답 RES = " + res);

                            if (res.equals(duplicated))
                                //Toast.makeText(activity, "Duplicated the e-mail", Toast.LENGTH_SHORT).show();
                                Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                            else {
                                //Toast.makeText(activity, "Complete update...", Toast.LENGTH_SHORT).show();
                                Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                                User.saveLogin(activity, null, email, null);
                                //Intent intent = new Intent(activity, LoginActivity.class);
                                //activity.startActivity(intent);
                                activity.finish();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e("tag", "실패 : " + res);
                            //Toast.makeText(activity, "전송실패", Toast.LENGTH_SHORT).show();
                            Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getIP (final Context context, JSONObject object,final VoIPVideoIo io) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.get(context,  NetworkConstants.serverAddress + API_GETIP
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e("tag", "응답 RES = " + res);

                            try {
                                JSONObject jsonObject = new JSONObject(res);
                                String ip = jsonObject.getString("ip");
                                io.attachIP(ip);
                                Intent intent = new Intent();
                                intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
                                intent.setAction(TCPCmd.GUI_VOIP_CTRL);
                                intent.putExtra("message", "/CALL_BUTTON/");
                                intent.putExtra("sender", ip);
                                context.startService(intent);

                            } catch (JSONException e) {
                                CallController.finish();
                            }

                            Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                            //Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                         //   String res = new String(responseBody);
//                            Log.e("tag", "실패 : " + res);

                            //Toast.makeText(context, "Wrong number", Toast.LENGTH_SHORT).show();
                          //  Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                           // CallController.finish();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIP (final Context context, JSONObject object, final String ip) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            //entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            // Log.e("tag", "token = " + User.getLogin(context));
            client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.post(context,  NetworkConstants.serverAddress + API_SETIP
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e("tag", "응답 RES = " + res);


                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();

                            PhoneState.getInstance().setCurrentIP(context, ip);
                            PhoneState.getInstance().setUpdatingIP(0);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            //String res = new String(responseBody);
                            //Log.e("tag", "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                            PhoneState.setUpdatingIP(0);
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void create (final Context context, JSONObject object) {
//        try {
//
//            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
//            AsyncHttpClient client = new AsyncHttpClient();
//            client.addHeader("project","voip");
//            client.addHeader("client","app");
//
//            client.post(context,  NetworkConstants.serverAddress + API_REGISTER
//                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                            String res = new String(responseBody);
//                            Log.e("tag", "응답 RES = " + res);
//
//
//                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                            String res = new String(responseBody);
//                            Log.e("tag", "실패 : " + res);
//                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
//                        }
//                    }  );
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void requestConfernceCall (final Activity activity, JSONObject object, final String startTime, final String endTime) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(activity));


            client.post(activity,  NetworkConstants.serverAddress + API_CREATE_CC
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            String phoneNumber;
                            Log.e("tag", "응답 RES = " + res);
                            Log.v("dae", "응답 RES = " + res);//dhtest

//                            ConferenceDatabaseHelper ConferenceDB = new ConferenceDatabaseHelper(getApplicationContext());
//                            ConferenceDB.insert(startTime, endTime, "1111"+mEndMinute);
//                            ConferenceDB.showList();
//                            Log.v("dae", "data : "+ConferenceDB.conferenceList.toString());


//                            Intent intent1 = new Intent(context, ConferenceActivity.class);


                            try {
                                JSONObject object = new JSONObject(res);
                                phoneNumber = object.getString("phone");
                                String startTimeDB = startTime.substring(0, 10) + " " + startTime.substring(11, 16);
                                String endTimeDB = endTime.substring(0, 10) + " " + endTime.substring(11, 16);

                                ConferenceDatabaseHelper ConferenceDB = new ConferenceDatabaseHelper(activity);
                                ConferenceDB.insert(startTimeDB, endTimeDB, phoneNumber);
                                //ConferenceDB.showList();
                                Log.v("dae", "data : "+ConferenceDB.conferenceList.toString());

                                //Toast.makeText(activity, "전송완료", Toast.LENGTH_SHORT).show();
                                String showTxt = "Success : " + phoneNumber;
                                Toast.makeText(activity, showTxt, Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Intent intent1 = new Intent(activity, ConferenceActivity.class);
                            activity.startActivity(intent1);
                            activity.finish();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e("tag", "실패 : " + res);
                            //Toast.makeText(activity, "전송실패", Toast.LENGTH_SHORT).show();
                            Toast.makeText(activity, res, Toast.LENGTH_LONG).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getConference(final Context context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.get(context,  NetworkConstants.serverAddress + API_GET_CC
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            try {
                                String res = new String(responseBody);
                                JSONArray ja = new JSONArray(res);

                                Log.e("tag", "응답 RES = " + res);
                                Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();

                                ConferenceDatabaseHelper conferenceDB = new ConferenceDatabaseHelper(context);
                                conferenceDB.deleteListAll();

                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject order = ja.getJSONObject(i);

                                    String phoneNumber = order.getString("phoneNumber");
                                    JSONObject schedule = order.getJSONObject("schedule");
                                    String from = schedule.getString("from");
                                    String to = schedule.getString("to");
                                    String startTimeDB = from.substring(0, 10) + " " + from.substring(11, 16);
                                    String endTimeDB = to.substring(0, 10) + " " + to.substring(11, 16);

                                    //dhtest
                                    Log.i("dhtest", "GetConference phone : " + phoneNumber + " from : " + startTimeDB + " to : " + endTimeDB);

                                    ConferenceDatabaseHelper ConferenceDB = new ConferenceDatabaseHelper(context);
                                    ConferenceDB.insert(startTimeDB, endTimeDB, phoneNumber);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e("tag", "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final int MAX_RETRY = 1;
    final int TIMEOUT = 10000;
    public void getIPforCC (final ConferenceCallingActivity context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(MAX_RETRY,TIMEOUT);
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.get(context,  NetworkConstants.serverAddress + API_GET_IP_CC
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e("tag", "응답 RES = " + res);
                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();

                            try {
                                JSONArray array = new JSONArray(res);
                                ArrayList<String> arrayList = new ArrayList<>();
                                for (int i =0; i<array.length() ;i++ ) {
                                   JSONObject item = (JSONObject) array.get(i);
                                   arrayList.add(item.getString("ip"));
                                   // Log.e("tag", "응답 ip = " + item.getString("ip"));
                                }
                                PhoneState.getInstance().setRemoteIPs(arrayList);
                                VoIPVideoIoCC.getInstance(context).attachIP();
                                VoIPAudioIoCC.getInstance(context).attachIP();

                                if (PhoneState.getInstance().myIndex(context) -1 ==0) {
                                    VoIPVideoIoCC.getInstance(context).attachView((ImageView)context.findViewById(R.id.cc1));
                                }
                                if (PhoneState.getInstance().myIndex(context) -1 ==1) {
                                    VoIPVideoIoCC.getInstance(context).attachView((ImageView)context.findViewById(R.id.cc2));
                                }
                                if (PhoneState.getInstance().myIndex(context) -1 ==2) {
                                    VoIPVideoIoCC.getInstance(context).attachView((ImageView)context.findViewById(R.id.cc3));
                                }
                                if (PhoneState.getInstance().myIndex(context) -1 ==3) {
                                    VoIPVideoIoCC.getInstance(context).attachView((ImageView)context.findViewById(R.id.cc4));
                                }


                                context.StartReceiveVideoThread();
                                VoIPVideoIoCC.getInstance(context).startVideo();
                                VoIPAudioIoCC.getInstance(context).StartAudio();
                            } catch (JSONException e) {
                                e.printStackTrace();
                         		CallController.endCCCall(context);
                            }
                            //////  PhoneState.getInstance().setRemoteIPs(null);

                            Toast.makeText(context, res, Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            if (responseBody!= null) {
                                String res = new String(responseBody);
                                Toast.makeText(context, res, Toast.LENGTH_LONG).show();
                            }

                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
