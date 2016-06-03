package com.mbrsv.tq;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.ArrayList;

public class BaseActor extends Group {

    public TextureRegion textureRegion;
    public Polygon boundingPolygon;
    public ArrayList<? extends BaseActor> parentList;

    public BaseActor() {
        super();
        textureRegion = new TextureRegion();
        boundingPolygon = null;
        parentList = null;
    }

    public void setParentList(ArrayList<? extends BaseActor> pl) {
        parentList = pl;
    }

    public void destroy() {
        remove();
        if (parentList != null) {
            parentList.remove(this);
        }
    }

    public void setOriginCenter() {
        if (getWidth() == 0) {
            System.err.println("Error: actor size not set");
        }
        setOrigin(getWidth() / 2, getHeight() / 2);
    }

    public void moveToOrigin(BaseActor target) {
        this.setPosition(
                target.getX() + target.getOriginX() - this.getOriginX(),
                target.getY() + target.getOriginY() - this.getOriginY());
    }

    public void setTexture(Texture t) {
        int w = t.getWidth();
        int h = t.getHeight();
        setWidth(w);
        setHeight(h);
        textureRegion.setRegion(t);
    }

    public void setRectangleBoundary() {
        float w = getWidth();
        float h = getHeight();
        float[] vertices = { 0,0, w,0, w,h, 0,h };
        boundingPolygon = new Polygon(vertices);
        boundingPolygon.setOrigin(getOriginX(), getOriginY());
    }

    public void setEllipseBoundary() {
        int n = 12; //number of vertices
        float w = getWidth();
        float h = getHeight();
        float[] vertices = new float[2 * n];
        for (int i = 0; i < n; i++) {
            float t = i * 6.28f / n;
            vertices[2 * i] = w / 2 * MathUtils.cos(t) + w / 2;
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(t) + h / 2;
        }
        boundingPolygon = new Polygon(vertices);
        boundingPolygon.setOrigin(getOriginX(), getOriginY());
    }

    public Polygon getBoundingPolygon() {
        boundingPolygon.setPosition(getX(), getY());
        boundingPolygon.setRotation(getRotation());
        return boundingPolygon;
    }

    public boolean overlaps(BaseActor other, boolean resolve) {
        Polygon polygon1 = this.getBoundingPolygon();
        Polygon polygon2 = other.getBoundingPolygon();
        if (!polygon1.getBoundingRectangle().overlaps(polygon2.getBoundingRectangle())) {
            return false;
        }
        Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();
        boolean polyOverlap = Intersector.overlapConvexPolygons(polygon1, polygon2, mtv);
        if (polyOverlap && resolve) {
            this.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        }
        float significant = 0.5f;
        return (polyOverlap && (mtv.depth > significant));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);
        if (isVisible()) {
            batch.draw(textureRegion, getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }
        super.draw(batch, parentAlpha);
    }

    public void copy(BaseActor original) {
        if (original.textureRegion.getTexture() != null) {
            this.textureRegion = new TextureRegion(original.textureRegion);
        }
        if (original.boundingPolygon != null) {
            this.boundingPolygon = new Polygon(original.boundingPolygon.getVertices());
            this.boundingPolygon.setOrigin(original.getOriginX(), original.getOriginY());
        }
        this.setPosition(original.getX(), original.getY());
        this.setOriginX(original.getOriginX());
        this.setOriginY(original.getOriginY());
        this.setWidth(original.getWidth());
        this.setHeight(original.getHeight());
        this.setColor(original.getColor());
        this.setVisible(original.isVisible());
    }

    public BaseActor clone() {
        BaseActor newbie = new BaseActor();
        newbie.copy(this);
        return newbie;
    }
}
