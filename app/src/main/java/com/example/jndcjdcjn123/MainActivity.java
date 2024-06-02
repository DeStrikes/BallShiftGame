package com.example.jndcjdcjn123;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GameControlInterface {

    MySurfaceView mySurfaceView;
    FrameLayout frameLayout;
    ImageView pauseButton, retryButton, menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //hideSystemUI();
        initLayout();
        pauseButton.callOnClick();
        int bestScore = loadBestScore();
        if (mySurfaceView != null) {
            mySurfaceView.setBestScore(bestScore);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mySurfaceView != null) {
            mySurfaceView.pause();
        }
        if (mySurfaceView != null) {
            saveBestScore(mySurfaceView.getBestScore());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mySurfaceView != null) {
            mySurfaceView.resume();
        }
    }

    private void hideSystemUI() {
        Window w = getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void initLayout() {
        frameLayout = new FrameLayout(this);
        setContentView(frameLayout);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels, height = displayMetrics.heightPixels;
        mySurfaceView = new MySurfaceView(this, width, height, this); // Передаем интерфейс
        frameLayout.addView(mySurfaceView);

        pauseButton = new ImageView(this);
        pauseButton.setImageResource(R.drawable.pause_button);
        FrameLayout.LayoutParams paramsPause = new FrameLayout.LayoutParams(100, 100);
        paramsPause.leftMargin = 50;
        paramsPause.topMargin = 50;
        pauseButton.setLayoutParams(paramsPause);
        frameLayout.addView(pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySurfaceView.updatePause();
                updatePauseMenuVisibility();
            }
        });

        retryButton = new ImageView(this);
        retryButton.setImageResource(R.drawable.retry_button);
        FrameLayout.LayoutParams paramsRetry = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsRetry.leftMargin = (width - width * 40 / 100) / 2;
        paramsRetry.topMargin = height / 2;
        retryButton.setLayoutParams(paramsRetry);
        frameLayout.addView(retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySurfaceView.restartGame();
                updatePauseMenuVisibility();
            }
        });

        menuButton = new ImageView(this);
        menuButton.setImageResource(R.drawable.menu_button);
        FrameLayout.LayoutParams paramsMenu = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsMenu.leftMargin = (width - width * 40 / 100) / 2;
        paramsMenu.topMargin = height / 2 + width * 40 / 200 + 100;
        menuButton.setLayoutParams(paramsMenu);
        frameLayout.addView(menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Will be added soon");
            }
        });
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void updatePauseMenuVisibility() {
        int visibility = mySurfaceView.paused || mySurfaceView.gameOver ? View.VISIBLE : View.INVISIBLE;
        retryButton.setVisibility(visibility);
        menuButton.setVisibility(visibility);
        pauseButton.setVisibility(mySurfaceView.gameOver ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void updatePauseVisibility() {
        runOnUiThread(this::updatePauseMenuVisibility);
    }

    private void saveBestScore(int bestScore) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("best_score", bestScore);
        editor.apply();
    }

    private int loadBestScore() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("best_score", 0);
    }
}
