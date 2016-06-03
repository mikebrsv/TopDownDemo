package com.mbrsv.tq;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class GameScreen extends BaseScreen {

    private PhysicsActor player;
    private BaseActor baseCoin;
    private ArrayList<BaseActor> coinList;
    private ArrayList<BaseActor> wallList;
    private ArrayList<BaseActor> removeList;

    private int tileSize = 32;
    private int tileCountWidth = 30;
    private int tileCountHeight = 30;

    final int mapWidth = tileSize * tileCountWidth;
    final int mapHeight = tileSize * tileCountHeight;

    private TiledMap tiledMap;
    private OrthographicCamera tiledCamera;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private int[] backgroundLayers = { 0, 1 };
    private int[] foregroundLayers = { 2 };

    public GameScreen(BaseGame g) { super(g); }

    @Override
    public void create() {
        //initialize player
        player = new PhysicsActor();

        //player animation
        float t = 0.15f;
        player.storeAnimation("down", GameUtils.parseSpriteSheet(
                "player.png", 3, 4, new int[] { 0, 1, 2 }, t, PlayMode.LOOP_PINGPONG));
        player.storeAnimation("left", GameUtils.parseSpriteSheet(
                "player.png", 3, 4, new int[] { 3, 4, 5 }, t, PlayMode.LOOP_PINGPONG));
        player.storeAnimation("right", GameUtils.parseSpriteSheet(
                "player.png", 3, 4, new int[] { 6, 7, 8 }, t, PlayMode.LOOP_PINGPONG));
        player.storeAnimation("up", GameUtils.parseSpriteSheet(
                "player.png", 3, 4, new int[] { 9, 10, 11 }, t, PlayMode.LOOP_PINGPONG));
        player.setSize(48, 48);

        player.setEllipseBoundary();
        mainStage.addActor(player);

        //initialize base coin; additional coins will be cloned from it later
        baseCoin = new BaseActor();
        baseCoin.setTexture(new Texture(Gdx.files.internal("coin.png")));
        baseCoin.setEllipseBoundary();
        coinList = new ArrayList<BaseActor>();

        wallList = new ArrayList<BaseActor>();

        removeList = new ArrayList<BaseActor>();

        //set up tile map, renderer, camera
        tiledMap = new TmxMapLoader().load("map01.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        tiledCamera = new OrthographicCamera();
        tiledCamera.setToOrtho(false, viewWidth, viewHeight);
        tiledCamera.update();

        //retrieve geometric data from the tilemap
        MapObjects objects = tiledMap.getLayers().get("ObjectData").getObjects();
        for (MapObject object : objects) {
            String name = object.getName();

            RectangleMapObject rectangleMapObject = (RectangleMapObject) object;
            Rectangle r = rectangleMapObject.getRectangle();

            switch (name) {
                case "player":
                    player.setPosition(r.x, r.y);
                    break;
                case "coin":
                    BaseActor coin = baseCoin.clone();
                    coin.setPosition(r.x, r.y);
                    mainStage.addActor(coin);
                    coinList.add(coin);
                    break;
                default:
                    System.err.println("Unknown tilemap object " + name);
            }
        }

        //retrieve data that represents solid walls
        objects = tiledMap.getLayers().get("PhysicsData").getObjects();
        for (MapObject object : objects) {
            RectangleMapObject rectangleMapObject = (RectangleMapObject) object;
            Rectangle r = rectangleMapObject.getRectangle();

            BaseActor solid = new BaseActor();
            solid.setPosition(r.x, r.y);
            solid.setSize(r.width, r.height);
            solid.setRectangleBoundary();
            wallList.add(solid);
        }
    }

    @Override
    public void update(float delta) {
        //player movement
        float playerSpeed = 500;
        player.setVelocityXY(0, 0);

        //input handing
        //set the corresponding animation to the player whenever an arrow key is pressed
        //also, the animation is either paused or started, depending on the speed of the player
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            player.setVelocityXY(-playerSpeed, 0);
            player.setActiveAnimation("left");
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            player.setVelocityXY(playerSpeed, 0);
            player.setActiveAnimation("right");
        }
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            player.setVelocityXY(0, playerSpeed);
            player.setActiveAnimation("up");
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            player.setVelocityXY(0, -playerSpeed);
            player.setActiveAnimation("down");
        }
        if (player.getSpeed() < 1) {
            player.pauseAnimation();
            player.setAnimationFrame(1);
        } else {
            player.startAnimation();
        }

        //collision detection
        for (BaseActor wall : wallList) {
            player.overlaps(wall, true);
        }

        for (BaseActor coin : coinList) {
            if (player.overlaps(coin, false)) {
                removeList.add(coin);
            }
        }

        //clean the removeList
        for (BaseActor ba : removeList) {
            ba.destroy();
        }

        //camera adjustment
        Camera mainCamera = mainStage.getCamera();

        //center camera on player
        mainCamera.position.x = player.getX() + player.getOriginX();
        mainCamera.position.y = player.getY() + player.getOriginY();

        //bound camera to layout
        mainCamera.position.x = MathUtils.clamp(
                mainCamera.position.x, viewWidth / 2, mapWidth - viewWidth / 2);
        mainCamera.position.y = MathUtils.clamp(
                mainCamera.position.y, viewHeight / 2, mapHeight - viewHeight / 2);
        mainCamera.update();

        //adjust tilemap camera to stay in sync with main camera
        tiledCamera.position.x = mainCamera.position.x;
        tiledCamera.position.y = mainCamera.position.y;
        tiledCamera.update();
        tiledMapRenderer.setView(tiledCamera);
    }

    @Override
    public void render(float delta) {
        uiStage.act(delta);
        if (!isPaused()) {
            mainStage.act(delta);
            update(delta);
        }
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        tiledMapRenderer.render(backgroundLayers);
        mainStage.draw();
        tiledMapRenderer.render(foregroundLayers);
        uiStage.draw();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.P) togglePaused();
        if (keycode == Keys.R) game.setScreen(new GameScreen(game));
        return false;
    }
}
