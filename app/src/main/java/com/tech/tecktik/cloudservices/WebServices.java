package com.tech.tecktik.cloudservices;

import com.tech.tecktik.model.PostSpeech;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface WebServices {
    String BASE_URL="http://50.30.40.213/";
    Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

    @Multipart
    @POST("audio")
    Call<PostSpeech> uploadAudio(@Part MultipartBody.Part audio);
}