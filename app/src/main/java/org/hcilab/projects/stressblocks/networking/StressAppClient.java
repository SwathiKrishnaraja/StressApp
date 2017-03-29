package org.hcilab.projects.stressblocks.networking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import org.hcilab.projects.stressblocks.R;
import org.hcilab.projects.stressblocks.TetrisActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class StressAppClient {

    private Context context;

    public StressAppClient(Context context) {
        this.context = context;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        return result;
    }

    private SSLSocketFactory getSocketFactory() throws Exception {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream inputStream = context.getResources().openRawResource(R.raw.server);
        InputStream caInput = new BufferedInputStream(inputStream);
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            Log.d("Certificate", "ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    public void getScores(Handler.Callback callback, String username) {
        String url = "score?top=10&deviceid=" + Settings.Secure.getString(context
                        .getContentResolver(),
                Settings.Secure.ANDROID_ID)+"&username=" + username;
        new GetTask(callback).execute(url);
    }

    public boolean sendNotificationEvent(JSONObject body, Handler.Callback callback) {
        try {
            body.put("deviceid", Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            body.put("id", UUID.randomUUID().toString());
        } catch (JSONException e) {
        }
        String data = "data=" + body.toString();
        new PostTask("event", callback).execute(data);
        return true;
    }

    public boolean sendSurveyAnswers(String answers, Handler.Callback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("deviceid", Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            body.put("answers", answers);
            body.put("timestamp", TetrisActivity.dateFormat.format(new Date()));
        } catch (JSONException e) {
        }
        String data = "data=" + body.toString();
        new PostTask("survey", callback).execute(data);
        return true;
    }

    public boolean sendStressLevel(int value, String rating, Handler.Callback callback) {
        JSONObject data = new JSONObject();
        try {
            data.put("deviceid", Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID))
                    .put("value", value)
                    .put("rating", rating)
                    .put("timestamp", TetrisActivity.dateFormat.format(new Date()));
        } catch (JSONException e) {
        }
        String result = "data=" + data.toString();
        new PostTask("stresslevel", callback).execute(result);
        return true;
    }

    public boolean sendScore(Context context, int value, String username) {
        JSONObject data = new JSONObject();
        try {
            data.put("deviceid", Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID))
                    .put("value", value)
                    .put("username", username)
                    .put("timestamp", TetrisActivity.dateFormat.format(new Date()));
        } catch (JSONException e) {
        }
        String result = "data=" + data.toString();
        new PostTask("score", null).execute(result);
        return true;
    }

    public class PostTask extends AsyncTask<String, Void, Boolean> {
        private String endpoint;
        private Handler.Callback callback;


        public PostTask(String endpoint, Handler.Callback callback) {
            this.endpoint = endpoint;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... data) {
            HttpsURLConnection client = null;
            try {
                SSLSocketFactory sslSocketFactory = getSocketFactory();
                URL url = new URL("https://129.69.197.6/" + endpoint);
                client = (HttpsURLConnection) url.openConnection();
                client.setSSLSocketFactory(sslSocketFactory);
                client.setRequestMethod("POST");
                client.setDoOutput(true);
                client.setDoInput(true);
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;" +
                        "charset=UTF-8");
                client.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession sslSession) {
                        return hostname.equals("129.69.197.6");
                    }
                });

                OutputStream os = client.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data[0]);
                writer.flush();
                writer.close();

                InputStream in = new BufferedInputStream(client.getInputStream());
                Log.d("Client request", "POST /" + endpoint + ": " + data[0]);
                String response = convertInputStreamToString
                        (in);
                Log.d("Backend response", "POST /" + endpoint + ": " + response);
                in.close();
                os.close();
            } catch (Exception e) {
                Log.e("Network exception", e.getClass().toString() + " " + e.getMessage());
                return false;
            } finally {
                if (client != null) {
                    client.disconnect();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (callback != null) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                Log.d("sent", aBoolean.booleanValue() + "");
                bundle.putBoolean("sent", aBoolean.booleanValue());
                message.setData(bundle);
                callback.handleMessage(message);
            }
        }
    }

    public class GetTask extends AsyncTask<String, Void, String> {
        private Handler.Callback callback;

        public GetTask(Handler.Callback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            String endpoint = params[0];
            HttpsURLConnection client = null;
            String response = "";
            try {
                SSLSocketFactory sslSocketFactory = getSocketFactory();
                URL url = new URL("https://129.69.197.6/" + endpoint);
                client = (HttpsURLConnection) url.openConnection();
                client.setSSLSocketFactory(sslSocketFactory);
                client.setRequestMethod("GET");
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;" +
                        "charset=UTF-8");
                client.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession sslSession) {
                        return hostname.equals("129.69.197.6");
                    }
                });

                InputStream in = new BufferedInputStream(client.getInputStream());
                response = convertInputStreamToString(in);
                Log.d("Client request", "GET /" + endpoint);
                Log.d("Backend response", "GET /" + endpoint + ": " + response);
                in.close();
            } catch (Exception e) {
                Log.e("Network exception", e.getClass().toString() + " " + e.getMessage());
                return "Network exception";
            } finally {
                if (client != null) {
                    client.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("response", response);
            message.setData(bundle);
            callback.handleMessage(message);
        }
    }
}
