package arch3.lge.com.voip.model.serverApi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import arch3.lge.com.voip.listener.TCPListenerService;
import arch3.lge.com.voip.model.UDPnetwork.TCPCmd;
import arch3.lge.com.voip.model.call.PhoneState;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.model.user.User;
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

    public void login (final Context context, String source,final String email) {
        try {
            MyEncrypt encipher = new MyEncrypt();
            String text = encipher.encrypt(source);
            Log.i(LOG_TAG, encipher.decrypt(text));

            AsyncHttpClient client = new AsyncHttpClient();
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();


                            //@TODO save login & post IP
                            //User.saveLogin();
                            //



                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
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
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization", "Bearer "+User.getLogin(context));

            client.post(context,  NetworkConstants.serverAddress + API_RECOVERY
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "응답 RES = " + res);


                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            String res = new String(responseBody);
                            Log.e(LOG_TAG, "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
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

                            }

                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                            String res = new String(responseBody);
//                            Log.e("tag", "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                            //JSONObject jsonObject = new JSONObject(res);
                            // String ip = jsonObject.getString("ip");
                            // io.attachIP(ip);
                            String ip = "1.1.1.1";
                            Intent intent = new Intent();
                            intent.setClassName(context.getPackageName(), TCPCmd.class.getName());
                            intent.setAction(TCPCmd.GUI_VOIP_CTRL);
                            intent.putExtra("message", "/CALLIP/");
                            intent.putExtra("sender", ip);
                            context.startService(intent);


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
                            String res = new String(responseBody);
                            Log.e("tag", "실패 : " + res);
                            Toast.makeText(context, "전송실패", Toast.LENGTH_SHORT).show();
                            PhoneState.setUpdatingIP(0);
                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create (final Context context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","voip");
            client.addHeader("client","app");

            client.post(context,  NetworkConstants.serverAddress + API_REGISTER
                    , entity, NetworkConstants.ContentsType,  new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String res = new String(responseBody);
                            Log.e("tag", "응답 RES = " + res);


                            Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();
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
}
