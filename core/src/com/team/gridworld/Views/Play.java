package com.team.gridworld.Views;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.alpha;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.touchable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.team.gridworld.Assets;
import com.team.gridworld.Constants;
import com.team.gridworld.GamePreferences;
import com.team.gridworld.StringHelper;
import com.team.gridworld.Controllers.AudioManager;
import com.team.gridworld.Controllers.MyInputProcessor;
import com.team.gridworld.Models.GridWorld;

public class Play extends AbstractGameScreen {

	private boolean				isDebug			= true;

	private OrthographicCamera	camera;
	private OrthographicCamera	guiCamera;
	private SpriteBatch			spriteBatch;
	private ShapeRenderer		shapeRenderer;
	private Table				mainTable;
	private Skin				skin;
	private String				searchType;
	private StringHelper		stringHelper;
	private float				e;

	private String				lastMoveString	= null;

	private String				eventString		= null;
	private float				eventTimeout	= 5;
	private float				eventTimer		= 0;

	private float				playerStepTimeout;
	private float				playerStepTimer	= 2;

	private Stage				stage;
	private GridWorld			world;
	private Vector2				touchVector		= new Vector2();
	private Vector3				touchVector3d	= new Vector3();

	private float				width, height;
	private BitmapFont			font;

	// options pop up
	private Window				winOptions, winControl, winGridSettings;
	private TextButton			btnWinOptSave, btnWinOptRun, btnWinOptCancel;
	private CheckBox			chkE, chkAlpha, chkMusic, chkShowFpsCounter,
								chkUseMonochromeShader, chkUseAccelerometer;
	private Slider				sldE, sldAlpha, sldSteps,
								sldPlayerStep, sldRows, sldCols;
	private InputMultiplexer	inputMultiplexer;
	private InputProcessor		gameInputProcessor, menuInputProcessor;
	private boolean				isMenuOpen;
	private boolean				isPaused;

	public Play(DirectedGame game) {
		super(game);
	}

	@Override
	public void render(float delta) {
		// Clear screen
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Update player
		// if player is stepping
		if (world.getStepsToGo() > 0 && isPaused == false) {
			// player time step
			playerStepTimer += delta;
			// if player timer greater than step timeout
			if (playerStepTimer > GamePreferences.instance.playerTimeStep) {
				// reset timer
				// Perform update
				// if player is exploring
				// if (world.getE() >= 0.1f) {
				// perform a step of eGreedy search
				world.eGreedySearch();
				
				if (GamePreferences.instance.playerTimeStep < delta) {
					int extraSteps = (int) (delta / GamePreferences.instance.playerTimeStep);
					extraSteps = extraSteps * extraSteps * extraSteps; // squared
					int extraStep = 0;
					while (extraStep < extraSteps) {
						extraStep += 1;
						world.eGreedySearch();
					}
				}
				// else player is exploiting
				// } else {
				// perform a step of greedy search
				// world.greedy();
				// }
				playerStepTimer = 0;
			}
		}

		// Set renderer to game camera
		shapeRenderer.setProjectionMatrix(camera.combined);
		spriteBatch.setProjectionMatrix(camera.combined);

		// Render world
		world.render(shapeRenderer, spriteBatch);

		// Close renderer
		shapeRenderer.end();

		// Render GUI
		renderUI();

		stage.act(delta);
		stage.draw();

	}

	public void renderUI() {

		// set renderer to GUI camera
		spriteBatch.setProjectionMatrix(guiCamera.combined);
		spriteBatch.begin();

		spriteBatch.setColor(Color.WHITE);

		int columnWidth = (int) (width / world.getStats().size());

		// show the player's stats
		int count = 0;
		if (world.getStats() != null) {
			for (String value : world.getStats()) {
				font.draw(spriteBatch, value, -(width * .5f) + 10 + (columnWidth * count),
						(height * .5f) - 5);
				count += 1;

			}
		}

		if (isMenuOpen && world.getStepsToGo() > 0) {
			sldSteps.setValue(world.getStepsToGo());
		}
		spriteBatch.end();
	}

