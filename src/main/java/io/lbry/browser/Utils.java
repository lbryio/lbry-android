package io.lbry.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import io.lbry.browser.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.json.JSONObject;
import org.json.JSONException;

public final class Utils {

    private static final String TAG = Utils.class.getName();

    private static final String AES_MODE = "AES/ECB/PKCS7Padding";

    private static final String KEY_ALIAS = "LBRYKey";

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";

    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";

    private static final String SP_NAME = "app";

    private static final String SP_ENCRYPTION_KEY = "key";

    private static final String SP_API_SECRET_KEY = "api_secret";

    public static final String SDK_URL = "http://127.0.0.1:5279";

    public static final String SP_DOWNLOAD_DIR_KEY = "download_dir";

    public static String getAndroidRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static int getAndroidSdk() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String getFilesDir(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getAppInternalStorageDir(Context context) {
        File[] dirs = context.getExternalFilesDirs(null);
        return dirs[0].getAbsolutePath();
    }

    public static String getAppExternalStorageDir(Context context) {
        File[] dirs = context.getExternalFilesDirs(null);
        if (dirs.length > 1) {
            return dirs[1].getAbsolutePath();
        }
        return null;
    }

    public static String getInternalStorageDir(Context context) {
        String appInternal = getAppInternalStorageDir(context);
        return writableRootForPath(appInternal);
    }

    public static String getExternalStorageDir(Context context) {
        String appExternal = getAppInternalStorageDir(context);
        if (appExternal == null) {
            return null;
        }

        return writableRootForPath(appExternal);
    }

    public static String writableRootForPath(String path) {
        File file = new File(path);
        while (file != null && file.canWrite()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.canWrite()) {
                break;
            }
            file = parent;
        }

        return file.getAbsolutePath();
    }

    public static String getConfiguredDownloadDirectory(Context context) {
        // use the default folder (usually [private files]/Download with WRITE_EXTERNAL_STOAGE permission not granted)
        // if none is configured or specified
        String defaultDirectory = String.format("%s/Download", getInternalStorageDir(context));

        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String downloadDirectory = pref.getString(SP_DOWNLOAD_DIR_KEY, defaultDirectory);
        return downloadDirectory;
    }

