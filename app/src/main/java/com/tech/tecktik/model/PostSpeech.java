package com.tech.tecktik.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostSpeech {

    @SerializedName("Transcription")
    @Expose
    private String Transcription;

    public String getTranscription() {
        return Transcription;
    }

    /*@SerializedName("Error")
    @Expose
    private String error;

    public String getError() {
        return error;
    }*/

}