	// init
	@Override
	public void show() {
		StringHelper.getInstance();
		StringHelper.init();

		isPaused = true;

		playerStepTimeout = GamePreferences.instance.playerTimeStep;
		// background = new Sprite(Assets.instance.);
		// float backgroundWidth = 2 * -Constants.VIEWPORT_WIDTH;
		// float backgroundHeight = 2 * -Constants.VIEWPORT_HEIGHT;
		// background.setBounds(-backgroundWidth / 2, -backgroundHeight / 2,
		// backgroundWidth, backgroundHeight);
		// background.setOrigin(backgroundWidth / 2 + 100,
		// backgroundHeight / 2 - 50);

		// create a new stage object to hold all of the other objects
		stage = new Stage();
		camera = new OrthographicCamera(Constants.VIEWPORT_WIDTH,
				Constants.VIEWPORT_HEIGHT);
		spriteBatch = new SpriteBatch();

		// create a new table the size of the window
		mainTable = new Table(Assets.instance.skin);
		mainTable.setFillParent(true);

		/* create some buttons */
		int padding = 20;

		// Run Button
		TextButton btnWinOptRun = new TextButton("Run", Assets.instance.skin, "small");
		mainTable.add(btnWinOptRun).padRight(padding);
		btnWinOptRun.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setStepsToGo(GamePreferences.instance.steps);
				isPaused = false;
				// saveSettings();
				AudioManager.instance.onSettingsUpdated();
				isPaused = false;
			}
		});
		// Pause Button
		TextButton btnWinOptPause = new TextButton("Pause", Assets.instance.skin, "small");
		mainTable.add(btnWinOptPause).padRight(padding);
		btnWinOptPause.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				isPaused = !isPaused;
			}
		});

		// Row of buttons
		// tbl.row();

		// Mode button
		TextButton btnGameMode = new TextButton("Mode", Assets.instance.skin, "small");
		btnGameMode.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.cycleMode();

				sldAlpha.setValue(world.getLearningRate());
				sldE.setValue(world.getE());
				sldSteps.setValue(world.getStep());
			}
		});
		mainTable.add(btnGameMode).padRight(padding );
		// Reset Button
		TextButton btnReset = new TextButton("Reset", Assets.instance.skin, "small");
		mainTable.add(btnReset).padRight(padding);
		btnReset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.init();
				isPaused = true;
			}
		});

		// Cliff World Button
		TextButton btnCliffWorld = new TextButton("Cliff World", Assets.instance.skin, "small");
		btnCliffWorld.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setWorldType(0);
				world.init();
				isPaused = true;
			}
		});
		mainTable.add(btnCliffWorld).padRight(padding );

		// Barrier World Button
		TextButton btnBarrierWorld = new TextButton("Barrier World", Assets.instance.skin, "small");
		btnBarrierWorld.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setWorldType(1);
				world.init();
				isPaused = true;
			}
		});
		mainTable.add(btnBarrierWorld).padRight(padding );

		// NarrowPass World Button
		TextButton btnNarrowPassWorld = new TextButton("NarrowPass World", Assets.instance.skin,
				"small");
		btnNarrowPassWorld.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setWorldType(2);
				world.init();
				isPaused = true;
			}
		});
		mainTable.add(btnNarrowPassWorld).padRight(padding );
		
		// NarrowPass World Button
		TextButton btnTrickWorld = new TextButton("Trick World", Assets.instance.skin,
				"small");
		btnTrickWorld.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setWorldType(3);
				world.init();
				isPaused = true;
			}
		});
		mainTable.add(btnTrickWorld).padRight(padding );

		// Pause Button
		TextButton btnGridSettings = new TextButton("Grid Settings", Assets.instance.skin, "small");
		btnGridSettings.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!winGridSettings.isVisible()) {
					winGridSettings.setVisible(true);
					showGridSettingsWindow(true, true);
				} else {
					showGridSettingsWindow(false, true);
					winGridSettings.setVisible(false);
				}
			}
		});
		mainTable.add(btnGridSettings).padRight(padding );

		// Menu Button
		TextButton tbMenu = new TextButton("Search Settings", Assets.instance.skin, "small");
		// use an anonymous inner class for then click event listener
		tbMenu.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!winOptions.isVisible()) {
//					loadSettings();
					winOptions.setVisible(true);
					showOptionsWindow(true, true);
				} else {
					showOptionsWindow(false, true);
					winOptions.setVisible(false);
				}

				event.handle();
			}
		});
		mainTable.add(tbMenu).padRight(padding );

		// Save Button
		btnWinOptSave = new TextButton("Save Settings", Assets.instance.skin, "small");
		mainTable.add(btnWinOptSave);
		btnWinOptSave.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				saveSettings();
			}
		});

		mainTable.padTop(25);
		mainTable.padLeft(25);
		mainTable.left();
		mainTable.top();

		stage.addActor(mainTable);

		Table layerOptionsWindow = buildOptionsWindowLayer();

		layerOptionsWindow.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				event.cancel();
			}
		});

		Table layerControlWindow = buildControlWindowLayer();
		layerControlWindow.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				event.cancel();
			}
		});

		Table layerGridSettingslWindow = buildWindowGridSettings();

		// assemble stage for menu screen
		stage.clear();
		stage.addActor(mainTable);
		stage.addActor(layerOptionsWindow);
		stage.addActor(layerControlWindow);
		stage.addActor(layerGridSettingslWindow);
		isMenuOpen = true;

		// Get a font from assets
		font = Assets.instance.fonts.defaultSmall;

		// Init GUI camera
		guiCamera = new OrthographicCamera(Constants.VIEWPORT_GUI_WIDTH,
				Constants.VIEWPORT_GUI_HEIGHT);

		// Get the window dimensions
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		

		// Init game camera with the window dimensions
		camera = new OrthographicCamera(width,
				height);

		// Get the UI skin
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

		// Init renderers
		shapeRenderer = new ShapeRenderer();
		spriteBatch = new SpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);

		// Init the Gridworld
		world = new GridWorld(this);
		world.init();

		inputMultiplexer = new InputMultiplexer();

		gameInputProcessor = new MyInputProcessor() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {

					case Keys.UP:
					case Keys.W:
						world.movePlayer("Up");
						break;
					case Keys.DOWN:
					case Keys.S:
						world.movePlayer("Down");
						break;
					case Keys.LEFT:
					case Keys.A:
						world.movePlayer("Left");
						break;
					case Keys.RIGHT:
					case Keys.D:
						world.movePlayer("Right");
						break;

					case Keys.M:
						world.cycleMode();
						break;

					case Keys.NUM_1:
						world.setStepsToGo(1000);
						break;

					case Keys.NUM_2:
						world.setStepsToGo(2000);
						break;

					case Keys.NUM_3:
						world.setStepsToGo(3000);
						break;

					case Keys.NUM_4:
						world.setStepsToGo(4000);
						break;

					case Keys.NUM_5:
						world.setStepsToGo(5000);
						break;

					case Keys.NUM_6:
						world.setStepsToGo(6000);
						break;

					case Keys.NUM_7:
						world.setStepsToGo(7000);
						break;

					case Keys.NUM_8:
						world.setStepsToGo(8000);
						break;
					case Keys.NUM_9:
						world.setStepsToGo(9000);
						break;
					case Keys.NUM_0:
						world.setStepsToGo(10000);
						break;

					case Keys.R:
						world.init();
						break;
						
					case Keys.P:
						world.displayResults();
						break;

				}
				return super.keyDown(keycode);
			}

			@Override
			public boolean keyUp(int keycode) {
				switch (keycode) {

				}

				return super.keyUp(keycode);
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer,
					int button) {

				// if (isMenuOpen) {
				// return super.touchDown(screenX, screenY, pointer, button);
				// }

				touchVector3d.set(screenX, screenY, 0);
				camera.unproject(touchVector3d);
				touchVector.set(touchVector3d.x, touchVector3d.y);
				// when the user clicks, find the square
				world.touchDown(touchVector3d.x, touchVector3d.y);

				return super.touchDown(screenX, screenY, pointer, button);
			}

		};

		inputMultiplexer.addProcessor(stage);

		inputMultiplexer.addProcessor(gameInputProcessor);
	}

	private Table buildOptionsWindowLayer() {
		winOptions = new Window("Search Settings", Assets.instance.skin);
		// + Audio Settings: Sound/Music CheckBox and Volume Slider
		winOptions.add(buildOptWinSettings()).row();
		// + Character Assets.instance.skin: Selection Box (White, Gray, Brown)
		winOptions.add(buildOptWinInputSelection()).row();
		// + Debug: Show FPS Counter
		if (isDebug) {
			winOptions.add(buildOptWinDebug()).row();
		}
		// + Separator and Buttons (Save, Cancel)

		// Hide Button
		// TextButton btnWinOptCancel = new TextButton("Hide", Assets.instance.skin, "small");
		// winOptions.add(btnWinOptCancel).right();
		// btnWinOptCancel.addListener(new ChangeListener() {
		// @Override
		// public void changed(ChangeEvent event, Actor actor) {
		// onCancelClicked();
		// }
		// });

		// Make options window slightly transparent
		// winOptions.setColor(1, 1, 1, 1f);

		// Hide options window by default
		showOptionsWindow(false, true);
		// Let TableLayout recalculate widget sizes and positions
		winOptions.pack();
		// Move options window to bottom right corner
		winOptions.setPosition(
				Constants.VIEWPORT_GUI_WIDTH,
				Constants.VIEWPORT_GUI_HEIGHT - winOptions.getHeight() - 50);
		winOptions.setMovable(true);
		winOptions.setVisible(false);

		winOptions.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});
		return winOptions;
	}

	private Table buildWindowGridSettings() {
		winGridSettings = new Window("Grid Settings", Assets.instance.skin);

		int padding = 20;
		int slideWidth = 300;
		Table tbl = new Table();

		tbl.row();
		tbl.columnDefaults(0).padRight(0);
		tbl.columnDefaults(0).padLeft(0);
		tbl.columnDefaults(0).padTop(padding);
		tbl.columnDefaults(1).padRight(padding);
		tbl.columnDefaults(1).padLeft(0);
		tbl.columnDefaults(1).padTop(padding + 2);
		tbl.columnDefaults(2).padRight(0);
		tbl.columnDefaults(2).padLeft(0);
		tbl.columnDefaults(2).padTop(padding + 2);
		tbl.columnDefaults(3).padRight(0);
		tbl.columnDefaults(3).padLeft(0);
		tbl.columnDefaults(3).padTop(padding);
		tbl.columnDefaults(4).padRight(0);
		tbl.columnDefaults(4).padLeft(0);
		tbl.columnDefaults(4).padTop(padding + 2);

		tbl.add(new Label("Rows : ", Assets.instance.skin));
		Label lblRowCount = new Label("" + GamePreferences.instance.rows, Assets.instance.skin);
		tbl.add(lblRowCount);
		tbl.add(new Label("2", Assets.instance.skin));
		sldRows = new Slider(2, 25, 1, false, Assets.instance.skin);
		sldRows.setValue(GamePreferences.instance.rows);
		sldRows.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GamePreferences.instance.rows = (int) sldRows.getValue();
				isPaused = true;
				world.init();
				((Label) sldRows.getUserObject()).setText("" + GamePreferences.instance.rows);
			}
		});
		sldRows.setUserObject(lblRowCount);
		tbl.add(sldRows).width(slideWidth);
		tbl.add(new Label("25", Assets.instance.skin));
		tbl.row();

		tbl.add(new Label("Cols : ", Assets.instance.skin));
		Label lblColCount = new Label("" + GamePreferences.instance.cols, Assets.instance.skin);
		tbl.add(lblColCount);
		tbl.add(new Label("2", Assets.instance.skin));
		sldCols = new Slider(2, 25, 1, false, Assets.instance.skin);
		sldCols.setValue(GamePreferences.instance.cols);
		sldCols.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GamePreferences.instance.cols = (int) sldCols.getValue();
				isPaused = true;
				world.init();
				((Label) sldCols.getUserObject()).setText("" + GamePreferences.instance.cols);
			}
		});
		sldCols.setUserObject(lblColCount);
		tbl.add(sldCols).width(slideWidth);
		tbl.add(new Label("25", Assets.instance.skin));
		tbl.row();
		tbl.pack();
		winGridSettings.add(tbl);

		// Hide options window by default
		showGridSettingsWindow(false, true);
		// Let TableLayout recalculate widget sizes and positions
		winGridSettings.pack();
		// Move options window to bottom right corner
		winGridSettings.setPosition(
				Constants.VIEWPORT_GUI_WIDTH * .25f,
				Constants.VIEWPORT_GUI_HEIGHT - winGridSettings.getHeight() - 60);
		winGridSettings.setMovable(true);
		winGridSettings.setVisible(false);

		winGridSettings.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});
		return winGridSettings;
	}

	private Table buildControlWindowLayer() {
		winControl = new Window("Control", Assets.instance.skin);

		Table controlTable = new Table();
		int padding = 20;
		Table tbl = new Table();

		// Row of buttons
		controlTable.row();

		// + Up Button with event handler
		TextButton btnUp = new TextButton("Up", Assets.instance.skin, "small");
		controlTable.add(btnUp).padRight(padding);
		btnUp.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.movePlayer("Up");
				event.handle();
			}
		});
		// + Down Button with event handler
		TextButton btnDown = new TextButton("Down", Assets.instance.skin, "small");
		controlTable.add(btnDown).padRight(padding);
		btnDown.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.movePlayer("Down");
				event.handle();
			}
		});

		// + Left Button with event handler
		TextButton btnLeft = new TextButton("Left", Assets.instance.skin, "small");
		controlTable.add(btnLeft).padRight(padding);
		btnLeft.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.movePlayer("Left");
				event.handle();
			}
		});

		// + Right Button with event handler
		TextButton btnRight = new TextButton("Right", Assets.instance.skin, "small");
		controlTable.add(btnRight);
		btnRight.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				world.movePlayer("Right");
				event.handle();
			}
		});

		tbl.add(controlTable).row();
		winControl.add(tbl);

		// Hide options window by default
		showControlWindow(true, true);
		// Let TableLayout recalculate widget sizes and positions
		winControl.pack();
		// Move options window to bottom right corner
		winControl.setPosition(
				Constants.VIEWPORT_GUI_WIDTH, -Constants.VIEWPORT_GUI_HEIGHT * .8f);
		winControl.setMovable(true);

		winControl.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});
		return winControl;
	}

	private Table buildOptWinSettings() {

		int padding = 10;

		int slideWidth = 1000;

		Table tbl = new Table();
		tbl.row();
		tbl.columnDefaults(0).padRight(padding);
		tbl.columnDefaults(0).padLeft(padding);
		tbl.columnDefaults(0).padTop(padding);
		tbl.columnDefaults(1).padRight(padding);
		tbl.columnDefaults(1).padLeft(padding);
		tbl.columnDefaults(1).padTop(padding + 2);
		tbl.columnDefaults(2).padRight(padding);
		tbl.columnDefaults(2).padLeft(padding);
		tbl.columnDefaults(2).padTop(padding);
		tbl.columnDefaults(3).padRight(padding);
		tbl.columnDefaults(3).padLeft(padding);
		tbl.columnDefaults(3).padTop(padding + 2);
		// tbl.columnDefaults(1).padRight(10);

		tbl.add(new Label("E: ", Assets.instance.skin));
		tbl.add(new Label("0.0", Assets.instance.skin));
		sldE = new Slider(0, 1.0f, 0.1f, false, Assets.instance.skin);
		sldE.setValue(GamePreferences.instance.e);
		sldE.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setE(sldE.getValue());
				GamePreferences.instance.e = sldE.getValue();
			}
		});
		tbl.add(sldE).width(slideWidth);
		tbl.add(new Label("1.0", Assets.instance.skin));
		tbl.row();

		tbl.add(new Label("Alpha: ", Assets.instance.skin));
		tbl.add(new Label("0.01", Assets.instance.skin));
		sldAlpha = new Slider(0.01f, 0.5f, 0.01f, false,
				Assets.instance.skin);
		sldAlpha.setValue(Math.round(GamePreferences.instance.alpha * 100f) / 100f);
		sldAlpha.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				float newAlpha = Math.round(sldAlpha.getValue() * 100f) / 100f;
				world.setLearningRate(newAlpha);
				GamePreferences.instance.alpha = newAlpha;
			}
		});
		tbl.add(sldAlpha).width(slideWidth);
		tbl.add(new Label("0.5", Assets.instance.skin));

		tbl.row();
		tbl.add(new Label("Steps: ", Assets.instance.skin));
		tbl.add(new Label("0", Assets.instance.skin));
		sldSteps = new Slider(0, 30000, 1, false,
				Assets.instance.skin);
		sldSteps.setValue(GamePreferences.instance.steps);
		sldSteps.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				world.setStepsToGo((int) sldSteps.getValue());
			}
		});
		tbl.add(sldSteps).width(slideWidth);
		tbl.add(new Label("30000", Assets.instance.skin));

		tbl.row();
		tbl.add(new Label("Step Time: ", Assets.instance.skin));
		tbl.add(new Label("0.0001", Assets.instance.skin));
		sldPlayerStep = new Slider(0.0001f, 1.0f, 0.0001f, false,
				Assets.instance.skin);
		sldPlayerStep.setValue(GamePreferences.instance.playerTimeStep);
		sldPlayerStep.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GamePreferences.instance.playerTimeStep = sldPlayerStep.getValue();
			}
		});
		tbl.add(sldPlayerStep).width(slideWidth);
		tbl.add(new Label("1.0", Assets.instance.skin));

		tbl.row();

		return tbl;
	}

	private Table buildOptWinInputSelection() {
		int padding = 20;
		Table table = new Table();
		return table;
	}

	// debug options
	private Table buildOptWinDebug() {
		int padding = 20;
		Table tbl = new Table();
		return tbl;
	}

	private void loadSettings() {
		GamePreferences prefs = GamePreferences.instance;
		prefs.load();
	}

	private void saveSettings() {
		GamePreferences prefs = GamePreferences.instance;
		try {
			prefs.e = Math.round(sldE.getValue() * 100f) / 100f;
			prefs.alpha = Math.round(sldAlpha.getValue() * 100f) / 100f;
			prefs.steps = (int) sldSteps.getValue();

			world.setE(prefs.e);
			world.setLearningRate(prefs.alpha);
			world.setStepsToGo(prefs.steps);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		prefs.save();
	}

	private void showOptionsWindow(boolean visible, boolean animated) {
		float alphaTo = visible ? 1.0f : 0.0f;
		float duration = animated ? 0.25f : 0.0f;
		Touchable touchEnabled = visible ? Touchable.enabled
				: Touchable.disabled;
		winOptions.addAction(sequence(touchable(touchEnabled),
				alpha(alphaTo, duration)));

	}

	private void showControlWindow(boolean visible, boolean animated) {
		float alphaTo = visible ? 1.0f : 0.0f;
		float duration = animated ? 0.25f : 0.0f;
		Touchable touchEnabled = visible ? Touchable.enabled
				: Touchable.disabled;
		winControl.addAction(sequence(touchable(touchEnabled),
				alpha(alphaTo, duration)));

	}

	private void showGridSettingsWindow(boolean visible, boolean animated) {
		float alphaTo = visible ? 1.0f : 0.0f;
		float duration = animated ? 0.25f : 0.0f;
		Touchable touchEnabled = visible ? Touchable.enabled
				: Touchable.disabled;
		winGridSettings.addAction(sequence(touchable(touchEnabled),
				alpha(alphaTo, duration)));

	}

	@Override
	public void dispose() {
		stage.dispose();
		spriteBatch.dispose();
	}

	@Override
	public void resize(int width, int height) {
		// set the view to the new window width and height
		// stage.setViewport(width, height, true);
		stage.getViewport().update(width, height, true);
		// invalidate the table hierarchy for it to reposition elements
		mainTable.invalidateHierarchy();
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	public void newEvent(String newEventString) {
		// System.out.println("newEvent(" + newEventString + ")");
		eventString = newEventString;
		eventTimer = 0;
	}

	@Override
	public InputProcessor getInputProcessor() {

		return inputMultiplexer;
	}
}
