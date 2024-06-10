package com.example.jndcjdcjn123;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceThread surfaceThread;
    private Bitmap backgroundBitmap, groundLeftBitmap, groundRightBitmap, playerBitmap, playerLeftBitmap, playerRightBitmap;
    private Bitmap obstacle1, obstacle2, obstacle3, obstacle1_dual, obstacle1_mirrored, obstacle2_dual, obstacle2_mirrored, obstacle3_dual, obstacle3_mirrored;
    private int appWidth, appHeight, backgroundWidth;
    private Typeface montserratBlack, montserratSemiBold;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_PERIOD = 1000 / TARGET_FPS;
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
        backgroundWidth = (int) Math.floor((float) (height + 100) / 1920 * width);
        backgroundBitmap = loadScaledBitmap(context, R.drawable.space_bg, (int) Math.floor((float) (height + 100) / 1920 * width), height + 100);
        groundLeftBitmap = loadScaledBitmap(context, R.drawable.ground, engine.groundWidth, engine.groundHeight);
        groundRightBitmap = loadScaledBitmap(context, R.drawable.ground_mirrored, engine.groundWidth, engine.groundHeight);
        int playerSize = appWidth * 20 / 100;
        playerBitmap = loadScaledBitmap(context, R.drawable.ball, playerSize, playerSize);
        playerLeftBitmap = Bitmap.createBitmap(playerBitmap, 0, 0, playerSize / 2, playerSize);
        playerRightBitmap = Bitmap.createBitmap(playerBitmap, playerSize / 2, 0, playerSize / 2, playerSize);
        obstacle1 = loadScaledBitmap(context, R.drawable.obstacle1, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_dual = loadScaledBitmap(context, R.drawable.obstacle1_dual, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle1_mirrored = loadScaledBitmap(context, R.drawable.obstacle1_mirrored, engine.obstacleWidth, engine.obstacleHeight1);
        obstacle2 = loadScaledBitmap(context, R.drawable.obstacle2, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_dual = loadScaledBitmap(context, R.drawable.obstacle2_dual, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle2_mirrored = loadScaledBitmap(context, R.drawable.obstacle2_mirrored, engine.obstacleWidth, engine.obstacleHeight2);
        obstacle3 = loadScaledBitmap(context, R.drawable.obstacle3, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_dual = loadScaledBitmap(context, R.drawable.obstacle3_dual, engine.obstacleWidth, engine.obstacleHeight3);
        obstacle3_mirrored = loadScaledBitmap(context, R.drawable.obstacle3_mirrored, engine.obstacleWidth, engine.obstacleHeight3);
        montserratBlack = ResourcesCompat.getFont(context, R.font.montserrat_black);
        montserratSemiBold = ResourcesCompat.getFont(context, R.font.montserrat_semibold);
        surfaceThread = new SurfaceThread(getHolder(), this);
    }

    private Bitmap loadScaledBitmap(Context context, int resId, int width, int height) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resId), width, height, true);
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
        canvas.drawBitmap(backgroundBitmap, -(backgroundWidth - appWidth) / 2, 0, null);
        drawGround(canvas);
        drawPlayer(canvas);
        drawObstacles(canvas);
        drawScore(canvas);
        if (engine.isPaused() || engine.isGameOver()) {
            drawPauseOverlay(canvas);
        }
        //debug(canvas);
    }

    private void drawGround(Canvas canvas) {
        synchronized (engine.getGroundLeft()) {
            for (Wall wall : engine.getGroundLeft()) {
                if (wall.y + engine.groundHeight > 0)
                    canvas.drawBitmap(groundLeftBitmap, wall.x, wall.y, null);
            }
        }
        synchronized (engine.getGroundRight()) {
            for (Wall wall : engine.getGroundRight()) {
                if (wall.y + engine.groundHeight > 0)
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
            if (obstacle.y + obstacle.height > 0)
                canvas.drawBitmap(getObstacleBitmap(obstacle.x, obstacle.height), obstacle.x, obstacle.y, null);
        }
    }

    private Bitmap getObstacleBitmap(int x, int height) {
        if (height == engine.obstacleHeight1) {
            if (x == engine.groundWidth)
                return obstacle1;
            else if (x == engine.groundWidth + engine.playerSize * 3 / 4)
                return obstacle1_dual;
            return obstacle1_mirrored;
        } else if (height == engine.obstacleHeight2) {
            if (x == engine.groundWidth)
                return obstacle2;
            else if (x == engine.groundWidth + engine.playerSize * 3 / 4)
                return obstacle2_dual;
            return obstacle2_mirrored;
        } else {
            if (x == engine.groundWidth)
                return obstacle3;
            else if (x == engine.groundWidth + engine.playerSize * 3 / 4)
                return obstacle3_dual;
            return obstacle3_mirrored;
        }
    }

    private void drawScore(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(montserratSemiBold);
        String scoreText = String.valueOf(engine.getCountPassedObstacles());
        float textWidth = paint.measureText(scoreText);
        canvas.drawText(scoreText, (appWidth - textWidth) / 2, 200, paint);
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
            textWidth = textPaint.measureText("Вы проиграли!");
            canvas.drawText("Вы проиграли!", (appWidth - textWidth) / 2, appHeight / 2 - 400, textPaint);
            textPaint.setTextSize(60);
            textWidth = textPaint.measureText("Рекорд: " + engine.getBestScore());
            canvas.drawText("Рекорд: " + engine.drawScore, (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
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
}
