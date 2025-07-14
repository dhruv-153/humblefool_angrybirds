package com.Angrybird.BirdPackage;

import java.io.Serializable;

public class BirdData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String type; // Bird type (e.g., "Red", "Blue", etc.)
    public float angle; // Angle of the bird
    public float posX, posY; // Position of the bird
    public float health; // Bird health

    public BirdData(String type, float angle, float posX, float posY, float health) {
        this.type = type;
        this.angle = angle;
        this.posX = posX;
        this.posY = posY;
        this.health = health;
    }
}
