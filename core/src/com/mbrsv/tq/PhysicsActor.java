package com.mbrsv.tq;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PhysicsActor extends AnimatedActor {

    private Vector2 velocity;
    private Vector2 acceleration;
    private float maxSpeed;
    private float deceleration;
    private boolean autoAngle;

    public PhysicsActor() {
        velocity = new Vector2();
        acceleration = new Vector2();
        maxSpeed = 9999;
        deceleration = 0;
        autoAngle = false;
    }

    //VELOCITY METHODS
    public void setVelocityXY(float vx, float vy) { velocity.set(vx, vy); }
    public void addVelocityXY(float vx, float vy) { velocity.add(vx, vy); }
    public void setVelocityAS(float angleDeg, float speed) {
        velocity.x = speed * MathUtils.cosDeg(angleDeg);
        velocity.y = speed * MathUtils.sinDeg(angleDeg); }

    //SPEED METHODS
    public float getSpeed() { return velocity.len(); }
    public void setSpeed(float s) { velocity.setLength(s); }
    public void setMaxSpeed(float ms) { maxSpeed = ms; }

    //ANGLE METHODS
    public float getMotionAngle() {
        return MathUtils.atan2(velocity.y, velocity.x) * MathUtils.radiansToDegrees; }
    public void setAutoAngle(boolean b) { autoAngle = b; }

    //ACCELERATION METHODS
    public void setAccelerationXY(float ax, float ay) { acceleration.set(ax, ay); }
    public void addAccelerationXY(float ax, float ay) { acceleration.add(ax, ay); }
    public void setAccelerationAS(float angleDeg, float speed) {
        acceleration.x = speed * MathUtils.cosDeg(angleDeg);
        acceleration.y = speed * MathUtils.sinDeg(angleDeg); }
    public void addAccelerationAS(float angleDeg, float speed) { acceleration.add(
            speed * MathUtils.cosDeg(angleDeg),
            speed * MathUtils.sinDeg(angleDeg)); }
    public void accelerateForward(float speed) { setAccelerationAS(getRotation(), speed); }
    public void setDeceleration(float d) { deceleration = d; }

    @Override
    public void act(float delta) {
        super.act(delta);
        velocity.add(acceleration.x * delta, acceleration.y * delta); //apply acceleration
        //decrease velocity when not accelerating
        if (acceleration.len() < 0.01) {
            float decelerateAmount = deceleration * delta;
            if (getSpeed() < decelerateAmount) {
                setSpeed(0);
            } else {
                setSpeed(getSpeed() - decelerateAmount);
            }
        }
        //cap at max speed
        if (getSpeed() > maxSpeed) {
            setSpeed(maxSpeed);
        }
        //apply velocity
        moveBy(velocity.x * delta, velocity.y * delta);
        //rotate img when moving
        if (autoAngle && getSpeed() > 0.1) {
            setRotation(getMotionAngle());
        }
    }

    public void copy(PhysicsActor original) {
        super.copy(original);
        this.velocity = new Vector2(original.velocity);
        this.acceleration = new Vector2(original.acceleration);
        this.maxSpeed = original.maxSpeed;
        this.deceleration = original.deceleration;
        this.autoAngle = original.autoAngle;
    }

    public PhysicsActor clone() {
        PhysicsActor newbie = new PhysicsActor();
        newbie.copy(this);
        return newbie;
    }
}
