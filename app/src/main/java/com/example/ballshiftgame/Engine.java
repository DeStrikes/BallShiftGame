package com.example.ballshiftgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Engine {
    private static final String TAG = "Engine";
    private int appWidth, appHeight;
    int speed = 10;
    private int speedReturn = 15;
    private int speedGain = 3;
    private int speedIncreaseInterval = 10;
    private int originPlayerX;
    private int originPlayerY;
    private int offsetGround = 0;
    private int startSpeed = 10, startSpeedReturn = 15, countPassedObstacles = 0, lastIncreasedScore = 0, bestScore = 0;
    int drawScore = 0, groundWidth, groundHeight, playerSize, obstacleWidth, obstacleHeight1, obstacleHeight2, obstacleHeight3;
    private boolean paused = false, gameOver = false;
    private MoveDirection direction = MoveDirection.IDLE;
    private Player player;
    private GameControlInterface gameControl;

    private ArrayList<Wall> groundLeft = new ArrayList<>(), groundRight = new ArrayList<>();
    private ArrayList<Obstacle> obstacles = new ArrayList<>();

    static Random random = new Random(System.currentTimeMillis() / 1000L);

    private static final String PREFS_NAME = "MyPreferences";
    private static final String UPGRADE_DISTANCE_KEY = "upgrade_distance";
    private static final String UPGRADE_SPEED_KEY = "upgrade_speed";
    private static final String UPGRADE_SPEED_RETURN_KEY = "upgrade_speed_return";
    private static final String UPGRADE_SCORE_MULTIPLIER_LEVEL_KEY = "upgrade_score_multiplier_level";
    private static final String UPGRADE_SCORE_MULTIPLIER_KEY = "upgrade_score_multiplier";
    private static final String BALANCE_KEY = "balance";
    private static final String UPGRADE_DISTANCE_COST_KEY = "upgrade_distance_cost";
    private static final String UPGRADE_SPEED_COST_KEY = "upgrade_speed_cost";
    private static final String UPGRADE_SPEED_RETURN_COST_KEY = "upgrade_speed_return_cost";
    private static final String UPGRADE_SCORE_MULTIPLIER_COST_KEY = "upgrade_score_multiplier_cost";
    private static final String CURRENT_SKIN_KEY = "current_skin";
    private static final String SKIN_PURCHASED_PREFIX = "skin_purchased_";
    private static final String UPGRADE_START_SPEED_KEY = "upgrade_start_speed";
    private static final String UPGRADE_START_SPEED_COST_KEY = "upgrade_start_speed_cost";

    List<Planet> planets;

    private int upgradeDistance = 0;
    private int upgradeSpeed = 0;
    private int upgradeSpeedReturn = 0;
    private int upgradeScoreMultiplierLevel = 0;
    private float upgradeScoreMultiplier = 1.0f;
    private int balance = 0;
    private int upgradeDistanceCost = 50;
    private int upgradeSpeedCost = 50;
    private int upgradeSpeedReturnCost = 50;
    private int upgradeScoreMultiplierCost = 50;
    private int currentSkin = 0;
    private boolean[] purchasedSkins;

    private int upgradeStartSpeed = 0;
    private int upgradeStartSpeedCost = 70;

    enum MoveDirection {
        LEFT, SPLIT, RIGHT, IDLE
    }

    public Engine(Context context, int width, int height, GameControlInterface gameControl) {
        this.gameControl = gameControl;
        this.appHeight = height;
        this.appWidth = width;
        loadPreferences(context);
        initializeDimensions();
        initializePlayer();
        initializeGround();
        initializeObstacles();
        initializePlanets();
        speed = upgradeSpeed + startSpeed;
        speedReturn = startSpeedReturn + upgradeSpeedReturn;
        purchasedSkins[0] = true;
    }

    private void initializePlanets() {
        planets = new ArrayList<>();
        planets.add(new Planet(appWidth * 15 / 100, 0, 0));
        planets.add(new Planet(appWidth * 20 / 100, -appHeight, 1));
        planets.add(new Planet(appWidth * 18 / 100, -2 * appHeight, 2));
        planets.add(new Planet( appWidth * 25 / 100, -3 * appHeight, 3));
        planets.add(new Planet( appWidth * 30 / 100, -4 * appHeight, 4));
    }

    private void loadPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        upgradeDistance = sharedPreferences.getInt(UPGRADE_DISTANCE_KEY, 0);
        upgradeSpeed = sharedPreferences.getInt(UPGRADE_SPEED_KEY, 0);
        upgradeSpeedReturn = sharedPreferences.getInt(UPGRADE_SPEED_RETURN_KEY, 0);
        upgradeScoreMultiplierLevel = sharedPreferences.getInt(UPGRADE_SCORE_MULTIPLIER_LEVEL_KEY, 0);
        upgradeScoreMultiplier = sharedPreferences.getFloat(UPGRADE_SCORE_MULTIPLIER_KEY, 1.0f);
        balance = sharedPreferences.getInt(BALANCE_KEY, 0);
        currentSkin = sharedPreferences.getInt(CURRENT_SKIN_KEY, 0);
        purchasedSkins = new boolean[SkinMenuActivity.SKIN_COUNT];
        for (int i = 0; i < SkinMenuActivity.SKIN_COUNT; i++) {
            purchasedSkins[i] = sharedPreferences.getBoolean(SKIN_PURCHASED_PREFIX + i, false);
        }

        upgradeDistanceCost = sharedPreferences.getInt(UPGRADE_DISTANCE_COST_KEY, 50);
        upgradeSpeedCost = sharedPreferences.getInt(UPGRADE_SPEED_COST_KEY, 50);
        upgradeSpeedReturnCost = sharedPreferences.getInt(UPGRADE_SPEED_RETURN_COST_KEY, 50);
        upgradeScoreMultiplierCost = sharedPreferences.getInt(UPGRADE_SCORE_MULTIPLIER_COST_KEY, 50);

        upgradeStartSpeed = sharedPreferences.getInt(UPGRADE_START_SPEED_KEY, 0);
        upgradeStartSpeedCost = sharedPreferences.getInt(UPGRADE_START_SPEED_COST_KEY, 50);
        startSpeed -= upgradeSpeed;
        speedReturn += upgradeSpeedReturn;
        startSpeed += upgradeStartSpeed;
    }

    public void savePreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(UPGRADE_DISTANCE_KEY, upgradeDistance);
        editor.putInt(UPGRADE_SPEED_KEY, upgradeSpeed);
        editor.putInt(UPGRADE_SPEED_RETURN_KEY, upgradeSpeedReturn);
        editor.putInt(UPGRADE_SCORE_MULTIPLIER_LEVEL_KEY, upgradeScoreMultiplierLevel);
        editor.putFloat(UPGRADE_SCORE_MULTIPLIER_KEY, upgradeScoreMultiplier);
        editor.putInt(BALANCE_KEY, balance);
        editor.putInt(CURRENT_SKIN_KEY, currentSkin);
        for (int i = 0; i < SkinMenuActivity.SKIN_COUNT; i++) {
            editor.putBoolean(SKIN_PURCHASED_PREFIX + i, purchasedSkins[i]);
        }

        editor.putInt(UPGRADE_DISTANCE_COST_KEY, upgradeDistanceCost);
        editor.putInt(UPGRADE_SPEED_COST_KEY, upgradeSpeedCost);
        editor.putInt(UPGRADE_SPEED_RETURN_COST_KEY, upgradeSpeedReturnCost);
        editor.putInt(UPGRADE_SCORE_MULTIPLIER_COST_KEY, upgradeScoreMultiplierCost);

        editor.putInt(UPGRADE_START_SPEED_KEY, upgradeStartSpeed);
        editor.putInt(UPGRADE_START_SPEED_COST_KEY, upgradeStartSpeedCost);

        editor.apply();
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
        obstacles.add(new Obstacle(groundWidth, -2 * obstacleHeight1, obstacleWidth, obstacleHeight1, getRandomNumber(0, 2)));
        int lastY = -2 * obstacleHeight1;
        for (int i = 0; i < 9; ++i) {
            int x = getRandomXPosition();
            int y = getRandomYPosition();
            int curY = lastY - 5 * obstacleHeight2 - speed * (35 + upgradeDistance * 3 / 2);
            obstacles.add(new Obstacle(x, curY, obstacleWidth, y, getRandomNumber(0, 2)));
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
        synchronized (groundLeft) {
            groundLeft.clear();
        }
        synchronized (groundRight) {
            groundRight.clear();
        }
        obstacles.clear();
        initializePlayer();
        initializeGround();
        initializeObstacles();
        initializePlanets();
        speed = upgradeSpeed + startSpeed;
        speedReturn = startSpeedReturn + upgradeSpeedReturn;
        countPassedObstacles = 0;
        paused = false;
        gameOver = false;
    }

    public void updatePause() {
        paused = !paused;
    }

    public void increaseSpeed() {
        speed += speedGain;
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
        updatePlanetPositions();
        if (checkPlayerCollision()) {
            gameOver = true;
            addPointsToBalance(countPassedObstacles);
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
        synchronized (groundLeft) {
            int minY = getMinY(groundLeft);
            resetGroundPosition(groundLeft, minY);
        }
        synchronized (groundRight) {
            int minY = getMinY(groundRight);
            resetGroundPosition(groundRight, minY);
        }
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
                int curY = minY - 5 * obstacleHeight2 - speed * (35 + upgradeDistance * 3 / 2);
                obstacles.set(i, new Obstacle(x, curY, obstacleWidth, y, getRandomNumber(0, 2)));
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

    private void updatePlanetPositions() {
        if (!isPaused() && !isGameOver()) {
            for (Planet planet : planets) {
                planet.y += speed / 10;
            }
            resetPlanetsIfNeeded();
        }
    }

    private void resetPlanetsIfNeeded() {
        for (int i = 0; i < planets.size(); ++i) {
            Planet planet = planets.get(i);
            if (planet.y >= appHeight) {
                planet.y = planets.get((i + planets.size() - 1) % planets.size()).y - appHeight;
            }
        }
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

    public int getBalance() {
        return balance;
    }

    public void addToBalance(int amount) {
        balance += amount;
    }

    public void subtractFromBalance(int amount) {
        if (balance >= amount) {
            balance -= amount;
        }
    }

    public void upgradeDistance() {
        upgradeDistance++;
    }

    public void upgradeSpeedReturn() {
        upgradeSpeedReturn++;
    }

    public void upgradeStartSpeed() {
        upgradeStartSpeed++;
    }

    public void upgradeScoreMultiplier() {
        upgradeScoreMultiplierLevel++;
        upgradeScoreMultiplier += 0.25;
    }

    private void addPointsToBalance(int points) {
        balance += Math.floor(points * upgradeScoreMultiplier);
    }

    public int getUpgradeDistance() {
        return upgradeDistance;
    }

    public int getUpgradeSpeed() {
        return upgradeSpeed;
    }

    public int getUpgradeSpeedReturn() {
        return upgradeSpeedReturn;
    }

    public int getUpgradeScoreMultiplierLevel() {
        return upgradeScoreMultiplierLevel;
    }

    public float getUpgradeScoreMultiplier() {
        return upgradeScoreMultiplier;
    }

    public int getUpgradeDistanceCost() {
        return upgradeDistanceCost;
    }

    public void setUpgradeDistanceCost(int upgradeDistanceCost) {
        this.upgradeDistanceCost = upgradeDistanceCost;
    }

    public int getUpgradeSpeedCost() {
        return upgradeSpeedCost;
    }

    public void setUpgradeSpeedCost(int upgradeSpeedCost) {
        this.upgradeSpeedCost = upgradeSpeedCost;
    }

    public int getUpgradeSpeedReturnCost() {
        return upgradeSpeedReturnCost;
    }

    public void setUpgradeSpeedReturnCost(int upgradeSpeedReturnCost) {
        this.upgradeSpeedReturnCost = upgradeSpeedReturnCost;
    }

    public int getUpgradeScoreMultiplierCost() {
        return upgradeScoreMultiplierCost;
    }

    public void setUpgradeScoreMultiplierCost(int upgradeScoreMultiplierCost) {
        this.upgradeScoreMultiplierCost = upgradeScoreMultiplierCost;
    }

    public int getUpgradeStartSpeed() {
        return upgradeStartSpeed;
    }

    public int getUpgradeStartSpeedCost() {
        return upgradeStartSpeedCost;
    }

    public void setUpgradeStartSpeedCost(int upgradeStartSpeedCost) {
        this.upgradeStartSpeedCost = upgradeStartSpeedCost;
    }

    public int getCurrentSkin() {
        return currentSkin;
    }

    public void setCurrentSkin(int currentSkin) {
        this.currentSkin = currentSkin;
    }

    public boolean isSkinPurchased(int skinIndex) {
        return purchasedSkins[skinIndex];
    }

    public void purchaseSkin(int skinIndex) {
        purchasedSkins[skinIndex] = true;
    }
}
