package com.example.ballshiftgame;

import android.animation.ObjectAnimator;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.jndcjdcjn123.R;

public class UpgradeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPreferences";
    private static final String POINTS_KEY = "points";
    private static final String UPGRADE_DISTANCE_KEY = "upgrade_distance";
    private static final String UPGRADE_SPEED_KEY = "upgrade_speed";
    private static final String UPGRADE_SPEED_RETURN_KEY = "upgrade_speed_return";
    private static final String UPGRADE_SCORE_MULTIPLIER_LEVEL_KEY = "upgrade_score_multiplier_level";
    private static final String UPGRADE_SCORE_MULTIPLIER_KEY = "upgrade_score_multiplier";
    private static final String BACKGROUND_OFFSET_KEY = "background_offset_upgrade";
    private static final String BACKGROUND_ANIMATOR_PLAY_TIME_KEY = "background_animator_play_time_upgrade";
    private static final String UPGRADE_START_SPEED_KEY = "upgrade_start_speed";

    private static final int MAX_UPGRADE_DISTANCE = 10;
    private static final int MAX_UPGRADE_SPEED = 10;
    private static final int MAX_UPGRADE_SPEED_RETURN = 10;
    private static final int MAX_UPGRADE_SCORE_MULTIPLIER = 10;
    private static final int MAX_UPGRADE_START_SPEED = 5;

    private Engine engine;
    private TextView pointsText;
    private Typeface montserratSemiBold;
    private ImageView ground, exitButton, faqImage, closeFaqButton, faqButton;
    private BackgroundSurfaceView backgroundSurfaceView;
    private int backgroundOffset = 0;
    private static long backgroundAnimatorCurrentPlayTime = 0;
    private boolean isFaqVisible = false;
    private LinearLayout upgradeScrollLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new Engine(this, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, null);
        montserratSemiBold = ResourcesCompat.getFont(this, R.font.montserrat_semibold);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        backgroundOffset = preferences.getInt(BACKGROUND_OFFSET_KEY, 0);
        backgroundAnimatorCurrentPlayTime = preferences.getLong(BACKGROUND_ANIMATOR_PLAY_TIME_KEY, 0);
        initLayout();
        updatePointsText();
    }

    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);
        frameLayout.setBackgroundColor(Color.rgb(30, 30, 30));
        int width = getResources().getDisplayMetrics().widthPixels, height = getResources().getDisplayMetrics().heightPixels;

        backgroundSurfaceView = new BackgroundSurfaceView(this, null, width, height);
        backgroundSurfaceView.setBackgroundOffset(backgroundOffset);
        frameLayout.addView(backgroundSurfaceView);

        ground = new ImageView(this);
        ground.setImageResource(R.drawable.menu_bg);
        FrameLayout.LayoutParams paramsGround = new FrameLayout.LayoutParams(height, height);
        paramsGround.leftMargin = -(height - width) / 2;
        ground.setLayoutParams(paramsGround);
        frameLayout.addView(ground);

        pointsText = new TextView(this);
        pointsText.setTextSize(width * 0.015f);
        pointsText.setTypeface(montserratSemiBold);
        pointsText.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams paramsPoints = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsPoints.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        paramsPoints.topMargin = (int) (width * 0.05);
        pointsText.setLayoutParams(paramsPoints);
        frameLayout.addView(pointsText);

        LinearLayout upgradeLayout = new LinearLayout(this);
        upgradeLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams paramsUpgradeLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsUpgradeLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        paramsUpgradeLayout.topMargin = (int) (width * 0.2);
        upgradeLayout.setLayoutParams(paramsUpgradeLayout);
        frameLayout.addView(upgradeLayout);

        ScrollView scrollView = new ScrollView(this);
        FrameLayout.LayoutParams paramsScrollView = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsScrollView.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        paramsScrollView.topMargin = (int) (width * 0.2);
        paramsScrollView.bottomMargin = (int) (width * 0.3);
        scrollView.setLayoutParams(paramsScrollView);
        frameLayout.addView(scrollView);

        upgradeScrollLayout = new LinearLayout(this);
        upgradeScrollLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(upgradeScrollLayout);

        addUpgradeOption(upgradeScrollLayout, "Скорость", engine.getUpgradeSpeedReturnCost(), v -> upgradeSpeedReturn(), R.drawable.divide_speed_up, engine.getUpgradeSpeedReturn(), MAX_UPGRADE_SPEED_RETURN);
        addUpgradeOption(upgradeScrollLayout, "Множитель очков", engine.getUpgradeScoreMultiplierCost(), v -> upgradeScoreMultiplier(), R.drawable.divide_speed_up, engine.getUpgradeScoreMultiplierLevel(), MAX_UPGRADE_SCORE_MULTIPLIER);
        addUpgradeOption(upgradeScrollLayout, "Частота препятствий", engine.getUpgradeDistanceCost(), v -> upgradeDistance(), R.drawable.multiplier, engine.getUpgradeDistance(), MAX_UPGRADE_DISTANCE);
        addUpgradeOption(upgradeScrollLayout, "Начальная скорость", engine.getUpgradeStartSpeedCost(), v -> upgradeStartSpeed(), R.drawable.speed_up, engine.getUpgradeStartSpeed(), MAX_UPGRADE_START_SPEED);

        faqImage = new ImageView(this);
        faqImage.setImageResource(R.drawable.upgrade_guide);
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

        faqButton = new ImageView(this);
        faqButton.setImageResource(R.drawable.question_button_sq);
        FrameLayout.LayoutParams paramsFaq = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsFaq.gravity = Gravity.BOTTOM;
        paramsFaq.leftMargin = width * 33 / 100;
        paramsFaq.bottomMargin = height * 4 / 100;
        faqButton.setLayoutParams(paramsFaq);
        frameLayout.addView(faqButton);
        faqButton.setOnClickListener(v -> toggleFaq());

        frameLayout.setBackgroundColor(android.graphics.Color.BLACK);

        exitButton = new ImageView(this);
        exitButton.setImageResource(R.drawable.menu_button_sq);
        FrameLayout.LayoutParams paramsExit = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsExit.gravity = Gravity.BOTTOM;
        paramsExit.bottomMargin = height * 4 / 100;
        paramsExit.leftMargin = width * 15 / 100 + width * 33 / 100 + width * 4 / 100;
        frameLayout.addView(exitButton);
        exitButton.setLayoutParams(paramsExit);
        exitButton.setOnClickListener(v -> finish());
    }

    private void toggleFaq() {
        isFaqVisible = !isFaqVisible;
        if (isFaqVisible) {
            faqButton.setVisibility(View.INVISIBLE);
            faqImage.setVisibility(View.VISIBLE);
            closeFaqButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.INVISIBLE);
        } else {
            faqButton.setVisibility(View.VISIBLE);
            faqImage.setVisibility(View.INVISIBLE);
            closeFaqButton.setVisibility(View.INVISIBLE);
            exitButton.setVisibility(View.VISIBLE);
        }
    }

    private void addUpgradeOption(LinearLayout layout, String upgradeName, int cost, View.OnClickListener listener, int image, int currentLevel, int maxLevel) {
        int width = getResources().getDisplayMetrics().widthPixels;

        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.TOP);
        optionLayout.setPadding((int) (width * 0.02), 0, (int) (width * 0.02), (int) (width * 0.08));
        optionLayout.setTag(upgradeName);

        ImageView upgradeImage = new ImageView(this);
        upgradeImage.setImageResource(image);
        LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(0, width * 15 / 100, 0.2f);
        paramsImage.gravity = Gravity.TOP;
        upgradeImage.setLayoutParams(paramsImage);
        optionLayout.addView(upgradeImage);

        TextView upgradeText = new TextView(this);
        upgradeText.setText(upgradeName);
        upgradeText.setTextSize(width * 0.02f);
        upgradeText.setTypeface(montserratSemiBold);
        upgradeText.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        paramsText.gravity = Gravity.TOP;
        upgradeText.setLayoutParams(paramsText);
        optionLayout.addView(upgradeText);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsButtonLayout = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f);
        paramsButtonLayout.gravity = Gravity.TOP;
        buttonLayout.setLayoutParams(paramsButtonLayout);

        ImageView buyButton = new ImageView(this);
        buyButton.setImageResource(R.drawable.buy_button_sq);
        LinearLayout.LayoutParams paramsButton = new LinearLayout.LayoutParams((int) (width * 15 / 100), (int) (width * 15 / 100));
        paramsButton.gravity = Gravity.CENTER;
        buyButton.setLayoutParams(paramsButton);
        buyButton.setTag("buyButton");
        buyButton.setOnClickListener(v -> {
            listener.onClick(v);
            saveBackgroundState();
            updateAllUpgradeOptions();
            updatePointsText();
        });
        buttonLayout.addView(buyButton);

        TextView costText = new TextView(this);
        if (currentLevel >= maxLevel) {
            costText.setText("МАКС");
            buyButton.setOnClickListener(null);
        } else {
            costText.setText(cost + "$");
        }
        costText.setTextSize(width * 0.015f);
        costText.setTypeface(montserratSemiBold);
        costText.setTextColor(Color.WHITE);
        costText.setTag("costText");
        LinearLayout.LayoutParams paramsCostText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsCostText.gravity = Gravity.CENTER;
        costText.setLayoutParams(paramsCostText);
        buttonLayout.addView(costText);

        TextView levelText = new TextView(this);
        levelText.setText(currentLevel + "/" + maxLevel);
        levelText.setTextSize(width * 0.015f);
        levelText.setTypeface(montserratSemiBold);
        levelText.setTextColor(Color.WHITE);
        levelText.setTag("levelText");
        LinearLayout.LayoutParams paramsLevelText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsLevelText.gravity = Gravity.CENTER;
        levelText.setLayoutParams(paramsLevelText);
        buttonLayout.addView(levelText);

        optionLayout.addView(buttonLayout);

        layout.addView(optionLayout);
    }

    private void updatePointsText() {
        pointsText.setText("Баланс: " + engine.getBalance() + "$");
    }

    private void updateAllUpgradeOptions() {
        updateUpgradeOption("Скорость", engine.getUpgradeSpeedReturnCost(), engine.getUpgradeSpeedReturn(), MAX_UPGRADE_SPEED_RETURN);
        updateUpgradeOption("Множитель очков", engine.getUpgradeScoreMultiplierCost(), engine.getUpgradeScoreMultiplierLevel(), MAX_UPGRADE_SCORE_MULTIPLIER);
        updateUpgradeOption("Частота препятствий", engine.getUpgradeDistanceCost(), engine.getUpgradeDistance(), MAX_UPGRADE_DISTANCE);
        updateUpgradeOption("Начальная скорость", engine.getUpgradeStartSpeedCost(), engine.getUpgradeStartSpeed(), MAX_UPGRADE_START_SPEED);
    }

    private void updateUpgradeOption(String upgradeName, int cost, int currentLevel, int maxLevel) {
        LinearLayout upgradeOption = (LinearLayout) upgradeScrollLayout.findViewWithTag(upgradeName);
        if (upgradeOption != null) {
            TextView costText = upgradeOption.findViewWithTag("costText");
            TextView levelText = upgradeOption.findViewWithTag("levelText");
            ImageView buyButton = upgradeOption.findViewWithTag("buyButton");

            if (currentLevel >= maxLevel) {
                costText.setText("МАКС");
                buyButton.setOnClickListener(null);
            } else {
                costText.setText(cost + "$");
                buyButton.setOnClickListener(v -> {
                    switch (upgradeName) {
                        case "Скорость":
                            upgradeSpeedReturn();
                            break;
                        case "Множитель очков":
                            upgradeScoreMultiplier();
                            break;
                        case "Частота препятствий":
                            upgradeDistance();
                            break;
                        case "Начальная скорость":
                            upgradeStartSpeed();
                            break;
                    }
                    saveBackgroundState();
                    updateAllUpgradeOptions();
                    updatePointsText();
                });
            }

            levelText.setText(currentLevel + "/" + maxLevel);
        }
    }

    private void upgradeDistance() {
        if (engine.getBalance() >= engine.getUpgradeDistanceCost()) {
            engine.subtractFromBalance(engine.getUpgradeDistanceCost());
            engine.upgradeDistance();
            engine.setUpgradeDistanceCost((int) (engine.getUpgradeDistanceCost() * 1.2));
            saveUpgrades();
        }
    }

    private void upgradeSpeedReturn() {
        if (engine.getBalance() >= engine.getUpgradeSpeedReturnCost()) {
            engine.subtractFromBalance(engine.getUpgradeSpeedReturnCost());
            engine.upgradeSpeedReturn();
            engine.setUpgradeSpeedReturnCost((int) (engine.getUpgradeSpeedReturnCost() * 1.2));
            saveUpgrades();
        }
    }

    private void upgradeScoreMultiplier() {
        if (engine.getBalance() >= engine.getUpgradeScoreMultiplierCost()) {
            engine.subtractFromBalance(engine.getUpgradeScoreMultiplierCost());
            engine.upgradeScoreMultiplier();
            engine.setUpgradeScoreMultiplierCost((int) (engine.getUpgradeScoreMultiplierCost() * 1.4));
            saveUpgrades();
        }
    }

    private void upgradeStartSpeed() {
        if (engine.getBalance() >= engine.getUpgradeStartSpeedCost()) {
            engine.subtractFromBalance(engine.getUpgradeStartSpeedCost());
            engine.upgradeStartSpeed();
            engine.setUpgradeStartSpeedCost((int) (engine.getUpgradeStartSpeedCost() * 1.3));
            saveUpgrades();
        }
    }

    private void saveUpgrades() {
        engine.savePreferences(this);
    }

    private void saveBackgroundState() {
        backgroundOffset = backgroundSurfaceView.getCurrentOffset();
        backgroundAnimatorCurrentPlayTime = backgroundSurfaceView.getCurrentAnimatorPlayTime();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(BACKGROUND_OFFSET_KEY, backgroundOffset);
        editor.putLong(BACKGROUND_ANIMATOR_PLAY_TIME_KEY, backgroundAnimatorCurrentPlayTime);
        editor.apply();
    }

    private static class BackgroundSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private Bitmap backgroundBitmap1, backgroundBitmap2, backgroundBitmap3;
        private int appWidth, appHeight, backgroundWidth, backgroundHeight;
        private int backgroundY1, backgroundY2, backgroundY3;
        private static final int SPEED = 5;
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

        public int getCurrentOffset() {
            return backgroundY1;
        }

        public long getCurrentAnimatorPlayTime() {
            return animator.getCurrentPlayTime();
        }

        public void resumeAnimatorPlayTime(long playTime) {
            animator.setCurrentPlayTime(playTime);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            animator.start();
            resumeAnimatorPlayTime(backgroundAnimatorCurrentPlayTime);
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
            if (animator.isRunning()) {
                animator.pause();
            }
        }

        public void resume() {
            if (animator.isPaused()) {
                animator.resume();
            } else {
                animator.start();
            }
        }
    }
}
