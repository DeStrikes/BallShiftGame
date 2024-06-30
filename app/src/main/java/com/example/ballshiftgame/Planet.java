package com.example.ballshiftgame;

import android.graphics.Bitmap;

public class Planet {
    int size;
    int y;
    int type;

    public Planet(int size, int y, int type) {
        this.type = type;
        this.size = size;
        this.y = y;
    }
}
