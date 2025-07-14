package com.Angrybird;

import com.Angrybird.BirdPackage.*;
import com.Angrybird.BlockPackage.*;
import com.Angrybird.PigPackage.*;
import com.Angrybird.StaticElements.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.Angrybird.Main.*;
import static com.badlogic.gdx.math.MathUtils.random;

public class LevelMenu implements Serializable {
    private transient Main main;
    private transient Texture background;
    private transient Texture randomLevelButton;
    private transient Texture lockedLevelTexture;
    private transient Texture lvl1Texture;
    private transient Texture lvl2Texture;
    private transient Texture lvl3Texture;

    private transient Texture unlockedLevelTexture;
    private transient Texture exitButton;
    private transient Viewport viewport;
    public transient CollisionManager collisionManager;

    private static final long serialVersionUID = 1L;

    // Status for each level (locked/unlocked)
    public static boolean[] levelStatuses = {true, false, false};
    private static List<Level> levels = new ArrayList<>();

    public LevelMenu(Main main, Viewport viewport) {
        this.main = main;
        this.viewport = viewport;

        background = new Texture("images/level_menu_page.png");
        randomLevelButton = new Texture("images/random_level.png");
        lockedLevelTexture = new Texture("images/locked_level.png");
        lvl1Texture = new Texture("images/lvl1.png");
        lvl2Texture = new Texture("images/lvl2.png");
        lvl3Texture = new Texture("images/lvl3.png");

        exitButton = new Texture("images/exit_button.png");
        collisionManager = new CollisionManager();
        setLevels();
    }

    private void setLevels() {
        for (int i = 0; i < 3; i++) {
            World world = new World(new Vector2(0, GRAVITY), true);
            levels.add(new Level(main, viewport, getLevelStructure(i, world), world));
        }
    }

    private Structure getLevelStructure(int levelIndex, World world) {
        switch (levelIndex) {
            case 0:
                return new Structure(world, new Vector2(10, 3), 3, -1, "Glass", "Wood", 1);
            case 1:
                return new Structure(world, new Vector2(10, 3), 3, -1, "Wood", "Glass", 3);
            case 2:
                return new Structure(world, new Vector2(10, 3), 1, 3, "Stone", "Glass", 3);
            default:
                return new Structure(world, new Vector2(10, 3), 2, 0, "Stone", "Glass", 3);
        }
    }

