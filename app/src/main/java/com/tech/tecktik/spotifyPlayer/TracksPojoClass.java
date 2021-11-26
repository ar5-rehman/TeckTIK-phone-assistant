package com.tech.tecktik.spotifyPlayer;

import android.os.Parcel;
import android.os.Parcelable;

public class TracksPojoClass implements Parcelable {

    String trackName;
    String trackAlbum;
    String trackImageSmall;
    String trackImageLarge;
    String trackPreviewUrl;
    String trackDuration;
    String trackArtist;
    String trackUrl;

    public TracksPojoClass(String trackUrl, String trackName, long trackDuration) {
        this.trackName = trackName;

        this.trackDuration = String.valueOf(trackDuration);
        this.trackUrl = trackUrl;
    }

    public TracksPojoClass(Parcel in) {
        ReadFromParcel(in);
    }

    private void ReadFromParcel(Parcel in) {
        trackName = in.readString();
        trackDuration = in.readString();
        trackUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(trackName);
        out.writeString(trackDuration);
        out.writeString(trackUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TracksPojoClass> CREATOR = new Creator<TracksPojoClass>() {
        public TracksPojoClass createFromParcel(Parcel in) {
            return new TracksPojoClass(in);
        }

        public TracksPojoClass[] newArray(int size) {
            return new TracksPojoClass[size];
        }
    };
}