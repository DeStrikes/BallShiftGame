package com.example.ballshiftgame;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.jndcjdcjn123.R;

public class MainMenuActivity extends AppCompatActivity {

    ImageView playButton, quitButton, upgradeButton, menuLogo, faqButton, closeFaqButton, skinButton, settingsButton, ground;
    TextView bestScoreText, balanceText;
    private static final String PREFS_NAME = "MyPreferences";
    private static final String BEST_SCORE_KEY = "best_score";
    private static final String BALANCE_KEY = "balance";
    private Typeface montserratSemiBold;
    private BackgroundSurfaceView backgroundSurfaceView;
    private ImageView faqImage;
    private boolean isFaqVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        montserratSemiBold = ResourcesCompat.getFont(this, R.font.montserrat_semibold);
        initLayout();
        updateBestScore();
        updateBalance();
    }

    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        backgroundSurfaceView = new BackgroundSurfaceView(this, null, width, height);
        frameLayout.addView(backgroundSurfaceView);

        ground = new ImageView(this);
        ground.setImageResource(R.drawable.menu_bg);
        FrameLayout.LayoutParams paramsGround = new FrameLayout.LayoutParams(height, height);
        paramsGround.leftMargin = -(height - width) / 2;
        ground.setLayoutParams(paramsGround);
        frameLayout.addView(ground);

        balanceText = new TextView(this);
        balanceText.setTextSize(width * 0.015f);
        balanceText.setTypeface(montserratSemiBold);
        balanceText.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams paramsBalance = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsBalance.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        paramsBalance.topMargin = (int) (width * 0.05);
        balanceText.setLayoutParams(paramsBalance);
        frameLayout.addView(balanceText);

        bestScoreText = new TextView(this);
        bestScoreText.setTextSize(20);
        bestScoreText.setTypeface(montserratSemiBold);
        bestScoreText.setTextColor(Color.WHITE);
        bestScoreText.setText("Рекорд: 0");
        FrameLayout.LayoutParams paramsScore = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsScore.gravity = Gravity.CENTER;
        paramsScore.topMargin = width * 8 / 100;
        bestScoreText.setLayoutParams(paramsScore);
        frameLayout.addView(bestScoreText);

        playButton = new ImageView(this);
        playButton.setImageResource(R.drawable.play_button_sq);
        FrameLayout.LayoutParams paramsPlay = new FrameLayout.LayoutParams(width * 35 / 100, width * 35 / 100);
        paramsPlay.gravity = Gravity.CENTER_HORIZONTAL;
        paramsPlay.topMargin = height / 2 - width * 35 / 100;
        playButton.setLayoutParams(paramsPlay);
        frameLayout.addView(playButton);
        playButton.setOnClickListener(v -> startGame());

        faqButton = new ImageView(this);
        faqButton.setImageResource(R.drawable.question_button_sq);
        FrameLayout.LayoutParams paramsFaq = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsFaq.gravity = Gravity.BOTTOM;
        paramsFaq.leftMargin = width * 4 / 100;
        paramsFaq.bottomMargin = height * 4 / 100;
        faqButton.setLayoutParams(paramsFaq);
        frameLayout.addView(faqButton);
        faqButton.setOnClickListener(v -> toggleFaq());

        faqImage = new ImageView(this);
        faqImage.setImageResource(R.drawable.main_menu_guide);
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
        closeFaqButton.setOnClickListener(v -> toggleFaq());

        upgradeButton = new ImageView(this);
        upgradeButton.setImageResource(R.drawable.upgrade_button_sq);
        FrameLayout.LayoutParams paramsUpgrade = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsUpgrade.gravity = Gravity.BOTTOM;
        paramsUpgrade.leftMargin = width * 15 / 100 + width * 4 * 2 / 100;
        paramsUpgrade.bottomMargin = height * 4 / 100;
        upgradeButton.setLayoutParams(paramsUpgrade);
        frameLayout.addView(upgradeButton);
        upgradeButton.setOnClickListener(v -> openUpgradeMenu());

        skinButton = new ImageView(this);
        skinButton.setImageResource(R.drawable.skin_button_sq);
        FrameLayout.LayoutParams paramsSkin = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsSkin.gravity = Gravity.BOTTOM;
        paramsSkin.leftMargin = width * 15 * 2 / 100 + width * 4 * 3 / 100;
        paramsSkin.bottomMargin = height * 4 / 100;
        skinButton.setLayoutParams(paramsSkin);
        frameLayout.addView(skinButton);
        skinButton.setOnClickListener(v -> openSkinMenu());

        settingsButton = new ImageView(this);
        settingsButton.setImageResource(R.drawable.settings_button_sq);
        FrameLayout.LayoutParams paramsSettings = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsSettings.gravity = Gravity.BOTTOM;
        paramsSettings.leftMargin = width * 15 * 3 / 100 + width * 4 * 4 / 100;
        paramsSettings.bottomMargin = height * 4 / 100;
        settingsButton.setLayoutParams(paramsSettings);
        frameLayout.addView(settingsButton);
        settingsButton.setOnClickListener(v -> openSettingsMenu());

        quitButton = new ImageView(this);
        quitButton.setImageResource(R.drawable.quit_button_sq);
        FrameLayout.LayoutParams paramsQuit = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsQuit.gravity = Gravity.BOTTOM;
        paramsQuit.leftMargin = width * 15 * 4 / 100 + width * 4 * 5 / 100;
        paramsQuit.bottomMargin = height * 4 / 100;
        quitButton.setLayoutParams(paramsQuit);
        frameLayout.addView(quitButton);
        quitButton.setOnClickListener(v -> quitGame());

        menuLogo = new ImageView(this);
        menuLogo.setImageResource(R.drawable.menu_logo);
        FrameLayout.LayoutParams paramsLogo = new FrameLayout.LayoutParams(width * 80 / 100, width * 21 / 100);
        paramsLogo.gravity = Gravity.CENTER_HORIZONTAL;
        paramsLogo.topMargin = 300;
        menuLogo.setLayoutParams(paramsLogo);
        frameLayout.addView(menuLogo);
    }

    private void toggleFaq() {
        isFaqVisible = !isFaqVisible;
        if (isFaqVisible) {
            faqButton.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.INVISIBLE);
            quitButton.setVisibility(View.INVISIBLE);
            upgradeButton.setVisibility(View.INVISIBLE);
            skinButton.setVisibility(View.INVISIBLE);
            settingsButton.setVisibility(View.INVISIBLE);
            faqImage.setVisibility(View.VISIBLE);
            closeFaqButton.setVisibility(View.VISIBLE);
            menuLogo.setVisibility(View.INVISIBLE);
        } else {
            faqImage.setVisibility(View.INVISIBLE);
            closeFaqButton.setVisibility(View.INVISIBLE);
            faqButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
            quitButton.setVisibility(View.VISIBLE);
            upgradeButton.setVisibility(View.VISIBLE);
            skinButton.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            menuLogo.setVisibility(View.VISIBLE);
        }
    }

    private void updateBestScore() {
        int bestScore = loadBestScore();
        bestScoreText.setText("Рекорд: " + bestScore);
    }

    private void updateBalance() {
        int balance = loadBalance();
        balanceText.setText("Баланс: " + balance + "$");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalance();
        if (backgroundSurfaceView != null) {
            backgroundSurfaceView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundSurfaceView != null) {
            backgroundSurfaceView.pause();
        }
    }

    private int loadBestScore() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(BEST_SCORE_KEY, 0);
    }

    private int loadBalance() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(BALANCE_KEY, 0);
    }

    private void startGame() {
        Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openUpgradeMenu() {
        Intent intent = new Intent(MainMenuActivity.this, UpgradeActivity.class);
        startActivity(intent);
    }

    private void openSkinMenu() {
        Intent intent = new Intent(MainMenuActivity.this, SkinMenuActivity.class);
        startActivity(intent);
    }

    private void openSettingsMenu() {
        Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void quitGame() {
        finish();
        System.exit(0);
    }

    private static class BackgroundSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private Bitmap backgroundBitmap1, backgroundBitmap2, backgroundBitmap3;
        private int appWidth, appHeight, backgroundWidth, backgroundHeight;
        private int backgroundY1, backgroundY2, backgroundY3;
        private static final int SPEED = 1;
        private ObjectAnimator animator;

        public BackgroundSurfaceView(Context context, AttributeSet attrs, int width, int height) {
            super(context, attrs);
            init(context, width, height);
        }

        private void init(Context context, int width, int height) {
            appHeight = height;
            appWidth = width;
            getHolder().addCallback(this);
            if ((int) Math.floor((float) backgroundHeight * 1080 / 1920) < width) {
                backgroundWidth = width;
                backgroundHeight = (int) Math.floor((float) backgroundWidth * 1920 / 1080);
            } else {
                backgroundHeight = height + 100;
                backgroundWidth = (int) Math.floor((float) backgroundHeight * 1080 / 1920);
            }
            backgroundBitmap1 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
            backgroundBitmap2 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
            backgroundBitmap3 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
            backgroundY1 = 0;
            backgroundY2 = -backgroundHeight;
            backgroundY3 = -2 * backgroundHeight;

            setupAnimator();
        }

        private Bitmap loadScaledBitmap(Context context, int resId, int width, int height) {
            return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resId), width, height, true);
        }

        private void setupAnimator() {
            animator = ObjectAnimator.ofInt(this, "backgroundOffset", 0, backgroundHeight);
            animator.setDuration(100000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.RESTART);
        }

        public void setBackgroundOffset(int offset) {
            backgroundY1 = offset;
            backgroundY2 = offset - backgroundHeight;
            backgroundY3 = offset - 2 * backgroundHeight;

            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                drawCanvas(canvas);
                getHolder().unlockCanvasAndPost(canvas);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            animator.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            animator.cancel();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        public void drawCanvas(Canvas canvas) {
            if (canvas == null) {
                return;
            }
            super.draw(canvas);
            drawBackground(canvas);
        }

        private void drawBackground(Canvas canvas) {
            if (backgroundY1 + backgroundHeight > 0 && backgroundY1 < appHeight) {
                canvas.drawBitmap(backgroundBitmap1, -(backgroundWidth - appWidth) / 2, backgroundY1, null);
            }
            if (backgroundY2 + backgroundHeight > 0 && backgroundY2 < appHeight) {
                canvas.drawBitmap(backgroundBitmap2, -(backgroundWidth - appWidth) / 2, backgroundY2, null);
            }
            if (backgroundY3 + backgroundHeight > 0 && backgroundY3 < appHeight) {
                canvas.drawBitmap(backgroundBitmap3, -(backgroundWidth - appWidth) / 2, backgroundY3, null);
            }
        }

        public void pause() {
            animator.cancel();
        }

        public void resume() {
            animator.start();
        }
    }
}
