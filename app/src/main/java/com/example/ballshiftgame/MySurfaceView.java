package com.example.ballshiftgame;

import static com.example.ballshiftgame.SettingsActivity.SHOW_GUIDE_LINES_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import com.example.jndcjdcjn123.R;

import java.util.ArrayList;
import java.util.List;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceThread surfaceThread;
    private Bitmap backgroundBitmap1, backgroundBitmap2, backgroundBitmap3;
    private Bitmap groundLeftBitmap, groundRightBitmap, playerBitmap, playerLeftBitmap, playerRightBitmap;
    private Bitmap obstacle1, obstacle2, obstacle3, obstacle1_dual, obstacle1_mirrored, obstacle2_dual, obstacle2_mirrored, obstacle3_dual, obstacle3_mirrored;
    private Bitmap obstacle1_1, obstacle2_1, obstacle3_1, obstacle1_dual_1, obstacle1_mirrored_1, obstacle2_dual_1, obstacle2_mirrored_1, obstacle3_dual_1, obstacle3_mirrored_1;
    private Bitmap obstacle1_2, obstacle2_2, obstacle3_2, obstacle1_dual_2, obstacle1_mirrored_2, obstacle2_dual_2, obstacle2_mirrored_2, obstacle3_dual_2, obstacle3_mirrored_2;
    private Bitmap moonBitmap, earthBitmap, marsBitmap, saturnBitmap, sunBitmap;
    private int appWidth, appHeight, backgroundWidth, backgroundHeight;
    private int backgroundY1, backgroundY2, backgroundY3;
    private Typeface montserratBlack, montserratSemiBold;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_PERIOD = 1000 / TARGET_FPS;
    private boolean showFps;
    private boolean showGuideLines;
    Engine engine;
    private GameControlInterface gameControl;

    private class FPSCounter {
        private static final int MAX_SAMPLES = 100;
        private final long[] frameTimes = new long[MAX_SAMPLES];
        private int frameCount = 0;
        private int sampleCount = 0;
        private double fps = 0.0;

        public void logFrame() {
            long currentTime = SystemClock.elapsedRealtime();
            if (frameCount > 0) {
                long elapsed = currentTime - frameTimes[frameCount % MAX_SAMPLES];
                fps = 1000.0 / ((double) elapsed / (double) sampleCount);
            }
            frameTimes[frameCount % MAX_SAMPLES] = currentTime;
            frameCount++;
            sampleCount = Math.min(sampleCount + 1, MAX_SAMPLES);
        }

        public double getFPS() {
            return fps;
        }
    }

    private FPSCounter fpsCounter = new FPSCounter();

    public MySurfaceView(Context context, int width, int height, GameControlInterface gameControl) {
        super(context);
        this.gameControl = gameControl;
        init(context, width, height);
    }

    private void init(Context context, int width, int height) {
        appHeight = height;
        appWidth = width;
        engine = new Engine(context, width, height, gameControl);
        getHolder().addCallback(this);
        backgroundHeight = height + 100;
        backgroundWidth = (int) Math.floor((float) backgroundHeight / 1920 * width);
        backgroundBitmap1 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
        backgroundBitmap2 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
        backgroundBitmap3 = loadScaledBitmap(context, R.drawable.space_bg, backgroundWidth, backgroundHeight);
        backgroundY1 = 0;
        backgroundY2 = -backgroundHeight;
        backgroundY3 = -2 * backgroundHeight;
        groundLeftBitmap = loadScaledBitmap(context, R.drawable.ground, engine.groundWidth, engine.groundHeight);
        groundRightBitmap = loadScaledBitmap(context, R.drawable.ground_mirrored, engine.groundWidth, engine.groundHeight);
        int playerSize = appWidth * 20 / 100;
        playerBitmap = loadScaledBitmap(context, getSkinResource(engine.getCurrentSkin()), playerSize, playerSize);
        playerLeftBitmap = Bitmap.createBitmap(playerBitmap, 0, 0, playerSize / 2, playerSize);
        playerRightBitmap = Bitmap.createBitmap(playerBitmap, playerSize / 2, 0, playerSize / 2, playerSize);
        obstacle1 = loadScaledBitmap(context, R.drawable.obstacle1, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_dual = loadScaledBitmap(context, R.drawable.obstacle1_dual, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_mirrored = loadScaledBitmap(context, R.drawable.obstacle1_mirrored, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_1 = loadScaledBitmap(context, R.drawable.obstacle1_1, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_dual_1 = loadScaledBitmap(context, R.drawable.obstacle1_dual_1, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_mirrored_1 = loadScaledBitmap(context, R.drawable.obstacle1_mirrored_1, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_2 = loadScaledBitmap(context, R.drawable.obstacle1_2, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_dual_2 = loadScaledBitmap(context, R.drawable.obstacle1_dual_2, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_mirrored_2 = loadScaledBitmap(context, R.drawable.obstacle1_mirrored_2, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle2 = loadScaledBitmap(context, R.drawable.obstacle2, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_dual = loadScaledBitmap(context, R.drawable.obstacle2_dual, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_mirrored = loadScaledBitmap(context, R.drawable.obstacle2_mirrored, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_1 = loadScaledBitmap(context, R.drawable.obstacle2_1, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_dual_1 = loadScaledBitmap(context, R.drawable.obstacle2_dual_1, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_mirrored_1 = loadScaledBitmap(context, R.drawable.obstacle2_mirrored_1, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_2 = loadScaledBitmap(context, R.drawable.obstacle2_2, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_dual_2 = loadScaledBitmap(context, R.drawable.obstacle2_dual_2, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_mirrored_2 = loadScaledBitmap(context, R.drawable.obstacle2_mirrored_2, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle3 = loadScaledBitmap(context, R.drawable.obstacle3, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_dual = loadScaledBitmap(context, R.drawable.obstacle3_dual, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_mirrored = loadScaledBitmap(context, R.drawable.obstacle3_mirrored, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_1 = loadScaledBitmap(context, R.drawable.obstacle3_1, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_dual_1 = loadScaledBitmap(context, R.drawable.obstacle3_dual_1, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_mirrored_1 = loadScaledBitmap(context, R.drawable.obstacle3_mirrored_1, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_2 = loadScaledBitmap(context, R.drawable.obstacle3_2, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_dual_2 = loadScaledBitmap(context, R.drawable.obstacle3_dual_2, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_mirrored_2 = loadScaledBitmap(context, R.drawable.obstacle3_mirrored_2, engine.obstacleWidth, engine.obstacleHeight3);
        montserratBlack = ResourcesCompat.getFont(context, R.font.montserrat_black);
        montserratSemiBold = ResourcesCompat.getFont(context, R.font.montserrat_semibold);
        surfaceThread = new SurfaceThread(getHolder(), this);
        showFps = loadShowFpsPreference(context);
        moonBitmap = loadScaledBitmap(context, R.drawable.moon, appWidth * 15 / 100, appWidth * 15 / 100);
        earthBitmap = loadScaledBitmap(context, R.drawable.earth, appWidth * 20 / 100, appWidth * 20 / 100);
        marsBitmap = loadScaledBitmap(context, R.drawable.mars, appWidth * 18 / 100, appWidth * 18 / 100);
        saturnBitmap = loadScaledBitmap(context, R.drawable.saturn, appWidth * 25 / 100, appWidth * 25 / 100);
        sunBitmap = loadScaledBitmap(context, R.drawable.sun, appWidth * 30 / 100, appWidth * 30 / 100);

        showGuideLines = loadShowGuideLinesPreference(context);
    }

    private boolean loadShowGuideLinesPreference(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return preferences.getBoolean(SHOW_GUIDE_LINES_KEY, false);
    }

    private boolean loadShowFpsPreference(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return preferences.getBoolean("show_fps", false);
    }

    private Bitmap loadScaledBitmap(Context context, int resId, int width, int height) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resId), width, height, true);
    }

    private int getSkinResource(int skinIndex) {
        switch (skinIndex) {
            case 0: return R.drawable.ball;
            case 1: return R.drawable.skin2;
            case 2: return R.drawable.skin1;
            case 3: return R.drawable.skin3;
            case 4: return R.drawable.skin4;
            case 5: return R.drawable.skin6;
            default: return R.drawable.ball;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            if (x < (float) appWidth / 3) {
                engine.setDirection(Engine.MoveDirection.LEFT);
            } else if (x > (float) (2 * appWidth) / 3) {
                engine.setDirection(Engine.MoveDirection.RIGHT);
            } else {
                engine.setDirection(Engine.MoveDirection.SPLIT);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            engine.setDirection(Engine.MoveDirection.IDLE);
        }
        return true;
    }

    public void drawCanvas(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        super.draw(canvas);
        drawBackground(canvas);
        drawPlanets(canvas);
        drawGround(canvas);
        drawPlayer(canvas);
        drawObstacles(canvas);
        drawScore(canvas);
        if (engine.isPaused() || engine.isGameOver()) {
            drawPauseOverlay(canvas);
        }
        if (showGuideLines) {
            drawGuideLines(canvas);
        }
        fpsCounter.logFrame();
    }

    private void drawGuideLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAlpha(30);
        paint.setStrokeWidth(5);
        float thirdWidth = appWidth / 3.0f;
        canvas.drawLine(thirdWidth, 0, thirdWidth, appHeight, paint);
        canvas.drawLine(2 * thirdWidth, 0, 2 * thirdWidth, appHeight + 100, paint);
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

    private void drawPlanets(Canvas canvas) {
        for (Planet planet : engine.planets) {
            if (planet.y + planet.size > 0 && planet.y < appHeight) {
                Bitmap current = moonBitmap;
                if (planet.type == 1) current = earthBitmap;
                else if (planet.type == 2) current = marsBitmap;
                else if (planet.type == 3) current = saturnBitmap;
                else if (planet.type == 4) current = sunBitmap;
                canvas.drawBitmap(current, (appWidth - planet.size) / 2, planet.y, null);
            }
        }
    }

    private void drawGround(Canvas canvas) {
        synchronized (engine.getGroundLeft()) {
            for (Wall wall : engine.getGroundLeft()) {
                if (wall.y + engine.groundHeight > 0 && wall.y < appHeight)
                    canvas.drawBitmap(groundLeftBitmap, wall.x, wall.y, null);
            }
        }
        synchronized (engine.getGroundRight()) {
            for (Wall wall : engine.getGroundRight()) {
                if (wall.y + engine.groundHeight > 0 && wall.y < appHeight)
                    canvas.drawBitmap(groundRightBitmap, wall.x, wall.y, null);
            }
        }
    }

    private void drawPlayer(Canvas canvas) {
        Player player = engine.getPlayer();
        canvas.drawBitmap(playerLeftBitmap, player.x1, player.y1, null);
        canvas.drawBitmap(playerRightBitmap, player.x2, player.y2, null);
    }

    private void drawObstacles(Canvas canvas) {
        for (Obstacle obstacle : engine.getObstacles()) {
            if (obstacle.y + obstacle.height > 0 && obstacle.y < appHeight)
                canvas.drawBitmap(getObstacleBitmap(obstacle.x, obstacle.height, obstacle.type), obstacle.x, obstacle.y, null);
        }
    }

    private Bitmap getObstacleBitmap(int x, int height, int type) {
        if (height == engine.obstacleHeight1) {
            if (x == engine.groundWidth)
                if (type == 0) return obstacle1;
                else if (type == 1) return obstacle1_1;
                else return obstacle1_2;
            else if (x == engine.groundWidth + engine.getPlayer().playerSize * 3 / 4)
                if (type == 0) return obstacle1_dual;
                else if (type == 1) return obstacle1_dual_1;
                else return obstacle1_dual_2;
            if (type == 0) return obstacle1_mirrored;
            else if (type == 1) return obstacle1_mirrored_1;
            else return obstacle1_mirrored_2;
        } else if (height == engine.obstacleHeight2) {
            if (x == engine.groundWidth)
                if (type == 0) return obstacle2;
                else if (type == 1) return obstacle2_1;
                else return obstacle2_2;
            else if (x == engine.groundWidth + engine.getPlayer().playerSize * 3 / 4)
                if (type == 0) return obstacle2_dual;
                else if (type == 1) return obstacle2_dual_1;
                else return obstacle2_dual_2;
            if (type == 0) return obstacle2_mirrored;
            else if (type == 1) return obstacle2_mirrored_1;
            else return obstacle2_mirrored_2;
        } else {
            if (x == engine.groundWidth)
                if (type == 0) return obstacle3;
                else if (type == 1) return obstacle3_1;
                else return obstacle3_2;
            else if (x == engine.groundWidth + engine.getPlayer().playerSize * 3 / 4)
                if (type == 0) return obstacle3_dual;
                else if (type == 1) return obstacle3_dual_1;
                else return obstacle3_dual_2;
            if (type == 0) return obstacle3_mirrored;
            else if (type == 1) return obstacle3_mirrored_1;
            else return obstacle3_mirrored_2;
        }
    }

    private Bitmap getRandomBitmap(Bitmap a, Bitmap b, Bitmap c) {
        int chose = Engine.getRandomNumber(1, 3);
        if (chose == 1) return a;
        else if (chose == 2) return b;
        else return c;
    }

    private void drawScore(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(montserratSemiBold);
        String scoreText = String.valueOf(engine.getCountPassedObstacles());
        float textWidth = paint.measureText(scoreText);
        canvas.drawText(scoreText, (appWidth - textWidth) / 2, 200, paint);

        if (showFps) {
            paint.setTextSize(60);
            canvas.drawText(String.format("FPS: %.2f", fpsCounter.getFPS()), 50, 500, paint);
        }
    }

    private void drawPauseOverlay(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0, 0, appWidth, appHeight + 1000, paint);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(100);
        textPaint.setTypeface(montserratSemiBold);
        float textWidth;
        if (engine.isGameOver()) {
            textWidth = textPaint.measureText("Вы разбились!");
            canvas.drawText("Вы разбились!", (appWidth - textWidth) / 2, appHeight / 2 - 400, textPaint);
            textPaint.setTextSize(60);
            String pointsCalculation = engine.getCountPassedObstacles() + " x " + engine.getUpgradeScoreMultiplier() + " = " + (int) Math.floor(engine.getCountPassedObstacles() * engine.getUpgradeScoreMultiplier()) + "$";
            textWidth = textPaint.measureText(pointsCalculation);
            canvas.drawText(pointsCalculation, (appWidth - textWidth) / 2, appHeight / 2 - 300, textPaint);
            textWidth = textPaint.measureText("Рекорд: " + engine.getBestScore());
            canvas.drawText("Рекорд: " + engine.getBestScore(), (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
        } else {
            textWidth = textPaint.measureText("Пауза");
            canvas.drawText("Пауза", (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
        }
    }

    public void pause() {
        if (surfaceThread != null) {
            surfaceThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    surfaceThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            surfaceThread = null;
        }
    }

    public void resume() {
        if (surfaceThread == null || !surfaceThread.isRunning()) {
            surfaceThread = new SurfaceThread(getHolder(), this);
            surfaceThread.setRunning(true);
            surfaceThread.start();
        }
    }

    public void setBestScore(int bestScore) {
        engine.setBestScore(bestScore);
    }

    public int getBestScore() {
        return engine.getBestScore();
    }

    private static class SurfaceThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private final MySurfaceView mySurfaceView;
        private boolean running;

        public SurfaceThread(SurfaceHolder surfaceHolder, MySurfaceView mySurfaceView) {
            this.surfaceHolder = surfaceHolder;
            this.mySurfaceView = mySurfaceView;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                long startTime = System.currentTimeMillis();
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            mySurfaceView.engine.update();
                            mySurfaceView.updateBackgroundPosition();
                            mySurfaceView.drawCanvas(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                long sleepTime = FRAME_PERIOD - timeTaken;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void updateBackgroundPosition() {
        if (!engine.isPaused() && !engine.isGameOver()) {
            backgroundY1 += engine.speed / 4;
            backgroundY2 += engine.speed / 4;
            backgroundY3 += engine.speed / 4;
        }

        if (backgroundY1 >= appHeight) {
            backgroundY1 = backgroundY3 - backgroundHeight;
        }
        if (backgroundY2 >= appHeight) {
            backgroundY2 = backgroundY1 - backgroundHeight;
        }
        if (backgroundY3 >= appHeight) {
            backgroundY3 = backgroundY2 - backgroundHeight;
        }
    }
}
