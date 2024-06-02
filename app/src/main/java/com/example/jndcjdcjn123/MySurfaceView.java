package com.example.jndcjdcjn123;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceThread surfaceThread;
    private Bitmap backgroundBitmap, groundLeftBitmap, groundRightBitmap, playerBitmap, playerLeftBitmap, playerRightBitmap;
    private Bitmap obstacle1, obstacle2, obstacle3, obstacle1_dual, obstacle1_mirrored, obstacle2_dual, obstacle2_mirrored, obstacle3_dual, obstacle3_mirrored;
    private int appWidth, appHeight, speed = 10, speedReturn = 15, countPassedObstacles = 0, lastIncreasedScore = 0, lastBestScore = 0;
    private ArrayList<Wall> groundLeft = new ArrayList<>(), groundRight = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private int offsetGround = 0, speedGain = 3, speedIncreaseInterval = 10, startSpeed = 10, startSpeedReturn = 15;
    private int groundWidth, groundHeight, originPlayerX, originPlayerY, obstacleWidth, obstacleHeight1, obstacleHeight2, obstacleHeight3, backgroundWidth;
    private Player player;
    private Typeface montserratBlack, montserratSemiBold;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_PERIOD = 1000 / TARGET_FPS;
    private enum MoveDirection {
        LEFT,
        SPLIT,
        RIGHT,
        IDLE
    }
    public boolean paused = false;
    public boolean gameOver = false;
    public int bestScore = 0;
    private MoveDirection direction = MoveDirection.IDLE;
    private GameControlInterface gameControl; // ссылка на интерфейс

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
        groundWidth = appWidth * 10 / 100;
        groundHeight = groundWidth * 4;
        getHolder().addCallback(this);
        backgroundWidth = (int) Math.floor((float) (height + 100) / 1920 * width);
        backgroundBitmap = loadScaledBitmap(context, R.drawable.space_bg, (int) Math.floor((float) (height + 100) / 1920 * width), height + 100);
        groundLeftBitmap = loadScaledBitmap(context, R.drawable.ground, groundWidth, groundHeight);
        groundRightBitmap = loadScaledBitmap(context, R.drawable.ground_mirrored, groundWidth, groundHeight);
        int playerSize = appWidth * 20 / 100;
        int playerX = originPlayerX = (appWidth - playerSize) / 2;
        int playerY = originPlayerY = (int) (appHeight * 0.80) - playerSize;
        player = new Player(playerX, playerY, playerSize);
        playerBitmap = loadScaledBitmap(context, R.drawable.ball, playerSize, playerSize);
        playerLeftBitmap = Bitmap.createBitmap(playerBitmap, 0, 0, playerSize / 2, playerSize);
        playerRightBitmap = Bitmap.createBitmap(playerBitmap, playerSize / 2, 0, playerSize / 2, playerSize);
        obstacleWidth = appWidth - groundWidth * 2 - playerSize * 3 / 2;
        obstacleHeight1 = obstacleWidth / 5;
        obstacleHeight2 = obstacleWidth * 36 / 100;
        obstacleHeight3 = obstacleWidth * 42 / 100;
        obstacle1 = loadScaledBitmap(context, R.drawable.obstacle1, obstacleWidth, obstacleHeight1);
        obstacle1_dual = loadScaledBitmap(context, R.drawable.obstacle1_dual, obstacleWidth, obstacleHeight1);
        obstacle1_mirrored = loadScaledBitmap(context, R.drawable.obstacle1_mirrored, obstacleWidth, obstacleHeight1);
        obstacle2 = loadScaledBitmap(context, R.drawable.obstacle2, obstacleWidth, obstacleHeight2);
        obstacle2_dual = loadScaledBitmap(context, R.drawable.obstacle2_dual, obstacleWidth, obstacleHeight2);
        obstacle2_mirrored = loadScaledBitmap(context, R.drawable.obstacle2_mirrored, obstacleWidth, obstacleHeight2);
        obstacle3 = loadScaledBitmap(context, R.drawable.obstacle3, obstacleWidth, obstacleHeight3);
        obstacle3_dual = loadScaledBitmap(context, R.drawable.obstacle3_dual, obstacleWidth, obstacleHeight3);
        obstacle3_mirrored = loadScaledBitmap(context, R.drawable.obstacle3_mirrored, obstacleWidth, obstacleHeight3);
        montserratBlack = ResourcesCompat.getFont(context, R.font.montserrat_black);
        montserratSemiBold = ResourcesCompat.getFont(context, R.font.montserrat_semibold);
        surfaceThread = new SurfaceThread(getHolder(), this);
        initializeGround();
        initializeObstacles();
    }

    private Bitmap loadScaledBitmap(Context context, int resId, int width, int height) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resId), width, height, true);
    }

    private void initializeGround() {
        groundLeft.add(new Wall(-offsetGround, -2 * groundHeight));
        groundRight.add(new Wall(appWidth - (groundWidth - offsetGround), -2 * groundHeight));
        int last = -2 * groundHeight;
        while (groundLeft.get(groundLeft.size() - 1).y <= appHeight) {
            groundLeft.add(new Wall(-offsetGround, last + groundHeight));
            groundRight.add(new Wall(appWidth - (groundWidth - offsetGround), last + groundHeight));
            last += groundHeight;
        }
    }

    private void initializeObstacles() {
        obstacles.add(new Obstacle(groundWidth, -2 * obstacleHeight1, obstacleWidth, obstacleHeight1, obstacle1));
        int lastY = -2 * obstacleHeight1;
        for (int i = 0; i < 9; ++i) {
            int x = getRandomXPosition();
            int y = getRandomYPosition();
            Bitmap curBitmap = getObstacleBitmap(x, y);
            int curY = lastY - 5 * y - speed * 35;
            obstacles.add(new Obstacle(x, curY, obstacleWidth, y, curBitmap));
            lastY = curY;
        }
    }

    private int getRandomXPosition() {
        switch (getRandomNumber(1, 3)) {
            case 1:
                return groundWidth;
            case 2:
                return groundWidth + player.playerSize * 3 / 4;
            case 3:
                return appWidth - groundWidth - obstacleWidth;
            default:
                return groundWidth;
        }
    }
    private int getRandomYPosition() {
        switch (getRandomNumber(1, 3)) {
            case 1:
                return obstacleHeight1;
            case 2:
                return obstacleHeight2;
            case 3:
                return obstacleHeight3;
            default:
                return obstacleHeight1;
        }
    }

    private Bitmap getObstacleBitmap(int x, int y) {
        if (y == obstacleHeight1) {
            if (x == groundWidth)
                return obstacle1;
            else if (x == groundWidth  + player.playerSize * 3 / 4)
                return obstacle1_dual;
            return obstacle1_mirrored;
        } else if (y == obstacleHeight2) {
            if (x == groundWidth)
                return obstacle2;
            else if (x == groundWidth  + player.playerSize * 3 / 4)
                return obstacle2_dual;
            return obstacle2_mirrored;
        } else {
            if (x == groundWidth)
                return obstacle3;
            else if (x == groundWidth  + player.playerSize * 3 / 4)
                return obstacle3_dual;
            return obstacle3_mirrored;
        }
    }

    public void restartGame() {
        if (bestScore != lastBestScore) {
            lastBestScore = bestScore;
        }
        obstacles.clear();
        groundLeft.clear();
        groundRight.clear();
        int playerSize = appWidth * 20 / 100;
        player = new Player(originPlayerX, originPlayerY, playerSize);
        initializeGround();
        initializeObstacles();
        speed = startSpeed;
        speedReturn = startSpeedReturn;
        countPassedObstacles = 0;
        paused = false;
        gameOver = false;
        gameControl.updatePauseVisibility();
    }

    public void updatePause() {
        paused = !paused;
        gameControl.updatePauseVisibility();
    }

    public static int getRandomNumber(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public void increaseSpeed() {
        speed += speedGain;
        speedReturn += speedGain;
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
                direction = MoveDirection.LEFT;
            } else if (x > (float) (2 * appWidth) / 3) {
                direction = MoveDirection.RIGHT;
            } else {
                direction = MoveDirection.SPLIT;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            direction = MoveDirection.IDLE;
        }
        return true;
    }

    public boolean checkPlayerCollision() {
        for (Obstacle obstacle : obstacles) {
            if (Hitbox.checkIntersect(player.hitbox1, obstacle.hitbox) || Hitbox.checkIntersect(player.hitbox2, obstacle.hitbox)) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        if (paused || gameOver) {
            return;
        }
        if (countPassedObstacles % speedIncreaseInterval == 0 && countPassedObstacles != lastIncreasedScore) {
            increaseSpeed();
            lastIncreasedScore = countPassedObstacles;
        }
        updateGroundPosition();
        updatePlayerPosition();
        updateObstaclesPosition();
        if (checkPlayerCollision()) {
            gameOver = true;
            gameControl.updatePauseVisibility();
        }
    }

    private void updateGroundPosition() {
        for (Wall wall : groundLeft) {
            wall.y += speed;
        }
        for (Wall wall : groundRight) {
            wall.y += speed;
        }
        resetGroundPositionIfNeeded();
    }

    private void resetGroundPositionIfNeeded() {
        int minY = getMinY(groundLeft);
        resetGroundPosition(groundLeft, minY);
        minY = getMinY(groundRight);
        resetGroundPosition(groundRight, minY);
    }

    private int getMinY(ArrayList<Wall> ground) {
        int minY = Integer.MAX_VALUE;
        for (Wall wall : ground) {
            if (wall.y < minY) {
                minY = wall.y;
            }
        }
        return minY;
    }

    private void resetGroundPosition(ArrayList<Wall> ground, int minY) {
        for (Wall wall : ground) {
            if (wall.y >= appHeight) {
                wall.y = minY - groundHeight;
                break;
            }
        }
    }

    private void updatePlayerPosition() {
        player.checkSplit();
        if (player.split) {
            updateSplitPlayerPosition();
        } else {
            updateUnifiedPlayerPosition();
        }
        player.updateHitboxes();
    }

    private void updateSplitPlayerPosition() {
        if (direction == MoveDirection.IDLE && (player.x1 != originPlayerX || player.x2 != originPlayerX + player.playerSize / 2)) {
            resetPlayerToOrigin();
        } else {
            moveSplitPlayer();
        }
    }

    private void resetPlayerToOrigin() {
        if (player.x1 < originPlayerX) {
            player.x1 = min(player.x1 + speedReturn, originPlayerX);
        } else {
            player.x1 = max(player.x1 - speedReturn, originPlayerX);
        }
        if (player.x2 < originPlayerX + player.playerSize / 2) {
            player.x2 = min(player.x2 + speedReturn, originPlayerX + player.playerSize / 2);
        } else {
            player.x2 = max(player.x2 - speedReturn, originPlayerX + player.playerSize / 2);
        }
    }

    private void moveSplitPlayer() {
        if (direction == MoveDirection.LEFT) {
            player.x1 = max(player.x1 - speedReturn, groundWidth);
            player.x2 = max(player.x2 - 2 * speedReturn, player.x1 + player.playerSize / 2);
        } else if (direction == MoveDirection.RIGHT) {
            player.x2 = min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
            player.x1 = min(player.x1 + 2 * speedReturn, player.x2 - player.playerSize / 2);
        } else if (direction == MoveDirection.SPLIT) {
            player.x1 = max(player.x1 - speedReturn, groundWidth);
            player.x2 = min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
        }
    }

    private void updateUnifiedPlayerPosition() {
        if (direction == MoveDirection.IDLE && player.x1 != originPlayerX) {
            if (player.x1 < originPlayerX) {
                player.x1 = min(player.x1 + speedReturn, originPlayerX);
                player.x2 = min(player.x2 + speedReturn, originPlayerX + player.playerSize / 2);
            } else {
                player.x1 = max(player.x1 - speedReturn, originPlayerX);
                player.x2 = max(player.x2 - speedReturn, originPlayerX + player.playerSize / 2);
            }
        } else if (direction == MoveDirection.LEFT) {
            player.x1 = max(player.x1 - speedReturn, groundWidth);
            player.x2 = max(player.x2 - speedReturn, groundWidth + player.playerSize / 2);
        } else if (direction == MoveDirection.RIGHT) {
            player.x1 = min(player.x1 + speedReturn, appWidth - groundWidth - player.playerSize);
            player.x2 = min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
        } else if (direction == MoveDirection.SPLIT) {
            player.x1 = max(player.x1 - speedReturn, groundWidth);
            player.x2 = min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
        }
    }

    private void updateObstaclesPosition() {
        int minY = updateObstaclesY();
        resetObstaclesIfNeeded(minY);
        for (Obstacle obstacle : obstacles) {
            obstacle.updateHitbox();
        }
    }

    private int updateObstaclesY() {
        int minY = Integer.MAX_VALUE;
        for (Obstacle obstacle : obstacles) {
            obstacle.y += speed;
            if (obstacle.y < minY) {
                minY = obstacle.y;
            }
        }
        return minY;
    }

    private void resetObstaclesIfNeeded(int minY) {
        for (int i = 0; i < obstacles.size(); ++i) {
            if (obstacles.get(i).y > appHeight) {
                int x = getRandomXPosition();
                int y = getRandomYPosition();
                Bitmap curBitmap = getObstacleBitmap(x, y);
                int curY = minY - 5 * y - speed * 35;
                obstacles.set(i, new Obstacle(x, curY, obstacleWidth, y, curBitmap));
                countPassedObstacles++;
                break;
            }
        }
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
        if (paused || gameOver) {
            drawPauseOverlay(canvas);
        }
        //debug(canvas);
    }

    private void drawGround(Canvas canvas) {
        for (Wall wall : groundLeft) {
            if (wall.y + groundHeight > 0)
                canvas.drawBitmap(groundLeftBitmap, wall.x, wall.y, null);
        }
        for (Wall wall : groundRight) {
            if (wall.y + groundHeight > 0)
                canvas.drawBitmap(groundRightBitmap, wall.x, wall.y, null);
        }
    }

    private void drawPlayer(Canvas canvas) {
        canvas.drawBitmap(playerLeftBitmap, player.x1, player.y1, null);
        canvas.drawBitmap(playerRightBitmap, player.x2, player.y2, null);
    }

    private void drawObstacles(Canvas canvas) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.y + obstacle.height > 0)
                canvas.drawBitmap(obstacle.bitmap, obstacle.x, obstacle.y, null);
        }
    }

    private void drawScore(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(montserratSemiBold);
        String scoreText = String.valueOf(countPassedObstacles);
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
        if (gameOver) {
            textWidth = textPaint.measureText("Вы проиграли!");
            canvas.drawText("Вы проиграли!", (appWidth - textWidth) / 2, appHeight / 2 - 400, textPaint);
            textPaint.setTextSize(60);
            if (countPassedObstacles > bestScore || bestScore != lastBestScore) {
                bestScore = countPassedObstacles;
                textWidth = textPaint.measureText("Новый рекорд: " + countPassedObstacles);
                canvas.drawText("Новый рекорд: " + countPassedObstacles, (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
            } else {
                textWidth = textPaint.measureText("Рекорд: " + bestScore);
                canvas.drawText("Рекорд: " + bestScore, (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
            }
        } else {
            textWidth = textPaint.measureText("Пауза");
            canvas.drawText("Пауза", (appWidth - textWidth) / 2, appHeight / 2 - 200, textPaint);
        }
    }

    private void debug(Canvas canvas) {
        fpsCounter.logFrame();
        //player.hitbox1.drawHitbox(canvas);
        //player.hitbox2.drawHitbox(canvas);
        Paint paint = new Paint();
        paint.setTextSize(50);
        paint.setColor(Color.WHITE);
        canvas.drawText("FPS: " + String.format("%.2f", fpsCounter.getFPS()), 50, 50, paint);
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
        this.bestScore = bestScore;
    }

    public int getBestScore() {
        return bestScore;
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
                            mySurfaceView.update();
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
