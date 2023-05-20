package org.med.darknetandroid;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    public static Retrofit retrofit;
    public static String baseUrl = "http://192.168.130.24:8080/";

    public static Retrofit getRetrofit(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if(retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }
        return retrofit;
    }
}
