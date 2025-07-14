package com.Angrybird;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class GameObject {
    private static final long serialVersionUID = 1L; // Ensures compatibility during deserialization

    public float health;
    public Body body; // because Body is not serializable
    public Texture texture; // Texture of the object
    public World world; // because World is not serializable
    public boolean collision = false;
    public boolean launched = false;

    // Global list to store GameObjects that need to be destroyed
    private static List<GameObject> objectsToDestroy = new ArrayList<>();

    public GameObject() {
        this.health = 10f; // Default health
    }

    public void onCollision(Body other, float collisionForce) {
        if (launched) collision = true;

        float damageThreshold = 0.4f; // Example threshold
        if (collisionForce >= damageThreshold) {
            float damage = (collisionForce - damageThreshold) * 0.5f; // Scale damage
            health -= damage;
            System.out.println("Collision! Force: " + collisionForce + ", Damage: " + damage + ", Health: " + health);

            if (health <= 0) {
                collapse();
            }
        }
    }

    public void collapse() {
        objectsToDestroy.add(this);
    }

    // Process the objects that need to be destroyed after the world step
    public static void updateObjectsToDestroy() {
        for (GameObject obj : objectsToDestroy) {
            obj.destroy(); // Call destroy logic
        }
        objectsToDestroy.clear(); // Clear the list after processing
    }

    public Vector2 getPosition() {
        return body != null ? body.getPosition() : new Vector2(0, 0); // Return the position of the Box2D body
    }

    // Abstract method for destruction
    public abstract void destroy();

    public void setHealth(float health) {
        this.health = health;
    }

}
