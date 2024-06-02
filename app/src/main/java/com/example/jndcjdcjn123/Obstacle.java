package com.example.jndcjdcjn123;

import android.graphics.Bitmap;

public class Obstacle {
    public int x, y, height, width;
    public Bitmap bitmap;
    Hitbox hitbox;

    public Obstacle(int x, int y, int width, int height, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.bitmap = bitmap;
        hitbox = new Hitbox();
        hitbox.addRectangle(x, y, width, height);
    }

    public void updateHitbox() {
        hitbox.area.clear();
        hitbox.addRectangle(x, y, width, height);
    }
}
