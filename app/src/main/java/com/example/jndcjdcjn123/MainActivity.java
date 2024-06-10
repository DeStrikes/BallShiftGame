package com.example.jndcjdcjn123;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GameControlInterface {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "MyPreferences";
    private static final String BEST_SCORE_KEY = "best_score";

    MySurfaceView mySurfaceView;
    FrameLayout frameLayout;
    ImageView pauseButton, retryButton, menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                initLayout();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(frameLayout);
                        int bestScore = loadBestScore();
                        if (mySurfaceView != null) {
                            mySurfaceView.setBestScore(bestScore);
                        }
                        updatePauseMenuVisibility();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mySurfaceView != null) {
            mySurfaceView.pause();
            saveBestScore(mySurfaceView.getBestScore());
            Log.d(TAG, "Best score saved on pause: " + mySurfaceView.getBestScore());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mySurfaceView != null) {
            mySurfaceView.resume();
        }
    }

    private void initLayout() {
        frameLayout = new FrameLayout(MainActivity.this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels, height = displayMetrics.heightPixels;
        mySurfaceView = new MySurfaceView(MainActivity.this, width, height, MainActivity.this);
        frameLayout.addView(mySurfaceView);

        pauseButton = new ImageView(MainActivity.this);
        pauseButton.setImageResource(R.drawable.pause_button);
        FrameLayout.LayoutParams paramsPause = new FrameLayout.LayoutParams(width * 10 / 100, width * 10 / 100);
        paramsPause.leftMargin = 50;
        paramsPause.topMargin = 50;
        pauseButton.setLayoutParams(paramsPause);
        frameLayout.addView(pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySurfaceView.engine.updatePause();
                updatePauseMenuVisibility();
            }
        });
        retryButton = new ImageView(MainActivity.this);
        retryButton.setImageResource(R.drawable.retry_button);
        FrameLayout.LayoutParams paramsRetry = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsRetry.leftMargin = (width - width * 40 / 100) / 2;
        paramsRetry.topMargin = height / 2;
        retryButton.setLayoutParams(paramsRetry);
        frameLayout.addView(retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySurfaceView.engine.restartGame();
                updatePauseMenuVisibility();
            }
        });
        menuButton = new ImageView(MainActivity.this);
        menuButton.setImageResource(R.drawable.menu_button);
        FrameLayout.LayoutParams paramsMenu = new FrameLayout.LayoutParams(width * 40 / 100, width * 40 / 200);
        paramsMenu.leftMargin = (width - width * 40 / 100) / 2;
        paramsMenu.topMargin = height / 2 + width * 40 / 200 + 100;
        menuButton.setLayoutParams(paramsMenu);
        frameLayout.addView(menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBestScore(mySurfaceView.getBestScore());
                Log.d(TAG, "Best score saved on menu button click: " + mySurfaceView.getBestScore());
                openMainMenu();
            }
        });
    }
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    private void updatePauseMenuVisibility() {
        int visibility = mySurfaceView.engine.isPaused() || mySurfaceView.engine.isGameOver() ? View.VISIBLE : View.INVISIBLE;
        retryButton.setVisibility(visibility);
        menuButton.setVisibility(visibility);
        pauseButton.setVisibility(mySurfaceView.engine.isGameOver() ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void updatePauseVisibility() {
        runOnUiThread(this::updatePauseMenuVisibility);
    }

    private void saveBestScore(int bestScore) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(BEST_SCORE_KEY, bestScore);
        editor.apply();
    }

    private int loadBestScore() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int bestScore = sharedPreferences.getInt(BEST_SCORE_KEY, 0);
        return bestScore;
    }

    private void openMainMenu() {
        Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
