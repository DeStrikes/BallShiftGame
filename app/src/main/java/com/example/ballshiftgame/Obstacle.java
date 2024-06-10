package com.example.ballshiftgame;

public class Obstacle {
    public int x, y, height, width;
    Hitbox hitbox;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        hitbox = new Hitbox();
        hitbox.addRectangle(x, y, width, height);
    }

    public void updateHitbox() {
        hitbox.area.clear();
        hitbox.addRectangle(x, y, width, height);
    }
}
