package com.team.gridworld;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.team.gridworld.Controllers.AudioManager;
import com.team.gridworld.Views.DirectedGame;
import com.team.gridworld.Views.Play;
import com.team.gridworld.Views.ScreenTransition;
import com.team.gridworld.Views.ScreenTransitionFade;

public class GridWorld extends DirectedGame {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		Assets.instance.init(new AssetManager());
		GamePreferences.instance.load();
		// Load assets
//		AudioManager.instance.play(Assets.instance.music.intro);
		ScreenTransition transition = 
				ScreenTransitionFade.init(0);

		setScreen(new Play(this), transition);
	}

}