    public void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savedGame.dat"))) {
            oos.writeObject(levelStatuses);
            oos.writeObject(levels);
            writeBirdData(oos); // Save additional BirdData
            writeBlockData(oos);
            writePigData(oos);
            System.out.println("Game saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("savedGame.dat"))) {
            levelStatuses = (boolean[]) ois.readObject();
            List<Level> deserializedLevels = (List<Level>) ois.readObject();
            List<List<BirdData>> birdDataLists = (List<List<BirdData>>) ois.readObject();
            List<List<BlockData>> blockDataLists = (List<List<BlockData>>) ois.readObject();
            List<List<PigData>> pigDataLists = (List<List<PigData>>) ois.readObject();

            levels.clear();
            for (int i = 0; i < deserializedLevels.size(); i++) {
                Level deserializedLevel = deserializedLevels.get(i);
                reinitializeLevel(deserializedLevel, i, birdDataLists.get(i), blockDataLists.get(i), pigDataLists.get(i));
                levels.add(deserializedLevel);
            }

            System.out.println("Game loaded successfully!");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void reinitializeLevel(Level level, int index, List<BirdData> birdDataList, List<BlockData> blockDataList, List<PigData> pigDataList) {
        level.world = new World(new Vector2(0, GRAVITY), true);
        level.main = this.main;
        level.batch = new SpriteBatch();
        level.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        level.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        level.debugRenderer = new Box2DDebugRenderer();
//        level.pauseScreen = new PauseScreen(level.main, level.viewport);
        level.background = new Texture("images/world3.jpg");
        level.background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        level.slingshot = new Slingshot(level.viewport, level.world);
        level.thread = new ThreadLine(level.slingshot, level.viewport);
        level.structure =  new Structure(level.world, new Vector2(0, 3), -1, -1, "Glass", "Wood", 0);
        level.pauseScreen = new PauseScreen(main, viewport);
        level.createGround();

        // Restore birds
        level.birds = new ArrayList<>();
        for (BirdData birdData : birdDataList) {
            Bird bird = createBirdFromData(birdData, level.world);
            level.birds.add(bird);
        }

        level.structure.blocks = new ArrayList<>();
        for (BlockData blockData : blockDataList) {
            Block block = createBlockFromData(blockData, level.world);
            level.structure.blocks.add(block);
        }

        // Restore pigs
        level.structure.pigs = new ArrayList<>();
        for (PigData pigData : pigDataList) {
            Pig pig = createPigFromData(pigData, level.world);
            level.structure.pigs.add(pig);
        }

        level.launchHandler = new LaunchHandler(
            main, level.birds, level.structure,
            level.slingshot, level.thread,
            level.pauseScreen, level.viewport
        );
        startCollisionHandle(level.world);

    }

    private Block createBlockFromData(BlockData blockData, World world) {
        Block block = null;
        switch (blockData.type) {
            case "WoodBlock":
                block = new WoodBlock(world, new Vector2(blockData.posX, blockData.posY));
                break;
            case "GlassBlock":
                block = new GlassBlock(world, new Vector2(blockData.posX, blockData.posY));
                break;
            case "StoneBlock":
                block = new StoneBlock(world, new Vector2(blockData.posX, blockData.posY));
                break;
        }
        System.out.println("Block Position: " + blockData.posX + ", " + blockData.posY + " Angle" + blockData.angle);
        if (block != null) {
            // Ensure the angle is between 0 and 360 degrees
            blockData.angle = (blockData.angle % 360 + 360) % 360;

            block.setHealth(blockData.health);
            block.getBody().setTransform(block.getBody().getPosition(), (float) Math.toRadians(blockData.angle));
        }
        return block;
    }



    private Pig createPigFromData(PigData pigData, World world) {
        Pig pig = null;
        switch (pigData.type) {
            case "SmallPig":
                pig = new SmallPig(world, new Vector2(pigData.posX, pigData.posY));
                System.out.println("SmallPig restored");
                break;
            case "BigPig":
                pig = new BigPig(world, new Vector2(pigData.posX, pigData.posY));
                System.out.println("BigPig restored");
                break;
            case "ForemanPig":
                pig = new ForemanPig(world, new Vector2(pigData.posX, pigData.posY));
                System.out.println("ForemanPig restored");
                break;
        }

        if (pig != null) pig.setHealth(pigData.health);
        return pig;
    }



    private Bird createBirdFromData(BirdData birdData, World world) {
        Bird bird = null;
        switch (birdData.type) {
            case "Red":
                bird = new Red(world, new Vector2(birdData.posX, birdData.posY));
                System.out.println("Red restored");
                break;
            case "Chuck":
                bird = new Chuck(world, new Vector2(birdData.posX, birdData.posY));
                System.out.println("Chuck restored");
                break;
            case "Bomb":
                bird = new Bomb(world, new Vector2(birdData.posX, birdData.posY));
                System.out.println("Bomb restored");
                break;
        }
        if (bird != null) bird.setHealth(birdData.health);
        return bird;
    }

    private void writeBirdData(ObjectOutputStream oos) throws IOException {
        List<List<BirdData>> birdDataLists = new ArrayList<>();
        for (Level level : levels) {
            List<BirdData> birdDataList = new ArrayList<>();
            if (level.structure.getBirds() != null) {
                for (Bird bird : level.birds) {
                    if (bird.health<=0) continue;
                    birdDataList.add(new BirdData(
                        bird.getType(),
                        bird.getAngle(),
                        bird.getPosition().x,
                        bird.getPosition().y,
                        bird.getHealth()
                    ));
                }
            }
            birdDataLists.add(birdDataList);
        }
        oos.writeObject(birdDataLists);
    }

    private void writeBlockData(ObjectOutputStream oos) throws IOException {
        List<List<BlockData>> blockDataLists = new ArrayList<>();
        for (Level level : levels) {
            List<BlockData> blockDataList = new ArrayList<>();
            if (level.structure.getBlocks() != null) {
                for (Block block : level.structure.blocks) {
                    if (block.health<=0) continue;
                    blockDataList.add(new BlockData(
                        block.getType(),
                        block.getAngle(),
                        block.getPosition().x,
                        block.getPosition().y,
                        block.getHealth()
                    ));
                }
            }
            blockDataLists.add(blockDataList);
        }
        oos.writeObject(blockDataLists);
    }

    private void writePigData(ObjectOutputStream oos) throws IOException {
        List<List<PigData>> pigDataLists = new ArrayList<>();
        for (Level level : levels) {
            List<PigData> pigDataList = new ArrayList<>();
            if (level.structure.getPigs() != null) {
                for (Pig pig : level.structure.pigs) {
                    if (pig.health<=0) continue;
                    pigDataList.add(new PigData(
                        pig.getType(),
                        pig.getAngle(),
                        pig.getPosition().x,
                        pig.getPosition().y,
                        pig.getHealth()
                    ));
                }
            }
            pigDataLists.add(pigDataList);
        }
        oos.writeObject(pigDataLists);
    }


    public void startCollisionHandle(final World world) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                world.setContactListener(collisionManager); // Start the collision handling after the delay
            }
        }, 2f);

    }


    public void generateRandomLevel() {
        int floors1 = random.nextInt(5) - 1; // Random number between -1 and 3
        int TypeIndex1 = random.nextInt(3); // Random index for base type (0: Wood, 1: Glass, 2: Stone)
        int TypeIndex2 = random.nextInt(3); // Random index for roof type (0: Wood, 1: Glass, 2: Stone)
        String[] materials = {"Wood", "Glass", "Stone"};
        String Type1 = materials[TypeIndex1];
        int floors2 = random.nextInt(5) - 1; // Random number between -1 and 3
        String Type2 = materials[TypeIndex2];
        int birdnum = random.nextInt(4)+2;

        World world = new World(new Vector2(0, GRAVITY), true);
        startCollisionHandle(world);
        Structure structure = new Structure(world, new Vector2(10, 3), floors1, floors2, Type1, Type2, birdnum);  // Adjust structure position as needed
        main.startLevel(new Level(main, viewport, structure, world));

    }

    public void render(SpriteBatch batch) {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Draw Random Level button
        batch.draw(randomLevelButton, Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 + 50, 100, 50);

        // Draw unlocked and locked levels
        float levelYPos = Gdx.graphics.getHeight() / 2 - 50;
        float levelXPos = (Gdx.graphics.getWidth() / 4) * (1) - 50;

        batch.draw(lvl1Texture, (Gdx.graphics.getWidth() / 4)-50, levelYPos, 100, 100);
        if (levels.get(0).cleared) {
            batch.draw(lvl2Texture, (Gdx.graphics.getWidth() / 4)*2-50, levelYPos, 100, 100);
        } else {
            batch.draw(lockedLevelTexture, (Gdx.graphics.getWidth() / 4)*2-50, levelYPos, 100, 100);
        }

        if (levels.get(1).cleared) {
            batch.draw(lvl3Texture, (Gdx.graphics.getWidth() / 4)*3-50, levelYPos, 100, 100);
        } else {
            batch.draw(lockedLevelTexture, (Gdx.graphics.getWidth() / 4)*3-50, levelYPos, 100, 100);
        }


        // Draw Exit button
        batch.draw(exitButton, Gdx.graphics.getWidth() - 120, 20, 100, 50);
        batch.end();

        if (Gdx.input.justTouched()) {
            handleInput();
        }
    }

    private void handleInput() {
        float touchX = Gdx.input.getX();
        float touchY = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Random Level button
        if (touchX > Gdx.graphics.getWidth() / 2 - 50 && touchX < Gdx.graphics.getWidth() / 2 + 50 &&
            touchY > Gdx.graphics.getHeight() / 2 + 50 && touchY < Gdx.graphics.getHeight() / 2 + 100) {
            generateRandomLevel();
        }

        // Level buttons
        float levelYPos = Gdx.graphics.getHeight() / 2 - 50;
        for (int i = 0; i < 3; i++) {
            float levelXPos = (Gdx.graphics.getWidth() / 4) * (i + 1) - 50;
            if (touchX > levelXPos && touchX < levelXPos + 100 &&
                touchY > levelYPos && touchY < levelYPos + 100) {
                if (levelStatuses[i]) {
                    startCollisionHandle(levels.get(i).world);
                    main.startLevel(levels.get(i));
                } else {
                    System.out.println("Level " + (i + 1) + " is locked!");
                }
            }
        }

        // Exit button
        if (touchX > Gdx.graphics.getWidth() - 120 && touchX < Gdx.graphics.getWidth() - 20 &&
            touchY > 50 && touchY < 100) {
            main.exitToLandingPage();
        }

    }

    public Level resetLevel(Level level) {
        for (int i=0;i<3;i++) {
            Level cmp = levels.get(i);
            if (cmp.equals(level)) {
                World world = new World(new Vector2(0, GRAVITY), true);
                startCollisionHandle(world);
                levels.set(i, (new Level(main, viewport, getLevelStructure(i, world), world)));
                return levels.get(i);
            }
        }
        return null;
    }

    public void unlockNextLevel() {
        for (int i=0;i<3;i++) {
            if (levelStatuses[i]==false) {
                levelStatuses[i] = true;
                break;
            }
        }
    }

    public void startLatestLevel() {
        if (!levels.get(0).cleared) {
            main.startLevel(levels.get(0));
        } else if (!levels.get(1).cleared) {
            main.startLevel(levels.get(1));
        } else if (!levels.get(2).cleared) {
            main.startLevel(levels.get(2));
        } else {
            generateRandomLevel();
        }
    }



    public void dispose() {
        background.dispose();
        randomLevelButton.dispose();
        lockedLevelTexture.dispose();
        lvl1Texture.dispose();
        lvl2Texture.dispose();
        lvl3Texture.dispose();
        exitButton.dispose();
    }
}