    public static void saveApiSecret(String secret, Context context, KeyStore keyStore) {
        try {
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(SP_API_SECRET_KEY, encrypt(secret.getBytes(), context, keyStore));
            editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, "lbrynetservice - Could not save the API secret", ex);
        }
    }

    public static String loadApiSecret(Context context, KeyStore keyStore) {
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String encryptedSecret = pref.getString(SP_API_SECRET_KEY, null);
        if (encryptedSecret != null && encryptedSecret.trim().length() > 0) {
            try {
                byte[] decoded = Base64.decode(encryptedSecret, Base64.DEFAULT);
                return new String(decrypt(decoded, context, keyStore), Charset.forName("UTF8"));
            } catch (Exception ex) {
                Log.e(TAG, "lbrynetservice - Could not load the API secret", ex);
            }
        }

        return null;
    }

    public static void setSecureValue(String key, String value, Context context, KeyStore keyStore) {
        try {
            String encryptedValue = encrypt(value.getBytes(), context, keyStore);
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(key, encryptedValue);
            editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, "utils - Could not set a secure value", ex);
        }
    }

    public static String getSecureValue(String key, Context context, KeyStore keyStore) {
        try {
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            String encryptedValue = pref.getString(key, null);

            if (encryptedValue == null || encryptedValue.trim().length() == 0) {
                return null;
            }

            byte[] decoded = Base64.decode(encryptedValue, Base64.DEFAULT);
            return new String(decrypt(decoded, context, keyStore), Charset.forName("UTF8"));
        } catch (Exception ex) {
            Log.e(TAG, "utils - Could not retrieve a secure value", ex);
        }

        return null;
    }

    public static void setPassword(String serviceName, String username, String password, Context context, KeyStore keyStore) {
        try {
            String encryptedUsername = String.format("u_%s_%s", serviceName, encrypt(username.getBytes(), context, keyStore));
            String encryptedPassword = encrypt(password.getBytes(), context, keyStore);
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(encryptedUsername, encryptedPassword);
            editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, "lbrynetservice - Could not set a password", ex);
        }
    }

    public static String getPassword(String serviceName, String username, Context context, KeyStore keyStore) {
        try {
            String encryptedUsername = String.format("u_%s_%s", serviceName, encrypt(username.getBytes(), context, keyStore));
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            String encryptedPassword = pref.getString(encryptedUsername, null);
            if (encryptedPassword == null || encryptedPassword.trim().length() == 0) {
                return null;
            }

            byte[] decoded = Base64.decode(encryptedPassword, Base64.DEFAULT);
            return new String(decrypt(decoded, context, keyStore), Charset.forName("UTF8"));
        } catch (Exception ex) {
            Log.e(TAG, "lbrynetservice - could not decrypt password for user", ex);
        }

        return null;
    }

    public static void deletePassword(String serviceName, String username, Context context, KeyStore keyStore) {
        try {
            String encryptedUsername = String.format("u_%s_%s", serviceName, encrypt(username.getBytes(), context, keyStore));
            SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.remove(encryptedUsername);
            editor.commit();
        }  catch (Exception ex) {
            Log.e(TAG, "lbrynetservice - Could not delete a password", ex);
        }
    }

    public static String encrypt(byte[] input, Context context, KeyStore keyStore) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context, keyStore));
        return Base64.encodeToString(c.doFinal(input), Base64.DEFAULT);
    }

    public static byte[] decrypt(byte[] encrypted, Context context, KeyStore keyStore) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE, "BC");
        c.init(Cipher.DECRYPT_MODE, getSecretKey(context, keyStore));
        return c.doFinal(encrypted);
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static final KeyStore initKeyStore(Context context) throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER);
        ks.load(null);

        if (!ks.containsAlias(KEY_ALIAS)) {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 100);

            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(new X500Principal(String.format("CN=%s", KEY_ALIAS)))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

            try {
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA", KEYSTORE_PROVIDER);
                keygen.initialize(spec);
                keygen.generateKeyPair();
            } catch (NoSuchProviderException ex) {
                throw ex;
            } catch (InvalidAlgorithmParameterException ex) {
                throw ex;
            }
        }

        return ks;
    }

    public static String performRequest(String url) throws ConnectException {
        return performRequest(url, "GET", null);
    }

    public static String performRequest(String requestUrl, String requestMethod, String json) throws ConnectException {
        BufferedReader reader = null;
        DataOutputStream dos = null;
        HttpURLConnection conn = null;

        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod(requestMethod);
            if ("POST".equals(requestMethod)) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-type", "application/json");
            }

            if (json != null) {
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(json);
                dos.flush();
                dos.close();
            }

            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String input;
                while ((input = reader.readLine()) != null) {
                    sb.append(input);
                }

                return sb.toString();
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String error;
                while ((error = reader.readLine()) != null) {
                    sb.append(error);
                }
                return sb.toString();
            }
        } catch (ConnectException ex) {
            // unable to connect. rethrow
            throw ex;
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            // pass
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            // pass
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException ex) {
                // pass
            }
        }

        return null;
    }

    public static String sdkCall(String method) throws ConnectException {
        return sdkCall(method, null);
    }

    public static String sdkCall(String method, Map<String, String> params) throws ConnectException {
        try {
            JSONObject request = new JSONObject();
            request.put("method", method);
            if (params != null) {
                JSONObject requestParams = new JSONObject();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    requestParams.put(entry.getKey(), entry.getValue());
                }
                request.put("params", requestParams);
            }

            return performRequest(SDK_URL, "POST", request.toString());
        } catch (ConnectException ex) {
            // sdk not started yet. rethrow
            throw ex;
        } catch (JSONException ex) {
            // normally shouldn't happen
            Log.e(TAG, ex.getMessage(), ex);
            // pass
        }

        return null;
    }

    private static byte[] rsaEncrypt(byte[] secret, KeyStore keyStore) throws Exception {
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
            publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
        } else {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            privateKey = privateKeyEntry.getPrivateKey();
            publicKey = privateKeyEntry.getCertificate().getPublicKey();
        }

        if (publicKey == null) {
            throw new Exception("Could not obtain public key for encryption.");
        }

        // Encrypt the text
        Cipher inputCipher = Cipher.getInstance(RSA_MODE);
        inputCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        return outputStream.toByteArray();
    }

    private static byte[] rsaDecrypt(byte[] encrypted, KeyStore keyStore) throws Exception {
        PrivateKey privateKey = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
        } else {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            privateKey = privateKeyEntry.getPrivateKey();
        }

        if (privateKey == null) {
            throw new Exception("Could not obtain private key for decryption");
        }

        Cipher output = Cipher.getInstance(RSA_MODE);
        output.init(Cipher.DECRYPT_MODE, privateKey);
        CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<Byte>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte) nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }
        return bytes;
    }

    private static String generateSecretKey(Context context, KeyStore keyStore) throws Exception {
        byte[] key = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);

        byte[] encryptedKey = rsaEncrypt(key, keyStore);
        String base64Encrypted = Base64.encodeToString(encryptedKey, Base64.DEFAULT);

        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SP_ENCRYPTION_KEY, base64Encrypted);
        editor.commit();

        return base64Encrypted;
    }

    private static Key getSecretKey(Context context, KeyStore keyStore) throws Exception{
        SharedPreferences pref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String base64Key = pref.getString(SP_ENCRYPTION_KEY, null);
        if (base64Key == null || base64Key.trim().length() == 0) {
            base64Key = generateSecretKey(context, keyStore);
        }
        return new SecretKeySpec(rsaDecrypt(Base64.decode(base64Key, Base64.DEFAULT), keyStore), "AES");
    }

    public static String capitalizeAndStrip(String text) {
        String[] parts = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }

        return sb.toString();
    }
}
