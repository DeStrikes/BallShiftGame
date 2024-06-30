package com.example.ballshiftgame;

import android.graphics.Bitmap;

public class Obstacle {
    public int x, y, height, width, type;
    Hitbox hitbox;

    public Obstacle(int x, int y, int width, int height, int type) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.type = type;
        hitbox = new Hitbox();
        hitbox.addRectangle(x, y, width, height);
    }

    public void updateHitbox() {
        hitbox.area.clear();
        hitbox.addRectangle(x, y, width, height);
    }
}
