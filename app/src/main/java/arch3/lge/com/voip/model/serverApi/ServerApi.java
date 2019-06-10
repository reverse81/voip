package arch3.lge.com.voip.model.serverApi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Observable;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ServerApi {

    public final static String API_LOGIN = "auth/login";

    public void login (final Context context, String api, JSONObject jsonObject) {
        try {
            StringEntity entity = new StringEntity(jsonObject.toString());
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("project","app");
            client.post(context,  NetworkConstants.serverAddress + api
                    , entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res = new String(responseBody);
                    Log.e("tag", "응답 RES = " + res);

//                    String RES_CODE = null;
//                    String RES_MSG = null;
//
//                    JSONObject res_jsonObject = null;
//                    JSONObject res_jsonObject_old = null;
//
//                    try {
//                        res_jsonObject_old = new JSONObject(res);
//                        res_jsonObject = res_jsonObject_old.getJSONObject("head");
//                        RES_CODE = res_jsonObject.getString("RES_CODE");
//                        RES_MSG = res_jsonObject.getString("RES_MSG");
//                        Log.e("tag", "결과 Response CODE = " + RES_CODE);
//                        Log.e("tag", "결과 Response MSG = " + RES_MSG);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

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
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
