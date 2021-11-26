package com.tech.tecktik.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import com.tech.tecktik.R;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final int SPLASH_DISPLAY_LENGTH = 5000;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent m_intent = new Intent();
        m_intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(m_intent, 122);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);

                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 122) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // the user has the necessary data - create the TTS
                textToSpeech = new TextToSpeech(this, this);
            } else {
                // no data - install it now
                Intent m_intnt = new Intent();
                m_intnt.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(m_intnt);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.speak("Welcome to tech talk", TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}