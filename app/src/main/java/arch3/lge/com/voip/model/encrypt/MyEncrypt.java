package arch3.lge.com.voip.model.encrypt;

import android.util.Base64;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class MyEncrypt extends AbstractEncryt {
    protected   static String serverKey = "KKF2QT4fwpMeJf36POk6yJVHTAEPAPMY";
    protected   static String clientKey = "KKF2QT4fwpMeJf36POk6yJVHTAEPAPMY";

    public String encrypt(String strToEncrypt)
    {
        try
        {
            SecretKeySpec secretKey =  new SecretKeySpec(serverKey.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")), 0);

        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public byte[] encrypt(byte[] bytesToEncrypt)
    {
        try
        {
            SecretKeySpec secretKey =  new SecretKeySpec(clientKey.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(bytesToEncrypt);

        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public String decrypt(String strToEncrypt)
    {
        try
        {
            SecretKeySpec secretKey =  new SecretKeySpec(serverKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decode(strToEncrypt,0)),"UTF-8");

        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public byte[] decrypt(byte[] bytesToEncrypt,int offset, int length)
    {
        try
        {

            bytesToEncrypt = Arrays.copyOfRange(bytesToEncrypt, offset, offset+length);
            SecretKeySpec secretKey =  new SecretKeySpec(clientKey.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(bytesToEncrypt);

        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
}
