package com.tec.medxpert.utils;

import android.content.Context;

import android.util.Log;

import com.tec.medxpert.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailService {

    private static final String TAG = "EmailService";
    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send";
    private static final String SERVICE_ID = "service_nc3s2jm";
    private static final String TEMPLATE_ID = "template_ft6qfik";
    private static final String PUBLIC_KEY = "q3VYLRK-aemMc0_Dv";

    public static void sendEmail(Context context, String userEmail, String securityCode) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");

        try {
            JSONObject json = new JSONObject();
            json.put("service_id", SERVICE_ID);
            json.put("template_id", TEMPLATE_ID);
            json.put("user_id", PUBLIC_KEY);

            JSONObject templateParams = new JSONObject();
            templateParams.put("user_email", userEmail);
            templateParams.put("security_code", securityCode);

            json.put("template_params", templateParams);

            RequestBody body = RequestBody.create(mediaType, json.toString());

            Request request = new Request.Builder()
                    .url(EMAILJS_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            new Thread(() -> {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        assert response.body() != null;
                        Log.e(TAG, context.getString(R.string.email_service_error, response.body().string()));
                    } else {
                        Log.d(TAG, context.getString(R.string.email_service_success, userEmail));
                    }
                } catch (IOException e) {
                    Log.e(TAG, context.getString(R.string.email_service_error, e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, context.getString(R.string.email_service_failure, e.getMessage()));
        }
    }

}
