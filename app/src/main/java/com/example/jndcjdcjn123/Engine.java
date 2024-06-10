package com.example.jndcjdcjn123;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Engine {
    private static final String TAG = "Engine";
    private int appWidth, appHeight;
    private int speed = 10, speedReturn = 15, speedGain = 3, speedIncreaseInterval = 10, originPlayerX, originPlayerY, offsetGround = 0;
    private int startSpeed = 10, startSpeedReturn = 15, countPassedObstacles = 0, lastIncreasedScore = 0, bestScore = 0;
    int drawScore = 0, groundWidth, groundHeight, playerSize, obstacleWidth, obstacleHeight1, obstacleHeight2, obstacleHeight3;
    private boolean paused = false, gameOver = false;
    private MoveDirection direction = MoveDirection.IDLE;
    private Player player;
    private GameControlInterface gameControl;

    private ArrayList<Wall> groundLeft = new ArrayList<>(), groundRight = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();

    static Random random = new Random(System.currentTimeMillis() / 1000L);

    enum MoveDirection {
        LEFT, SPLIT, RIGHT, IDLE
    }

    public Engine(Context context, int width, int height, GameControlInterface gameControl) {
        this.gameControl = gameControl;
        this.appHeight = height;
        this.appWidth = width;
        initializeDimensions();
        initializePlayer();
        initializeGround();
        initializeObstacles();
    }

    private void initializeDimensions() {
        groundWidth = appWidth * 10 / 100;
        groundHeight = groundWidth * 4;
        playerSize = appWidth * 20 / 100;
        obstacleWidth = appWidth - groundWidth * 2 - playerSize * 3 / 2;
        obstacleHeight1 = obstacleWidth / 5;
        obstacleHeight2 = obstacleWidth * 36 / 100;
        obstacleHeight3 = obstacleWidth * 42 / 100;
    }

    private void initializePlayer() {
        originPlayerX = (appWidth - playerSize) / 2;
        originPlayerY = (int) (appHeight * 0.80) - playerSize;
        player = new Player(originPlayerX, originPlayerY, playerSize);
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
        obstacles.add(new Obstacle(groundWidth, -2 * obstacleHeight1, obstacleWidth, obstacleHeight1));
        int lastY = -2 * obstacleHeight1;
        for (int i = 0; i < 9; ++i) {
            int x = getRandomXPosition();
            int y = getRandomYPosition();
            int curY = lastY - 5 * y - speed * 35;
            obstacles.add(new Obstacle(x, curY, obstacleWidth, y));
            lastY = curY;
        }
    }

    private int getRandomXPosition() {
        int rand = getRandomNumber(1, 3);
        if (rand == 2) {
            return groundWidth + player.playerSize * 3 / 4;
        } else if (rand == 3) {
            return appWidth - groundWidth - obstacleWidth;
        } else {
            return groundWidth;
        }
    }

    private int getRandomYPosition() {
        int rand = getRandomNumber(1, 3);
        if (rand == 2) {
            return obstacleHeight2;
        } else if (rand == 3) {
            return obstacleHeight3;
        } else {
            return obstacleHeight1;
        }
    }

    public static int getRandomNumber(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public void restartGame() {
        if (countPassedObstacles > bestScore) {
            bestScore = countPassedObstacles;
        }
        obstacles.clear();
        groundLeft.clear();
        groundRight.clear();
        initializePlayer();
        initializeGround();
        initializeObstacles();
        speed = startSpeed;
        speedReturn = startSpeedReturn;
        countPassedObstacles = 0;
        paused = false;
        gameOver = false;
    }

    public void updatePause() {
        paused = !paused;
    }

    public void increaseSpeed() {
        speed += speedGain;
        speedReturn += speedGain;
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
            if (countPassedObstacles > getBestScore()) {
                setBestScore(countPassedObstacles);
            }
            drawScore = bestScore;
            gameControl.updatePauseVisibility();
        }
    }

    private void updateGroundPosition() {
        synchronized (groundLeft) {
            for (Wall wall : groundLeft) {
                wall.y += speed;
            }
        }
        synchronized (groundRight) {
            for (Wall wall : groundRight) {
                wall.y += speed;
            }
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
            player.x1 = Math.min(player.x1 + speedReturn, originPlayerX);
        } else {
            player.x1 = Math.max(player.x1 - speedReturn, originPlayerX);
        }
        if (player.x2 < originPlayerX + player.playerSize / 2) {
            player.x2 = Math.min(player.x2 + speedReturn, originPlayerX + player.playerSize / 2);
        } else {
            player.x2 = Math.max(player.x2 - speedReturn, originPlayerX + player.playerSize / 2);
        }
    }

    private void moveSplitPlayer() {
        if (direction == MoveDirection.LEFT) {
            player.x1 = Math.max(player.x1 - speedReturn, groundWidth);
            player.x2 = Math.max(player.x2 - 2 * speedReturn, player.x1 + player.playerSize / 2);
        } else if (direction == MoveDirection.RIGHT) {
            player.x2 = Math.min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
            player.x1 = Math.min(player.x1 + 2 * speedReturn, player.x2 - player.playerSize / 2);
        } else if (direction == MoveDirection.SPLIT) {
            player.x1 = Math.max(player.x1 - speedReturn, groundWidth);
            player.x2 = Math.min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
        }
    }

    private void updateUnifiedPlayerPosition() {
        if (direction == MoveDirection.IDLE && player.x1 != originPlayerX) {
            if (player.x1 < originPlayerX) {
                player.x1 = Math.min(player.x1 + speedReturn, originPlayerX);
                player.x2 = Math.min(player.x2 + speedReturn, originPlayerX + player.playerSize / 2);
            } else {
                player.x1 = Math.max(player.x1 - speedReturn, originPlayerX);
                player.x2 = Math.max(player.x2 - speedReturn, originPlayerX + player.playerSize / 2);
            }
        } else if (direction == MoveDirection.LEFT) {
            player.x1 = Math.max(player.x1 - speedReturn, groundWidth);
            player.x2 = Math.max(player.x2 - speedReturn, groundWidth + player.playerSize / 2);
        } else if (direction == MoveDirection.RIGHT) {
            player.x1 = Math.min(player.x1 + speedReturn, appWidth - groundWidth - player.playerSize);
            player.x2 = Math.min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
        } else if (direction == MoveDirection.SPLIT) {
            player.x1 = Math.max(player.x1 - speedReturn, groundWidth);
            player.x2 = Math.min(player.x2 + speedReturn, appWidth - groundWidth - player.playerSize / 2);
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
                int curY = minY - 5 * y - speed * 35;
                obstacles.set(i, new Obstacle(x, curY, obstacleWidth, y));
                countPassedObstacles++;
                break;
            }
        }
    }

    public boolean checkPlayerCollision() {
        for (Obstacle obstacle : obstacles) {
            if (Hitbox.checkIntersect(player.hitbox1, obstacle.hitbox) || Hitbox.checkIntersect(player.hitbox2, obstacle.hitbox)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Wall> getGroundLeft() {
        return groundLeft;
    }

    public ArrayList<Wall> getGroundRight() {
        return groundRight;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getCountPassedObstacles() {
        return countPassedObstacles;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setDirection(MoveDirection direction) {
        this.direction = direction;
    }

    public MoveDirection getDirection() {
        return direction;
    }

    public synchronized void setBestScore(int bestScore) {
        Log.d(TAG, "Setting best score: " + bestScore);
        this.bestScore = bestScore;
    }

    public synchronized int getBestScore() {
        return bestScore;
    }
}
