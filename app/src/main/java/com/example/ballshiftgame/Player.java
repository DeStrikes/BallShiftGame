package com.example.ballshiftgame;

public class Player {
    int x1, y1, x2, y2, playerSize;
    boolean split = false;
    Hitbox hitbox1, hitbox2;

    public Player(int x, int y, int playerSize) {
        this.playerSize = playerSize;
        this.x1 = x;
        this.y1 = y;
        this.x2 = x + playerSize / 2;
        this.y2 = y;
        hitbox1 = new Hitbox();
        hitbox2 = new Hitbox();
        updateHitboxes();
    }

    public void checkSplit() {
        split = x1 + playerSize / 2 != x2;
    }

    public int getProjectionX(int x, int y, boolean isLeft) {
        int oX = x1 + playerSize / 2;
        int oY = y1 + playerSize / 2;
        int R = playerSize / 2;
        double delta = Math.sqrt(R * R - (y - oY) * (y - oY));
        if (isLeft) {
            return (int) Math.floor(oX - delta);
        } else {
            return (int) Math.floor(oX + delta);
        }
    }
    private void addRectangleFromTwoCoordinates(int xx1, int yy1, int xx2, int yy2, boolean isLeft) {
        if (isLeft) {
            int oX = x1 + playerSize / 2, oY = y1 + playerSize / 2;
            int xx3, xx4;
            int yy3 = yy1, yy4 = yy2;
            xx3 = getProjectionX(xx1, yy1, true);
            hitbox1.addRectangle(xx3, yy3, xx1 - xx3, playerSize / 2);
        } else {
            int oX = x2, oY = y2 + playerSize / 2;
            int xx3, xx4;
            int yy3 = yy1, yy4 = yy2;
            xx3 = getProjectionX(xx1, yy1, false);
            hitbox2.addRectangle(xx1, yy1, xx3 - xx1, playerSize / 2);
        }
    }
    public void updateHitboxes() {
        hitbox1.area.clear();
        hitbox2.area.clear();
        hitbox1.addRectangle(x2 - 2, y1, 2, playerSize);
        hitbox2.addRectangle(x2, y2, 2, playerSize);
        addRectangleFromTwoCoordinates(x1 + playerSize / 2, y1 + playerSize / 2 - playerSize / 4, x1 + playerSize / 2, y1 + playerSize / 2 + playerSize / 4, true);
        addRectangleFromTwoCoordinates(x2, y2 + playerSize / 2 - playerSize / 4, x2, y2 + playerSize / 2 + playerSize / 4, false);
        addRectangleFromTwoCoordinates(x1 + playerSize / 2, y1 + playerSize / 2 - playerSize * 3 / 8, x1 + playerSize / 2, y1 + playerSize / 2 + playerSize * 3 / 8, true);
        addRectangleFromTwoCoordinates(x2, y2 + playerSize / 2 - playerSize * 3 / 8, x2, y2 + playerSize / 2 + playerSize * 3 / 8, false);
    }
}
