package com.example.ballshiftgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jndcjdcjn123.R;

public class MainActivity extends AppCompatActivity implements GameControlInterface {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "MyPreferences";
    private static final String BEST_SCORE_KEY = "best_score";

    MySurfaceView mySurfaceView;
    FrameLayout frameLayout;
    ImageView pauseButton, retryButton, menuButton, faqButton, closeFaqButton;
    ImageView faqImage;
    private boolean isFaqVisible = false;



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
            mySurfaceView.engine.savePreferences(this);
            Log.d(TAG, "Best score and preferences saved on pause: " + mySurfaceView.getBestScore());
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

        faqButton = new ImageView(this);
        faqButton.setImageResource(R.drawable.question_button_sq);
        FrameLayout.LayoutParams paramsFaq = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsFaq.leftMargin = 23 * width / 100;
        paramsFaq.topMargin = height / 2;
        faqButton.setLayoutParams(paramsFaq);
        frameLayout.addView(faqButton);

        faqImage = new ImageView(this);
        faqImage.setImageResource(R.drawable.game_guide);
        int faqImageWidth = width * 90 / 100;
        int faqImageHeight = faqImageWidth * 800 / 600;
        FrameLayout.LayoutParams paramsFaqImage = new FrameLayout.LayoutParams(faqImageWidth, faqImageHeight);
        paramsFaqImage.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        faqImage.setLayoutParams(paramsFaqImage);
        faqImage.setVisibility(View.INVISIBLE);
        frameLayout.addView(faqImage);

        closeFaqButton = new ImageView(this);
        closeFaqButton.setImageResource(R.drawable.cross_button_sq);
        FrameLayout.LayoutParams paramsCloseFaq = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsCloseFaq.gravity = Gravity.CENTER;
        paramsCloseFaq.topMargin = (int) (height * 5 / 100) + (faqImageHeight / 2);
        closeFaqButton.setLayoutParams(paramsCloseFaq);
        closeFaqButton.setVisibility(View.INVISIBLE);
        frameLayout.addView(closeFaqButton);

        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFaq();
            }
        });

        closeFaqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFaq();
            }
        });

        frameLayout.setBackgroundColor(android.graphics.Color.BLACK);

        retryButton = new ImageView(MainActivity.this);
        retryButton.setImageResource(R.drawable.retry_button_sq);
        FrameLayout.LayoutParams paramsRetry = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsRetry.leftMargin = width * 15 / 100 + 23 * width / 100 + 4 * width / 100;
        paramsRetry.topMargin = height / 2;
        retryButton.setLayoutParams(paramsRetry);
        frameLayout.addView(retryButton);
        retryButton.setOnClickListener(v -> {
            mySurfaceView.engine.restartGame();
            updatePauseMenuVisibility();
        });
        menuButton = new ImageView(MainActivity.this);
        menuButton.setImageResource(R.drawable.menu_button_sq);
        FrameLayout.LayoutParams paramsMenu = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsMenu.leftMargin = width * 15 * 2 / 100 + 23 * width / 100 + 4 * width * 2 / 100;
        paramsMenu.topMargin = height / 2;
        menuButton.setLayoutParams(paramsMenu);
        frameLayout.addView(menuButton);
        menuButton.setOnClickListener(v -> {
            saveBestScore(mySurfaceView.getBestScore());
            mySurfaceView.engine.savePreferences(MainActivity.this);
            Log.d(TAG, "Best score and preferences saved on menu button click: " + mySurfaceView.getBestScore());
            openMainMenu();
        });
    }

    private void toggleFaq() {
        isFaqVisible = !isFaqVisible;
        if (isFaqVisible) {
            mySurfaceView.pause();
            pauseButton.setVisibility(View.INVISIBLE);
            retryButton.setVisibility(View.INVISIBLE);
            menuButton.setVisibility(View.INVISIBLE);
            faqButton.setVisibility(View.INVISIBLE);
            faqImage.setVisibility(View.VISIBLE);
            closeFaqButton.setVisibility(View.VISIBLE);
        } else {
            faqImage.setVisibility(View.INVISIBLE);
            closeFaqButton.setVisibility(View.INVISIBLE);
            mySurfaceView.resume();
            updatePauseMenuVisibility();
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void updatePauseMenuVisibility() {
        int visibility = mySurfaceView.engine.isPaused() || mySurfaceView.engine.isGameOver() ? View.VISIBLE : View.INVISIBLE;
        retryButton.setVisibility(visibility);
        menuButton.setVisibility(visibility);
        faqButton.setVisibility(visibility);
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
