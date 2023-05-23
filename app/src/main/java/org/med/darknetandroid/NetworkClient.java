package org.med.darknetandroid;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//This class initializes a Retrofit instance from an OKHttpClient to be later used for image upload
public class NetworkClient {
    public static Retrofit retrofit;
    public static String baseUrl = "http://8823-77-42-248-5.ngrok-free.app/";
    public static Retrofit getRetrofit(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        if(retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
        }
        return retrofit;
    }
}
