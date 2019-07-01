package arch3.lge.com.voip.model.serverApi;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ApiParamBuilder {

    private final static String LOGTAG = "AppUp4:ApiParamBuilder";

   // protected final JSONObject mMandatoryParam;

    public ApiParamBuilder() {

    }

    protected final static String KEY_EMAIL = "email";
    protected final static String KEY_PASSWORD = "pwd";
    protected final static String KEY_PASSWORD_NEW = "new_pwd";
    protected final static String KEY_PASSWORD_RECOVERY = "recovery";

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

    public JSONObject getRecovery(String email, String phone, JSONObject recovery) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PHONENUMBER, phone);
            mMandatoryParam.put(KEY_PASSWORD_RECOVERY, recovery);
            Log.i("dhtest", mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on getRetrieveApplistParam...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject getCreate(String email, String password, JSONObject recovery) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_EMAIL, email);
            mMandatoryParam.put(KEY_PASSWORD, password);
            mMandatoryParam.put(KEY_PASSWORD_RECOVERY, recovery);
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

    //{"phoneNumber":"08055461638","schedule":{"from":"2019-06-25T16:30:00.000Z","to":"2019-06-25T16:50:00.000Z"}}
    public JSONObject requestCC(ArrayList<String> parti, String from, String to) {
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

    public JSONObject updateAccountInfo(String phoneNum, String email, String password, String newPassword) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_PHONENUMBER,phoneNum);
            if(email != null)
                mMandatoryParam.put(KEY_EMAIL,email);
            mMandatoryParam.put(KEY_PASSWORD,password);
            if (newPassword != null)
                mMandatoryParam.put(KEY_PASSWORD_NEW, newPassword);
            Log.i(LOGTAG, mMandatoryParam.toString());
            Log.v("dae", mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on updateAccountInfo...", e);
        }
        return mMandatoryParam;
    }

    public JSONObject requestConferenceInfo(String phoneNum) {
        JSONObject mMandatoryParam = new JSONObject();
        try {
            mMandatoryParam.put(KEY_PHONENUMBER,phoneNum);
            Log.i(LOGTAG, mMandatoryParam.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, "JSONException on updateAccountInfo...", e);
        }
        return mMandatoryParam;
    }
}
