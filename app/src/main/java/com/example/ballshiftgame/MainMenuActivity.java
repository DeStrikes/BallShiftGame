package com.example.ballshiftgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jndcjdcjn123.R;

public class MainMenuActivity extends AppCompatActivity {

    ImageView playButton, quitButton, menuLogo;
    TextView bestScoreText;
    private static final String PREFS_NAME = "MyPreferences";
    private static final String BEST_SCORE_KEY = "best_score";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        updateBestScore();
    }

    private void initLayout() {
        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        bestScoreText = new TextView(this);
        bestScoreText.setTextSize(20);
        bestScoreText.setTextColor(Color.WHITE);
        bestScoreText.setText("Рекорд: 0");
        FrameLayout.LayoutParams paramsScore = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsScore.gravity = Gravity.CENTER_HORIZONTAL;
        paramsScore.topMargin = height / 2 - 200;
        bestScoreText.setLayoutParams(paramsScore);
        frameLayout.addView(bestScoreText);
        playButton = new ImageView(this);
        playButton.setImageResource(R.drawable.play_button);
        FrameLayout.LayoutParams paramsPlay = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsPlay.gravity = Gravity.CENTER_HORIZONTAL;
        paramsPlay.topMargin = height / 2 - 100;
        playButton.setLayoutParams(paramsPlay);
        frameLayout.addView(playButton);
        playButton.setOnClickListener(v -> startGame());
        quitButton = new ImageView(this);
        quitButton.setImageResource(R.drawable.quit_button);
        FrameLayout.LayoutParams paramsQuit = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsQuit.gravity = Gravity.CENTER_HORIZONTAL;
        paramsQuit.topMargin = height / 2 + width * 40 / 200;
        quitButton.setLayoutParams(paramsQuit);
        frameLayout.addView(quitButton);
        quitButton.setOnClickListener(v -> quitGame());
        frameLayout.setBackgroundColor(android.graphics.Color.BLACK);
        menuLogo = new ImageView(this);
        menuLogo.setImageResource(R.drawable.menu_logo);
        FrameLayout.LayoutParams paramsLogo = new FrameLayout.LayoutParams(width * 80 / 100, width * 80 / 200);
        paramsLogo.gravity = Gravity.CENTER_HORIZONTAL;
        paramsLogo.topMargin = 300;
        menuLogo.setLayoutParams(paramsLogo);
        frameLayout.addView(menuLogo);
    }

    private void updateBestScore() {
        int bestScore = loadBestScore();
        bestScoreText.setText("Рекорд: " + bestScore);
    }

    private int loadBestScore() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(BEST_SCORE_KEY, 0);
    }

    private void startGame() {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void quitGame() {
        finish();
        System.exit(0);
    }
}
