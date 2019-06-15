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

import java.io.UnsupportedEncodingException;

import arch3.lge.com.voip.model.UDPnetwork.UDPCmd;
import arch3.lge.com.voip.model.codec.VoIPVideoIo;
import arch3.lge.com.voip.model.encrypt.MyEncrypt;
import arch3.lge.com.voip.model.user.User;
import arch3.lge.com.voip.utils.NetworkConstants;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ServerApi {

    public final static String API_LOGIN = "auth/login";
    public final static String API_RECOVERY = "users/recovery"; // post
    public final static String API_GETIP = "users/ip";  //get
    public final static String API_SETIP = "users/ip";   //post

    public void login (final Context context, String source) {
        try {
            MyEncrypt encipher = new MyEncrypt();
            String text = encipher.encrypt(source);
            Log.i("tag", encipher.decrypt(text));

            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","voip");
            client.addHeader("client","app");

            RequestParams params = new RequestParams();
            params.put("hashed_string", text);
            Log.i("tag",             params.toString() );

            client.post(context,  NetworkConstants.serverAddress + API_LOGIN
                    , params,  new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res = new String(responseBody);
                    Log.e("tag", "응답 RES = " + res);



                    Toast.makeText(context, "전송완료", Toast.LENGTH_SHORT).show();

//                    if (RES_CODE.equals("0000")) {
//                        Intent intent = new Intent(MainActivity.this, Login_success.class);
//                        context.startActivity(intent);
//                    } else {
//                        Toast.makeText(MainActivity.this, "로그인 실패 : " + RES_MSG, Toast.LENGTH_SHORT).show();
//                    }

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

    public void recovery (final Context context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization ", "Bearer "+User.getLogin(context));

            client.post(context,  NetworkConstants.serverAddress + API_RECOVERY
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

    public void getIP (final Context context, JSONObject object,final VoIPVideoIo io) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization ", "Bearer "+User.getLogin(context));

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
                            intent.setClassName(context.getPackageName(), UDPCmd.class.getName());
                            intent.setAction(UDPCmd.GUI_VOIP_CTRL);
                            intent.putExtra("message", "/CALLIP/");
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
                                intent.setClassName(context.getPackageName(), UDPCmd.class.getName());
                                intent.setAction(UDPCmd.GUI_VOIP_CTRL);
                                intent.putExtra("message", "/CALLIP/");
                                intent.putExtra("sender", ip);
                                context.startService(intent);


                        }
                    }  );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIP (final Context context, JSONObject object) {
        try {

            StringEntity entity = new StringEntity(object.toString(), "UTF-8");
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","voip");
            client.addHeader("client","app");
            client.addHeader("Authorization ", "Bearer "+User.getLogin(context));

            client.post(context,  NetworkConstants.serverAddress + API_GETIP
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
