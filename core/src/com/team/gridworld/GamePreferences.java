package com.team.gridworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

public class GamePreferences {
	public static final String			TAG			= GamePreferences.class.getName();
	public static final GamePreferences	instance	= new GamePreferences();
	public boolean						sound;
	public boolean						music;
	public float						volSound;
	public float						volMusic;
	public boolean						showFpsCounter;
	public boolean						useAccelerometer;
	public float						e;
	public float						alpha;
	public int							steps;
	public int rows;
	public int cols;
	

	public float						playerTimeStep;
	public boolean						useMonochromeShader;

	private Preferences					prefs;

	// singleton: prevent instantiation from other classes
	private GamePreferences() {
		prefs = Gdx.app.getPreferences(Constants.PREFERENCES);
	}

	public void init(float e, float alpha) {

		prefs.putFloat("E", e);

		prefs.putFloat("alpha", alpha);

	}

	public void load() {
		rows = prefs.getInteger("rows", 8);
		cols = prefs.getInteger("cols", 12);
		e = prefs.getFloat("e", 0.3f);
		alpha = prefs.getFloat("alpha", 0.01f);
		steps = prefs.getInteger("steps", 10000);
		playerTimeStep = prefs.getFloat("playerTimeStep", 0.01f);
		sound = prefs.getBoolean("sound", true);
		music = prefs.getBoolean("music", true);
		useAccelerometer = prefs.getBoolean("useAccelerometer", true);
		volSound = MathUtils
				.clamp(prefs.getFloat("volSound", 0.5f), 0.0f, 1.0f);
		volMusic = MathUtils
				.clamp(prefs.getFloat("volMusic", 0.5f), 0.0f, 1.0f);
		showFpsCounter = prefs.getBoolean("showFpsCounter", false);
		useMonochromeShader = prefs.getBoolean("useMonochromeShader", false);
	}

	public void save() {
		prefs.putInteger("rows", rows);
		prefs.putInteger("cols", cols);
		
		prefs.putFloat("e", e);
		prefs.putFloat("alpha", alpha);
		prefs.putInteger("steps", steps);
		
		prefs.putFloat("playerTimeStep", playerTimeStep);
		
		
		prefs.putBoolean("sound", sound);
		prefs.putBoolean("music", music);
		prefs.putFloat("volSound", volSound);
		prefs.putFloat("volMusic", volMusic);
		prefs.putBoolean("showFpsCounter", showFpsCounter);
		prefs.putBoolean("useAccelerometer", useAccelerometer);
		prefs.putBoolean("useMonochromeShader", useMonochromeShader);
		prefs.flush();
	}
}