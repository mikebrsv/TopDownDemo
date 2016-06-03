package com.mbrsv.tq;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class AnimatedActor extends BaseActor {

    private float elapsedTime;
    private Animation activeAnimation;
    private String activeAnimationName;
    private HashMap<String, Animation> animationStorage;
    private boolean pauseAnimation;

    public AnimatedActor() {
        super();
        elapsedTime = 0;
        activeAnimation = null;
        activeAnimationName = null;
        animationStorage = new HashMap<String, Animation>();
        pauseAnimation = false;
    }

    public void storeAnimation(String name, Animation animation) {
        animationStorage.put(name, animation);
        if (activeAnimationName == null) {
            setActiveAnimation(name);
        }
    }

    public void storeAnimation(String name, Texture texture) {
        TextureRegion textureRegion = new TextureRegion(texture);
        TextureRegion[] frames = { textureRegion };
        Animation animation = new Animation(1.0f, frames);
        storeAnimation(name, animation);
    }

    public void setActiveAnimation(String name) {
        if (!animationStorage.containsKey(name)) {
            System.out.println("No animation: " + name);
            return;
        }
        if (name.equals(activeAnimationName)) {
            return; //already playing, no need to change
        }
        activeAnimationName = name;
        activeAnimation = animationStorage.get(name);
        elapsedTime = 0;
        //if width of height not set, then set them
        if (getWidth() == 0 || getHeight() == 0) {
            Texture texture = activeAnimation.getKeyFrame(0).getTexture();
            setWidth(texture.getWidth());
            setHeight(texture.getHeight());
        }
    }

    public String getActiveAnimationName() {
        return activeAnimationName;
    }

    public void pauseAnimation() {
        pauseAnimation = true;
    }

    public void startAnimation() {
        pauseAnimation = false;
    }

    public void setAnimationFrame(int n) {
        elapsedTime = n * activeAnimation.getFrameDuration();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!pauseAnimation) {
            elapsedTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        textureRegion.setRegion(activeAnimation.getKeyFrame(elapsedTime));
        super.draw(batch, parentAlpha);
    }

    public void copy(AnimatedActor original) {
        super.copy(original);
        this.elapsedTime = 0;
        this.animationStorage = original.animationStorage;
        this.activeAnimationName = new String(original.activeAnimationName);
        this.activeAnimation = this.animationStorage.get(this.activeAnimationName);

    }

    @Override
    public AnimatedActor clone() {
        AnimatedActor newbie = new AnimatedActor();
        newbie.copy(this);
        return newbie;
    }
}
