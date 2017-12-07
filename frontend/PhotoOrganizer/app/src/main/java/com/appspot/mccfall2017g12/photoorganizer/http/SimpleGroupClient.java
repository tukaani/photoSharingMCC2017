package com.appspot.mccfall2017g12.photoorganizer.http;

import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;

import com.appspot.mccfall2017g12.photoorganizer.ThreadTools;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SimpleGroupClient {
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String CONTENT_TYPE = "application/json";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final com.fasterxml.jackson.databind.ObjectWriter OBJECT_WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    @AnyThread
    public static void post(final String endpoint, final Object data, final String idToken,
                            final Callback listener) {

        ThreadTools.runInWorkerThread(new Runnable() {
            @Override
            public void run() {

                Response response;

                try {
                    String json = OBJECT_WRITER.writeValueAsString(data);

                    RequestBody body = RequestBody.create(JSON, json);

                    Request request = new Request.Builder()
                            .url(endpoint)
                            .post(body)
                            .addHeader("Content-Type", CONTENT_TYPE)
                            .addHeader("Authorization", idToken)
                            .build();

                    response = CLIENT.newCall(request).execute();
                }
                catch (Exception e) {
                    fail(listener, e);
                    return;
                }

                if (response.isSuccessful())
                    success(listener);
                else
                    fail(listener, new HttpException(response.code()));
            }
        });
    }

    private static void success(final Callback listener) {
        ThreadTools.runInMainThread(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess();
            }
        });
    }

    private static void fail(final Callback listener, final Exception e) {
        ThreadTools.runInMainThread(new Runnable() {
            @Override
            public void run() {
                listener.onFailure(e);
            }
        });
    }

    private static class HttpException extends Exception {
        private final int code;

        private HttpException(int code) {
            this.code = code;
        }
    }

    public interface Callback {
        @MainThread
        void onSuccess();
        @MainThread
        void onFailure(Exception e);
    }
}
