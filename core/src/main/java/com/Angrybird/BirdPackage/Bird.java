package com.Angrybird.BirdPackage;

import com.Angrybird.GameObject;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static com.Angrybird.Main.PIXELS_TO_METERS;

public abstract class Bird extends GameObject {
    private static final long serialVersionUID = 1L; // Serialization version

    private Texture texture; // Marked as transient
    private Body birdBody; // Marked as transient
    private float density;
    private float diameter;
    private World world; // Marked as transient
    private String texturePath; // Save texture path for reinitialization
    private Vector2 initialPosition; // Save bird's initial position for reinitialization

    public Bird(World world, float health, Vector2 slingStartPos, String image_url, float bird_density, float bird_diameter) {
        this.world = world;
        this.texturePath = image_url; // Store the texture path for later use
        this.initialPosition = slingStartPos.cpy(); // Save initial position
        this.density = bird_density;
        this.diameter = bird_diameter;

        // Initialize texture and body
        texture = new Texture(image_url);
        createBirdBody(world, slingStartPos);
        birdBody.setUserData(this);
    }

    public Bird(float health, Vector2 slingStartPos, float bird_density, float bird_diameter) {
        this.world = new World (new Vector2(0,-9.8f), true);
        this.initialPosition = slingStartPos.cpy(); // Save initial position
        this.density = bird_density;
        this.diameter = bird_diameter;

        // Initialize texture and body
        createBirdBody(world, slingStartPos);
        birdBody.setUserData(this);
    }



    public Boolean checkCollisionStatus() {
        return this.collision;
    }

    @Override
    public void destroy() {
        System.out.println("Bird collapsed! Removing texture and destroying body.");
        health = -1;
        if (birdBody != null && world != null) {
            world.destroyBody(birdBody);
            birdBody = null;
        }

        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    private void createBirdBody(World world, Vector2 slingStartPos) {
        BodyDef birdBodyDef = new BodyDef();
        birdBodyDef.type = BodyDef.BodyType.DynamicBody;
        birdBodyDef.position.set(slingStartPos);

        birdBody = world.createBody(birdBodyDef);

        CircleShape birdShape = new CircleShape();
        birdShape.setRadius((diameter / 2) * PIXELS_TO_METERS);

        FixtureDef birdFixtureDef = new FixtureDef();
        birdFixtureDef.shape = birdShape;
        birdFixtureDef.density = density;

        birdBody.createFixture(birdFixtureDef);
        birdShape.dispose();
    }

    public void render(SpriteBatch batch) {
        if (texture == null) return;

        float birdWidthInPixels = diameter * PIXELS_TO_METERS;

        float birdPosX = birdBody.getPosition().x;
        float birdPosY = birdBody.getPosition().y;

        batch.begin();
        batch.draw(texture, birdPosX - birdWidthInPixels / 2,
            birdPosY - birdWidthInPixels / 2,
            birdWidthInPixels, birdWidthInPixels);
        batch.end();
    }

    public abstract void specialAbility();

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public Body getBody() {
        if (birdBody == null) {
            return null;
        }
        return birdBody;
    }

    public void initializeBody(World world) {
        this.world = world; // Set the world
        if (birdBody == null) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(initialPosition); // Use the saved initial position

            CircleShape shape = new CircleShape();
            shape.setRadius((diameter / 2) * PIXELS_TO_METERS); // Use the bird's diameter

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = density;

            birdBody = world.createBody(bodyDef);
            birdBody.createFixture(fixtureDef);

            birdBody.setUserData(this); // Ensure the body references the bird
            shape.dispose();
        }
    }

    public String getType() {
        return this.getClass().getSimpleName(); // Returns the simple class name (e.g., "RedBird", "BlueBird")
    }

    public Vector2 getPosition() {
        if (birdBody != null) {
            return birdBody.getPosition().cpy(); // Return a copy of the body's position
        }
        return initialPosition.cpy(); // Return the initial position if the body doesn't exist
    }

    public float getAngle() {
        if (birdBody != null) {
            return birdBody.getAngle() * (180f / (float) Math.PI); // Convert radians to degrees
        }
        return 0; // Default angle if the body doesn't exist
    }


    public float getHealth() {
        return health;
    }
}
