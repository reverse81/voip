package arch3.lge.com.voip.model.serverApi;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arch3.lge.com.voip.model.user.User;
import arch3.lge.com.voip.utils.NetworkConstants;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ApiParamBuilder {

    private final static String LOGTAG = "AppUp4:ApiParamBuilder";

   // protected final JSONObject mMandatoryParam;

    public ApiParamBuilder() {

    }

    protected final static String KEY_EMAIL = "email";
    protected final static String KEY_PASSWORD = "pwd";

    protected final static String KEY_PHONENUMBER = "phone";
    protected final static String KEY_IP = "ip";

    protected final static String KEY_PARTICIPANTS = "participants";
    protected final static String KEY_FROM = "from";
    protected final static String KEY_TO = "to";

    public JSONObject getLogin(String email, String password) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PASSWORD, password);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject getRecovery(String email, String phonenumber) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PHONENUMBER, phonenumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject getCreate(String email, String password) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PASSWORD, password);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject getPhoneParam(String phoneNumber) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_PHONENUMBER, phoneNumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject setIP(String ip,String phonenumber) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_IP, ip);
            mMandatoryParam.put(KEY_PHONENUMBER, phonenumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject requestCC(String[] parti,String from, String to) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            StringBuilder sb = new StringBuilder();
            for (String value : parti) {
                sb.append(value);
                sb.append(",");
            }
            mMandatoryParam.put(KEY_PARTICIPANTS,sb.substring(0,sb.length()-1));
            mMandatoryParam.put(KEY_FROM, from);
            mMandatoryParam.put(KEY_TO, to);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }
}
