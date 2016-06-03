package com.mbrsv.tq;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class BaseGame extends Game {

    Skin skin;

    public BaseGame() { skin = new Skin(); }

    public abstract void create();

    @Override
    public void dispose() { skin.dispose(); }
}
