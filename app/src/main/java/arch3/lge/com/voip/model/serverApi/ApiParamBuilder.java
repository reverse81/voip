package arch3.lge.com.voip.model.serverApi;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiParamBuilder {

    private final static String LOGTAG = "AppUp4:ApiParamBuilder";

    protected final JSONObject mMandatoryParam;

    public ApiParamBuilder() {
        mMandatoryParam = new JSONObject();
    }

    protected final static String KEY_EMAIL = "email";
    protected final static String KEY_PASSWORD = "pwd";

    protected final static String KEY_PHONENUMBER = "phone";
    protected final static String KEY_IP = "ip";


    public JSONObject getLogin(String email, String password) {
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
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PHONENUMBER, phonenumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject getIP(String phonenumber) {
        try {
            mMandatoryParam.put(KEY_PHONENUMBER, phonenumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject setIP(String ip,String phonenumber) {
        try {
            mMandatoryParam.put(KEY_IP, ip);
            mMandatoryParam.put(KEY_PHONENUMBER, phonenumber);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }
}
