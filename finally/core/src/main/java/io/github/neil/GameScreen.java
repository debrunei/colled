package io.github.neil;

// ============================================================
// 🎮 GAME SCREEN — Your platformer lives here
// ============================================================
//
// Day 1 (Apr 27): Get the player moving, jumping, and landing
// Day 2 (Apr 29): Add sprite sheet animations
// Day 3 (May 1):  Add enemies and collision
// Day 4 (May 5):  Add coins and platforms
// Day 5 (May 7):  Add MenuScreen and GameOverScreen
// Day 6 (May 11): Add HUD, sound, polish
// Day 7 (May 13): Final polish and submit
//
// ============================================================

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    private final Main game;

    // ── Constants ──
    private static final float GRAVITY = -500f;
    private static final float JUMP_VELOCITY = 300f;
    private static final float MOVE_SPEED = 150f;
    private static final float GROUND_Y = 50f;

    // ── Rendering ──
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerSheet;


    Animation<TextureRegion> idleAnim, runAnim, jumpAnim;
    float stateTime = 0f;
    boolean facingRight = true;


    Texture enemySheet, coinSheet;
    Animation<TextureRegion> slimeAnim, coinAnim;


    ArrayList<float[]> enemies;

    ArrayList<Rectangle> coins;
    Rectangle playerBounds;
    int score = 0;

    // ── Player ──
    private float playerX = 100f;
    private float playerY = GROUND_Y;
    private float velocityY = 0f;
    private boolean onGround = true;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);

        // For Day 1, just load the full sprite sheet as a single texture.
        // On Day 2 you'll split it into animations.
        playerSheet = new Texture("player.png");

        TextureRegion[][] grid = TextureRegion.split(playerSheet, 64, 64);
        idleAnim = new Animation<>(0.2f, grid[0]);   // row 0
        runAnim  = new Animation<>(0.1f, grid[1]);   // row 1
        jumpAnim = new Animation<>(0.15f, grid[2]);  // row 2

        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);

        enemySheet = new Texture("enemy-slime.png");
        TextureRegion[][] eGrid = TextureRegion.split(enemySheet, 64, 64);
        slimeAnim = new Animation<>(0.15f, eGrid[0]);
        slimeAnim.setPlayMode(Animation.PlayMode.LOOP);

        coinSheet = new Texture("coin.png");
        TextureRegion[][] cGrid = TextureRegion.split(coinSheet, 32, 32);
        coinAnim = new Animation<>(0.08f, cGrid[0]);
        coinAnim.setPlayMode(Animation.PlayMode.LOOP);


        playerBounds = new Rectangle((int) playerX, (int) playerY, 64, 64);


        enemies = new ArrayList<>();
        enemies.add(new float[]{250, GROUND_Y, 80, 200, 350});
        enemies.add(new float[]{450, GROUND_Y, 60, 400, 550});


        coins = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            coins.add(new Rectangle(150 + i * 70, 200, 32, 32));
       }
    }


    // ══════════════════════════════════════════
    // DAY 3 — UPDATE METHODS
    // ══════════════════════════════════════════
    private void updateEnemies(float delta){
        for (float[] enemy : enemies) {
            enemy[0] += enemy[2] * delta;
            if (enemy[0] <= enemy[3]){
                enemy[0] = enemy[3];
                enemy[2] = -enemy[2];
            }
            if (enemy[0] >= enemy[4]){
                enemy[0] = enemy[4];
                enemy[2] = -enemy[2];
            }
        }
    }


    private void checkCollisions(){
        playerBounds.setLocation((int) playerX, (int) playerY);

        for (float[] enemy : enemies){
            Rectangle enemyRect = new Rectangle((int) enemy[0], (int) enemy[1], 64, 64);
            if (playerBounds.equals(enemyRect)){
                playerX = 100;
                playerY = GROUND_Y;
                velocityY = 0;
                System.out.println("Hit! Resetting player.");
            }
        }
        Iterator<Rectangle> it = coins.iterator();
        while (it.hasNext()){
            Rectangle coin = it.next();
            if (playerBounds.equals(coin)){
                it.remove();
                score++;
                System.out.println("Coin! Score: " + score);
            }
        }
    }
    @Override
    public void render(float delta) {

        // ── INPUT ──
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            playerX -= MOVE_SPEED * delta;
        }else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            playerX += MOVE_SPEED * delta;
        }else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            velocityY = JUMP_VELOCITY;
            onGround = false;
        }

        // ── PHYSICS ──
        velocityY += GRAVITY * delta;
        playerY += velocityY * delta;
        if (playerY <= GROUND_Y){
            playerY = GROUND_Y;
            velocityY = 0;
            onGround = true;
        }


        updateEnemies(delta);
        checkCollisions();

        // ── ANIMATION ──

        stateTime += delta;

        Animation<TextureRegion> currentAnim;
        if (!onGround){
            currentAnim = jumpAnim;
        }else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            currentAnim = runAnim;
        }else {
            currentAnim = idleAnim;
        }

        boolean looping = onGround;
        TextureRegion frame = currentAnim.getKeyFrame(stateTime, looping);

        if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if   (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        }

        // ── DRAW ──
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(playerSheet, playerX, playerY, 64, 64);


        TextureRegion slimeFrame = slimeAnim.getKeyFrame(stateTime, true);
        for (float[] enemy : enemies) {
            batch.draw(slimeFrame, enemy[0], enemy[1]);
        }


        TextureRegion coinFrame = coinAnim.getKeyFrame(stateTime, true);
        for (Rectangle coin : coins) {
            batch.draw(coinFrame, coin.x, coin.y);
        }



        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        playerSheet.dispose();
        enemySheet.dispose();
        coinSheet.dispose();
    }
}
