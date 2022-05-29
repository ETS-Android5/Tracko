package com.kaavya.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

    private static OkHttpClient client = null;

    public static void reset() {
        client = null;
    }

    private static OkHttpClient getClient() {
        if (client != null) {
            return client;
        }

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .cookieJar(new CookieStore());

//        if(ApiGlobals.isLogHttpCalls())
//              HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//              logging.setLevel(HttpLoggingInterceptor.Level.BODY);

//            clientBuilder = clientBuilder.addInterceptor(logging);
//        }

        clientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        clientBuilder.readTimeout(60, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS);

        client = clientBuilder.build();
        return client;
    }

    public static String postRequest(String url, String dataType, String data) {
        try {
            OkHttpClient client = getClient();

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse(dataType), data);

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "<Error>HttpConnection returned " + response.code() + "</Error>";
            }

            String respStr = response.body().string();
            if(respStr.trim().length() == 0) {
                respStr =  "<Error>Empty Response</Error>";
            }
            return respStr;

        } catch(Exception ex) {
            ex.printStackTrace();
            return  "<Error>HttpConnection returned " + ex.getMessage() + "</Error>";
        }
    }

    public static String postFormRequest(String url, List<FormPostData> formData) {
        try {
            OkHttpClient client = getClient();

            FormBody.Builder formBody = new FormBody.Builder();
            for(FormPostData postData: formData) {
                formBody.add(postData.getName(), postData.getValue());
            }

            FormBody requestBody = formBody.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "<Error>HttpConnection returned " + response.code()+ "</Error>";
            }

            String respStr = response.body().string();
            if(respStr.trim().length() == 0) {
                respStr = "<Error>Empty Response</Error>";
            }
            return respStr;

        } catch(Exception ex) {
            ex.printStackTrace();
            return "<Error>HttpConnection returned " + ex.getMessage() + "</Error>";
        }
    }

    public static String postFiles(String url, ArrayList<FileToPost> files) {
        try {
            OkHttpClient client = getClient();
            MultipartBody.Builder multiPartBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            for(FileToPost file: files) {
                multiPartBodyBuilder = multiPartBodyBuilder.addFormDataPart(file.getName(), file.getFileName(),
                            RequestBody.create(file.getMediaType(), file.getFileData()));
            }

            RequestBody requestBody = multiPartBodyBuilder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "<Error>HttpConnection returned " + response.code() + "</Error>";
            }

            String respStr = response.body().string();
            if(respStr.trim().length() == 0) {
                respStr = "<Error>Empty Response</Error>";
            }
            return respStr;

        } catch(Exception ex) {
            ex.printStackTrace();
            return "<Error>HttpConnection returned " + ex.getMessage() + "</Error>";
        }
    }
}
