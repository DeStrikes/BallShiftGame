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
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.jndcjdcjn123.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPreferences";
    private static final String SHOW_FPS_KEY = "show_fps";
    private static final String HACK_CODE_HASH = "aff3abe45c1b4e14a1784a639b361bff";
    public static final String SHOW_GUIDE_LINES_KEY = "show_guide_lines";

    private Engine engine;
    private TextView pointsText;
    private Typeface montserratSemiBold;
    private ImageView ground;
    private BackgroundSurfaceView backgroundSurfaceView;
    private int backgroundOffset = 0;
    private static long backgroundAnimatorCurrentPlayTime = 0;
    private ImageView faqImage, closeFaqButton, faqButton, exitButton;
    private boolean isFaqVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = new Engine(this, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels, null);
        montserratSemiBold = ResourcesCompat.getFont(this, R.font.montserrat_semibold);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        backgroundOffset = preferences.getInt("background_offset_settings", 0);
        backgroundAnimatorCurrentPlayTime = preferences.getLong("background_animator_play_time_settings", 0);
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

        LinearLayout settingsLayout = new LinearLayout(this);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams paramsSettingsLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        paramsSettingsLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        paramsSettingsLayout.topMargin = (int) (width * 0.2);
        settingsLayout.setLayoutParams(paramsSettingsLayout);
        frameLayout.addView(settingsLayout);

        //addShowFpsOption(settingsLayout, "Отображать FPS");

        addShowGuideLinesOption(settingsLayout, "Ориентиры управления");

        addHackCodeOption(settingsLayout);

        faqImage = new ImageView(this);
        faqImage.setImageResource(R.drawable.settings_guide);
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

    private void addShowGuideLinesOption(LinearLayout layout, String optionName) {
        int width = getResources().getDisplayMetrics().widthPixels;

        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.TOP);
        optionLayout.setPadding((int) (width * 0.02), 0, (int) (width * 0.02), (int) (width * 0.08));

        CheckBox showGuideLinesCheckBox = new CheckBox(this);
        showGuideLinesCheckBox.setText(optionName);
        showGuideLinesCheckBox.setTextSize(width * 0.02f);
        showGuideLinesCheckBox.setTypeface(montserratSemiBold);
        showGuideLinesCheckBox.setTextColor(Color.WHITE);
        showGuideLinesCheckBox.setChecked(loadShowGuideLinesPreference());
        showGuideLinesCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveShowGuideLinesPreference(isChecked));
        optionLayout.addView(showGuideLinesCheckBox);

        layout.addView(optionLayout);
    }

    private boolean loadShowGuideLinesPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(SHOW_GUIDE_LINES_KEY, false);
    }

    private void saveShowGuideLinesPreference(boolean showGuideLines) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHOW_GUIDE_LINES_KEY, showGuideLines);
        editor.apply();
    }

    private void addShowFpsOption(LinearLayout layout, String optionName) {
        int width = getResources().getDisplayMetrics().widthPixels;

        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.TOP);
        optionLayout.setPadding((int) (width * 0.02), 0, (int) (width * 0.02), (int) (width * 0.08));

        CheckBox showFpsCheckBox = new CheckBox(this);
        showFpsCheckBox.setText(optionName);
        showFpsCheckBox.setTextSize(width * 0.02f);
        showFpsCheckBox.setTypeface(montserratSemiBold);
        showFpsCheckBox.setTextColor(Color.WHITE);
        showFpsCheckBox.setChecked(loadShowFpsPreference());
        showFpsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveShowFpsPreference(isChecked));
        optionLayout.addView(showFpsCheckBox);

        layout.addView(optionLayout);
    }

    private void addHackCodeOption(LinearLayout layout) {
        int width = getResources().getDisplayMetrics().widthPixels;

        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);
        optionLayout.setGravity(Gravity.TOP);
        optionLayout.setPadding((int) (width * 0.02), 0, (int) (width * 0.02), (int) (width * 0.08));

        EditText hackCodeInput = new EditText(this);
        hackCodeInput.setHint("Введите код");
        hackCodeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hackCodeInput.setTextColor(Color.WHITE);
        hackCodeInput.setTypeface(montserratSemiBold);
        hackCodeInput.setHintTextColor(Color.GRAY);
        hackCodeInput.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
        optionLayout.addView(hackCodeInput);

        Button applyCodeButton = new Button(this);
        applyCodeButton.setText("Применить");
        applyCodeButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
        applyCodeButton.setOnClickListener(v -> {
            String inputCode = hackCodeInput.getText().toString();
            if (validateHackCode(inputCode)) {
                engine.addToBalance(10000);
                engine.savePreferences(this);
                updatePointsText();
                Toast.makeText(this, "Код принят, $10000 добавлены на баланс", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Неверный код", Toast.LENGTH_SHORT).show();
            }
        });
        optionLayout.addView(applyCodeButton);

        layout.addView(optionLayout);
    }

    private boolean loadShowFpsPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(SHOW_FPS_KEY, false);
    }

    private void saveShowFpsPreference(boolean showFps) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHOW_FPS_KEY, showFps);
        editor.apply();
    }

    private boolean validateHackCode(String inputCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(inputCode.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(HACK_CODE_HASH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePointsText() {
        pointsText.setText("Баланс: " + engine.getBalance() + "$");
    }

    private void saveBackgroundState() {
        backgroundOffset = backgroundSurfaceView.getCurrentOffset();
        backgroundAnimatorCurrentPlayTime = backgroundSurfaceView.getCurrentAnimatorPlayTime();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("background_offset_settings", backgroundOffset);
        editor.putLong("background_animator_play_time_settings", backgroundAnimatorCurrentPlayTime);
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
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

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
