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

public class SkinMenuActivity extends AppCompatActivity {

    public static final int SKIN_COUNT = 6;

    private Engine engine;
    private TextView pointsText;
    private Typeface montserratSemiBold;
    private ImageView ground, faqButton;
    private BackgroundSurfaceView backgroundSurfaceView;
    private int backgroundOffset = 0;
    private static long backgroundAnimatorCurrentPlayTime = 0;
    private ImageView faqImage, closeFaqButton, exitButton;
    private boolean isFaqVisible = false;
    private LinearLayout skinLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new Engine(this, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, null);
        montserratSemiBold = ResourcesCompat.getFont(this, R.font.montserrat_semibold);
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        backgroundOffset = preferences.getInt("background_offset_skin_menu", 0);
        backgroundAnimatorCurrentPlayTime = preferences.getLong("background_animator_play_time_skin_menu", 0);
        initLayout();
        updatePointsText();
    }

    private void initLayout() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);
        frameLayout.setBackgroundColor(Color.rgb(30, 30, 30));
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

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

        ScrollView scrollView = new ScrollView(this);
        FrameLayout.LayoutParams paramsScrollView = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsScrollView.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        paramsScrollView.topMargin = (int) (width * 0.2);
        paramsScrollView.bottomMargin = (int) (width * 0.3);
        scrollView.setLayoutParams(paramsScrollView);
        frameLayout.addView(scrollView);

        skinLayout = new LinearLayout(this);
        skinLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(skinLayout);
        addSkinOption(skinLayout, "Робот", 0, getSkinCost(0), R.drawable.ball, engine.isSkinPurchased(0), engine.getCurrentSkin() == 0);
        addSkinOption(skinLayout, "Арбуз", 1, getSkinCost(1), R.drawable.skin2, engine.isSkinPurchased(1), engine.getCurrentSkin() == 1);
        addSkinOption(skinLayout, "Покерная фишка", 2, getSkinCost(2), R.drawable.skin1, engine.isSkinPurchased(2), engine.getCurrentSkin() == 2);
        addSkinOption(skinLayout, "Часы", 3, getSkinCost(3), R.drawable.skin3, engine.isSkinPurchased(3), engine.getCurrentSkin() == 3);
        addSkinOption(skinLayout, "Атом", 4, getSkinCost(4), R.drawable.skin4, engine.isSkinPurchased(4), engine.getCurrentSkin() == 4);
        addSkinOption(skinLayout, "Галактика", 5, getSkinCost(5), R.drawable.skin6, engine.isSkinPurchased(5), engine.getCurrentSkin() == 5);

        faqButton = new ImageView(this);
        faqButton.setImageResource(R.drawable.question_button_sq);
        FrameLayout.LayoutParams paramsFaq = new FrameLayout.LayoutParams(width * 15 / 100, width * 15 / 100);
        paramsFaq.gravity = Gravity.BOTTOM;
        paramsFaq.leftMargin = width * 33 / 100;
        paramsFaq.bottomMargin = height * 4 / 100;
        faqButton.setLayoutParams(paramsFaq);
        frameLayout.addView(faqButton);
        faqButton.setOnClickListener(v -> toggleFaq());

        faqImage = new ImageView(this);
        faqImage.setImageResource(R.drawable.skins_guide);
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
            closeFaqButton.setVisibility(View.VISIBLE);
            faqImage.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.INVISIBLE);
        } else {
            faqImage.setVisibility(View.INVISIBLE);
            closeFaqButton.setVisibility(View.INVISIBLE);
            faqButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);
        }
    }

    private void addSkinOption(LinearLayout layout, String skinName, int skinIndex, int cost, int image, boolean purchased, boolean equipped) {
        int width = getResources().getDisplayMetrics().widthPixels;

        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.TOP);
        optionLayout.setPadding((int) (width * 0.02), 0, (int) (width * 0.02), (int) (width * 0.08));
        optionLayout.setTag(skinName);

        ImageView skinImage = new ImageView(this);
        skinImage.setImageResource(image);
        LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(0, width * 15 / 100, 0.2f);
        paramsImage.gravity = Gravity.TOP;
        skinImage.setLayoutParams(paramsImage);
        optionLayout.addView(skinImage);

        TextView skinText = new TextView(this);
        skinText.setText(skinName);
        skinText.setTextSize(width * 0.02f);
        skinText.setTypeface(montserratSemiBold);
        skinText.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        paramsText.gravity = Gravity.TOP;
        skinText.setLayoutParams(paramsText);
        optionLayout.addView(skinText);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams paramsButtonLayout = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f);
        paramsButtonLayout.gravity = Gravity.TOP;
        buttonLayout.setLayoutParams(paramsButtonLayout);

        ImageView actionButton = new ImageView(this);
        actionButton.setTag("actionButton");
        if (purchased) {
            if (equipped) {
                actionButton.setImageResource(R.drawable.check_button_sq);
                actionButton.setOnClickListener(null);
            } else {
                actionButton.setImageResource(R.drawable.skin_button_sq);
                actionButton.setOnClickListener(v -> {
                    engine.setCurrentSkin(skinIndex);
                    engine.savePreferences(this);
                    saveBackgroundState();
                    updateAllSkinOptions();
                    updatePointsText();
                });
            }
        } else {
            actionButton.setImageResource(R.drawable.buy_button_sq);
            actionButton.setOnClickListener(v -> {
                if (engine.getBalance() >= cost) {
                    engine.subtractFromBalance(cost);
                    engine.purchaseSkin(skinIndex);
                    engine.savePreferences(this);
                    updatePointsText();
                    saveBackgroundState();
                    updateSkinOption(skinName, skinIndex, cost, image, true, false);
                }
            });
        }
        LinearLayout.LayoutParams paramsButton = new LinearLayout.LayoutParams((int) (width * 15 / 100), (int) (width * 15 / 100));
        paramsButton.gravity = Gravity.CENTER;
        actionButton.setLayoutParams(paramsButton);
        buttonLayout.addView(actionButton);

        if (!purchased) {
            TextView costText = new TextView(this);
            costText.setText(cost + "$");
            costText.setTextSize(width * 0.015f);
            costText.setTypeface(montserratSemiBold);
            costText.setTextColor(Color.WHITE);
            costText.setTag("costText");
            LinearLayout.LayoutParams paramsCostText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsCostText.gravity = Gravity.CENTER;
            costText.setLayoutParams(paramsCostText);
            buttonLayout.addView(costText);
        }

        optionLayout.addView(buttonLayout);

        layout.addView(optionLayout);
    }

    private void updatePointsText() {
        pointsText.setText("Баланс: " + engine.getBalance() + "$");
    }

    private void updateAllSkinOptions() {
        updateSkinOption("Робот", 0, getSkinCost(0), R.drawable.ball, engine.isSkinPurchased(0), engine.getCurrentSkin() == 0);
        updateSkinOption("Арбуз", 1, getSkinCost(1), R.drawable.skin2, engine.isSkinPurchased(1), engine.getCurrentSkin() == 1);
        updateSkinOption("Покерная фишка", 2, getSkinCost(2), R.drawable.skin1, engine.isSkinPurchased(2), engine.getCurrentSkin() == 2);
        updateSkinOption("Часы", 3, getSkinCost(3), R.drawable.skin3, engine.isSkinPurchased(3), engine.getCurrentSkin() == 3);
        updateSkinOption("Атом", 4, getSkinCost(4), R.drawable.skin4, engine.isSkinPurchased(4), engine.getCurrentSkin() == 4);
        updateSkinOption("Галактика", 5, getSkinCost(5), R.drawable.skin6, engine.isSkinPurchased(5), engine.getCurrentSkin() == 5);
    }

    private void updateSkinOption(String skinName, int skinIndex, int cost, int image, boolean purchased, boolean equipped) {
        LinearLayout skinOption = (LinearLayout) skinLayout.findViewWithTag(skinName);
        if (skinOption != null) {
            ImageView actionButton = skinOption.findViewWithTag("actionButton");
            TextView costText = skinOption.findViewWithTag("costText");

            if (purchased) {
                if (equipped) {
                    actionButton.setImageResource(R.drawable.check_button_sq);
                    actionButton.setOnClickListener(null);
                } else {
                    actionButton.setImageResource(R.drawable.skin_button_sq);
                    actionButton.setOnClickListener(v -> {
                        engine.setCurrentSkin(skinIndex);
                        engine.savePreferences(this);
                        saveBackgroundState();
                        updateAllSkinOptions();
                        updatePointsText();
                    });
                }
                if (costText != null) {
                    costText.setVisibility(View.GONE);
                }
            } else {
                actionButton.setImageResource(R.drawable.buy_button_sq);
                actionButton.setOnClickListener(v -> {
                    if (engine.getBalance() >= cost) {
                        engine.subtractFromBalance(cost);
                        engine.purchaseSkin(skinIndex);
                        engine.savePreferences(this);
                        updatePointsText();
                        saveBackgroundState();
                        updateSkinOption(skinName, skinIndex, cost, image, true, false);
                    }
                });
                if (costText != null) {
                    costText.setText(cost + "$");
                }
            }
        }
    }

    private int getSkinCost(int skinIndex) {
        switch (skinIndex) {
            case 0: return 0;
            case 1: return 50;
            case 2: return 100;
            case 3: return 200;
            case 4: return 500;
            case 5: return 1000;
            default: return 100;
        }
    }

    private void saveBackgroundState() {
        backgroundOffset = backgroundSurfaceView.getCurrentOffset();
        backgroundAnimatorCurrentPlayTime = backgroundSurfaceView.getCurrentAnimatorPlayTime();

        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("background_offset_skin_menu", backgroundOffset);
        editor.putLong("background_animator_play_time_skin_menu", backgroundAnimatorCurrentPlayTime);
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
