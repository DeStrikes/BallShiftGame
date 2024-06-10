package com.example.ballshiftgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

public class Hitbox {
    private class Rectangle {
        public int x, y, width, height;
        Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        boolean intersects(Rectangle other) {
            return this.x < other.x + other.width && this.x + this.width > other.x && this.y < other.y + other.height && this.y + this.height > other.y;
        }
    }
    Hitbox() {
        area = new ArrayList<>();
    }
    ArrayList<Rectangle> area;

    public void addRectangle(int x, int y, int width, int height) {
        area.add(new Rectangle(x, y, width, height));
    }
    public void move(int x, int y) {
        for (Rectangle rect : area) {
            rect.x += x;
            rect.y += y;
        }
    }
    public static boolean checkIntersect(Hitbox hitbox1, Hitbox hitbox2) {
        for (Rectangle rect1 : hitbox1.area) {
            for (Rectangle rect2 : hitbox2.area) {
                if (rect1.intersects(rect2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void drawHitbox(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        for (Rectangle rectangle : area) {
            canvas.drawRect(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, paint);
        }
    }
}
