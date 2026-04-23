package io.github.Foundations;


import com.badlogic.gdx.Game;
import io.github.found1.GameplayScreen;

public class Main extends Game {

    @Override
    public void create(){
        setScreen(new GameplayScreen());
    }
}
