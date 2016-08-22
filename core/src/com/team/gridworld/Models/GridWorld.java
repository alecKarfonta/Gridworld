package com.team.gridworld.Models;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.team.gridworld.Assets;
import com.team.gridworld.GamePreferences;
import com.team.gridworld.Views.Play;

public class GridWorld {

	private Play										play;
	private Grid										grid;
	private int											rows;
	private int											cols;
	private Random										randomGenerator;

	private int											playerCol;
	private int											playerRow;
	private LinkedList<Integer>							previousMoves;
	private int											previousMoveCount	= 30;
	private int											playerSize			= 20;
	private int											gridWidth			= Gdx.graphics
																					.getWidth() - 10;
	private int											gridHeight			= Gdx.graphics
																					.getHeight() - 100;
	private int											worldType;
	private String[]									worldTypes;
	private ArrayList<ArrayList<ArrayList<Integer>>>	worldPaths;

	public boolean										isDebug				= false;
	private int											stepCount;
	private float										e;
	private float										stepE;
	private int											step;
	private int											stepsToGo;
	private int											mode;
	private String[]									modes;
	private int											iterations;

	float												learningRate;
	private int											bestPathLength;

	public GridWorld(Play play) {
		this.play = play;

		iterations = 0;
		mode = 0;
		modes = new String[] { "Constant E", "Decaying E", "Truncated Sine E", "Greedy" };
		worldType = 0;
		worldTypes = new String[] { "Cliff", "Barrier", "Narrow Pass" };
		worldPaths = new ArrayList<ArrayList<ArrayList<Integer>>>();

		for (int modeIndex = 0; modeIndex < modes.length; modeIndex++) {
			worldPaths.add(new ArrayList<ArrayList<Integer>>());
			for (int worldTypeIndex = 0; worldTypeIndex < worldTypes.length; worldTypeIndex++) {
				worldPaths.get(modeIndex).add(new ArrayList<Integer>());
			}
		}
		init();
	}

	public void init() {

		// init previous move counts
		previousMoves = new LinkedList<Integer>();

		randomGenerator = new Random(System.currentTimeMillis());

		switch (mode) {
			case 0:
				e = GamePreferences.instance.e;
				break;
			case 1:
			case 2:
				e = GamePreferences.instance.e;
				break;
			case 3:
				e = 0.0f;
				break;
		}

		step = 0;
		rows = GamePreferences.instance.rows;
		cols = GamePreferences.instance.cols;
		learningRate = GamePreferences.instance.alpha;
		stepsToGo = GamePreferences.instance.steps;

		grid = new Grid(rows, cols, gridWidth, gridHeight);
		playerInit();

		switch (worldType) {
			case 0:
				initCliffWorld();
				break;
			case 1:
				initBarrierWorld();
				break;
			case 2:
				initNarrowPassWorld();
				break;
			case 3:
				initTrickWorld();
				break;

		}
	}

	public void playerInit() {
		playerCol = 0;
		playerRow = 0;
	}

	public void initCliffWorld() {

		for (int col = 1; col < cols - 1; col++) {
			grid.getTile(col, 0).tap();
			grid.getTile(col, 0).tap();
		}

		grid.getTile(cols - 1, 0).tap();
	}

	public void initNarrowPassWorld() {

		if (rows - 1 > 0) {
			for (int col = 1; col < cols - 1; col++) {
				grid.getTile(col, 1).tap();
				grid.getTile(col, 1).tap();
				grid.getTile(col, 1).tap();
			}
		}

		if (rows - 1 > 3) {
			for (int col = 1; col < cols - 1; col++) {
				grid.getTile(col, 3).tap();
				grid.getTile(col, 3).tap();
				grid.getTile(col, 3).tap();
			}
		}

		if (rows - 1 > 5) {

			for (int col = 1; col < cols - 1; col++) {
				grid.getTile(col, 5).tap();
				grid.getTile(col, 5).tap();
				grid.getTile(col, 5).tap();
			}
		}

		grid.getTile(cols - 2, 0).tap();
		grid.getTile(cols - 2, 0).tap();
		grid.getTile(cols - 1, 0).tap();
	}

	public void initBarrierWorld() {

		for (int row = 1; row < rows - 4; row++) {
			grid.getTile(1, row).tap();
			grid.getTile(1, row).tap();
		}

		if (cols - 1 > 3) {
			for (int row = 1; row < rows - 3; row++) {
				grid.getTile(3, row).tap();
				grid.getTile(3, row).tap();
				grid.getTile(3, row).tap();
			}
		}

		if (cols - 1 > 5) {
			for (int row = 0; row < rows - 2; row++) {
				if (row == 4) {
					continue;
				}
				grid.getTile(5, row).tap();
				grid.getTile(5, row).tap();
			}
		}

		grid.getTile(cols - 1, 1).tap();
	}

	public void initTrickWorld() {
		int midWidth = (int) (cols * .25f);
		int midHeight = (int) (rows * .75f);

		grid.getTile(midWidth, 0).tap();
		if (midWidth > 0) {
			grid.getTile(midWidth - 1, 0).tap();
			grid.getTile(midWidth - 1, 0).tap();
		}


		grid.getTile(0, midHeight).tap();
		grid.getTile(0, midHeight).setExitValue(50);
		int col = 1;
		
		while (col < cols) {
			grid.getTile(col, midHeight).tap();
			grid.getTile(col, midHeight).setExitValue(50);
			col += 1;
		}
	}

	public void getLengthOfBestPath() {
		// reset player position
		playerInit();

		int length = 0;
		boolean isGoal = false;
		while (!isGoal && length < rows * cols) {
			length += 1;
			int moveValue = greedy();

			if (moveValue == 100) {
				isGoal = true;
			}
		}
		bestPathLength = length;

		worldPaths.get(mode).get(worldType).add(bestPathLength);

		// print and rest world
		// System.out.println("iterations     \t\t : \t\t " + iterations + 1);
		// System.out.println("Mode           \t\t : \t\t " + modes[mode]);
		// System.out.println("worldType     \t\t : \t\t " + worldTypes[worldType] + 1);
		// System.out.println("Wolrd Size    \t\t : \t\t [" + GamePreferences.instance.rows + ","
		// + GamePreferences.instance.cols + "]");
		// System.out.println("bestPathLength \t\t : \t\t " + bestPathLength);
		// System.out
		// .println("_________________________________________________________________________________________");

		if (iterations < 40000) {
			init();

			stepsToGo = GamePreferences.instance.steps;
			iterations += 1;
			// Every ten games switch mode
			if (iterations % 10 == 0) {
				cycleMode();
			}

			if (iterations % (10 * modes.length) == 0) {
				worldType = (worldType + 1) % worldTypes.length;
			}
		}

	}

	public void eGreedySearch() {

		switch (modes[mode]) {
			case "Constant E":
				// Constant E search
				stepE = e;
				break;
			case "Decaying E":
				// Decaying E
				e *= 0.9999f;
				stepE = Math.max(0.01f, e);
				break;
			case "Truncated Sine E":
				// Sine search
				// get a sine value from the current step
				stepE = (float) Math.sin((float) step * 0.01f);
				// truncate sine values below 0
				stepE = Math.max(0, stepE);
				// instead of going from 0-1 go from 0-e
				stepE = (float) e * stepE;
				break;
			case "Greedy":
				stepE = 0;
				break;
		}

		// get a next move from eGreedy()
		int move = eGreedy(stepE);

		// Move player
		switch (move) {
			case 0:
				movePlayer("Up");
				break;
			case 1:
				movePlayer("Right");
				break;
			case 2:
				movePlayer("Down");
				break;
			case 3:
				movePlayer("Left");
				break;
		}

		step += 1;

		stepsToGo -= 1;

		if (stepsToGo == 0) {
			getLengthOfBestPath();
		}

	}

	public int greedy() {
		if (isDebug) {
			System.out.println("greedy()");
		}

		step += 1;

		stepsToGo -= 1;

		if (stepsToGo == 0) {
			getLengthOfBestPath();
		}
		Tile tile = grid.getTile(playerCol, playerRow);
		int move;

		ArrayList<Integer> bestMoves = new ArrayList<Integer>();

		if (isDebug) {
			if (tile.getIsMovable()) {
				System.out.println("greedy() : canMove = " + tile.canMove[0] + " "
						+ tile.canMove[1]
						+ " " + tile.canMove[2] + " " + tile.canMove[3] + " ");

				System.out.println("greedy() : QValues = " + tile.QValues[0] + " "
						+ tile.QValues[1]
						+ " " + tile.QValues[2] + " " + tile.QValues[3] + " ");
			}
		}

		// get the value of each move for the player
		float maxValue = -Float.MAX_VALUE;

		int maxIndex = -1;
		// get the the tile
		for (int index = 0; index < tile.QValues.length; index++) {
			if (!tile.canMove[index]) {
				continue;
			}
			float qValue = tile.QValues[index];

			if (isDebug) {
				System.out.println("greedy() : maxValue = " + maxValue);
				System.out.println("greedy() : QValues[" + index + "] = " + qValue);
				System.out.println("greedy() : qValue > maxValue = " + (qValue > maxValue));
			}
			if (qValue > maxValue) {
				maxValue = qValue;
				maxIndex = index;
				bestMoves = new ArrayList<Integer>();
				bestMoves.add(index);
			} else if (qValue == maxValue) {
				bestMoves.add(index);
			}
		}

		if (bestMoves.size() > 1) {
			// return first move
			move = bestMoves.get(bestMoves.get(randomGenerator.nextInt(bestMoves.size() - 1)));

		} else {
			move = maxIndex;
		}

		if (isDebug) {
			System.out.println("greedy() : move = " + move);
		}

		switch (move) {
			case 0:
				if (isDebug) {
					System.out.println("greedy() : up");
				}
				return movePlayer("Up");
			case 1:
				if (isDebug) {
					System.out.println("greedy() : right");
				}
				return movePlayer("Right");
			case 2:
				if (isDebug) {
					System.out.println("greedy() : down");
				}
				return movePlayer("Down");
			case 3:
				if (isDebug) {
					System.out.println("greedy() : left");
				}
				return movePlayer("Left");
			default:
				return -1;
		}

	}

	public int eGreedy(float e) {

		Tile tile = grid.getTile(playerCol, playerRow);
		int move;

		ArrayList<Integer> bestMoves = new ArrayList<Integer>();

		// generate a random value to see if should choose randomly
		float eRoll = randomGenerator.nextFloat();

		// if e is greater than a random roll
		if (e > eRoll) {
			// choose next move at random
			move = randomGenerator.nextInt(tile.QValues.length);
			while (tile.canMove[move] == false) {
				move = randomGenerator.nextInt(tile.QValues.length);
			}

		} else {

			// get the value of each move for the player
			float maxValue = -Float.MAX_VALUE;

			int maxIndex = -1;
			// get the the tile
			for (int index = 0; index < tile.QValues.length; index++) {
				if (!tile.canMove[index]) {
					continue;
				}
				float qValue = tile.QValues[index];

				if (isDebug) {
					System.out.println("greedy() : maxValue = " + maxValue);
					System.out.println("greedy() : QValues[" + index + "] = " + qValue);
					System.out.println("greedy() : qValue > maxValue = " + (qValue > maxValue));
				}
				if (qValue > maxValue) {
					maxValue = qValue;
					maxIndex = index;
					bestMoves = new ArrayList<Integer>();
					bestMoves.add(index);
				} else if (qValue == maxValue) {
					bestMoves.add(index);
				}
			}

			// if more than one best move (equal value)
			if (bestMoves.size() > 1) {
				// pick a random move
				int roll = randomGenerator.nextInt(bestMoves.size());

				if (previousMoves.size() > 0) {
					while (roll == 1 && previousMoves.peek() == 3
							|| roll == 3 && previousMoves.peek() == 1
							|| roll == 0 && previousMoves.peek() == 2
							|| roll == 2 && previousMoves.peek() == 0) {
						roll = randomGenerator.nextInt(bestMoves.size());
					}
				}
				move = bestMoves.get(roll);
			} else {
				move = maxIndex;
			}
		}

		return move;

	}

	public int movePlayer(String direction) {

		// Q-Update

		// System.out.println("movePlayer(" + direction + ")");
		if (previousMoves.size() >= previousMoveCount) {
			previousMoves.pop();
		}
		// save the player's previous position
		int previousPlayerCol = playerCol;
		int previousPlayerRow = playerRow;

		// get the new position of the player by the move direction
		switch (direction) {
			case "Up":
				playerRow += 1;
				break;
			case "Down":
				playerRow -= 1;
				break;
			case "Left":
				playerCol -= 1;
				break;
			case "Right":
				playerCol += 1;
				break;
		}

		// check if the player moved off the board
		if (playerRow >= rows || playerRow < 0 ||
				playerCol >= cols || playerCol < 0
				|| grid.getTile(playerCol, playerRow).getIsEmpty()) {
			// play.newEvent("Player fell off world!!!");
			// playerInit();
			// play.newEvent("Move fail!!!");
			playerCol = previousPlayerCol;
			playerRow = previousPlayerRow;
			return 0;
			// playerInit();
		}

		// get the next tile
		Tile nextTile = grid.getTile(playerCol, playerRow);

		float[] nextValues = nextTile.getQValues();

		// get the max value of the next tile
		float maxValue = Integer.MIN_VALUE;

		// loop over nextValues
		for (int index = 0; index < nextValues.length; index++) {
			// find the max
			if (nextValues[index] > maxValue) {
				maxValue = nextValues[index];
			}
		}

		switch (direction) {
			case "Up":
				grid.getTile(previousPlayerCol, previousPlayerRow).updateQValue(0, maxValue,
						learningRate);
				previousMoves.addLast(0);
				break;
			case "Down":
				grid.getTile(previousPlayerCol, previousPlayerRow).updateQValue(2, maxValue,
						learningRate);
				previousMoves.addLast(2);
				break;
			case "Left":
				grid.getTile(previousPlayerCol, previousPlayerRow).updateQValue(3, maxValue,
						learningRate);
				previousMoves.addLast(3);
				break;
			case "Right":
				grid.getTile(previousPlayerCol, previousPlayerRow).updateQValue(1, maxValue,
						learningRate);
				previousMoves.addLast(1);
				break;
		}

		// check if player moved onto exit tile
		if (nextTile.getIsExit()) {
			// then set the value
			nextTile.setQValue(0, nextTile.getExitValue());
			String eventString = "Player exited with a ";
			if (nextTile.getExitValue() > 0) {
				eventString += "win";
			} else {
				eventString += "lose";
			}
			play.newEvent(eventString);
			playerInit();
			return (int) nextTile.getExitValue();
		}

		return 0;
	}

	public void render(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
		grid.render(shapeRenderer, spriteBatch);

		spriteBatch.begin();

		Assets.instance.fonts.defaultNormal.draw(spriteBatch,
				previousMoves.toString(),
				-(gridWidth / 2) + 5,
				-(gridHeight / 2) - 5);

		spriteBatch.end();

		try {
			Vector2 playerPos = grid.getTile(playerCol, playerRow).getCenter();
			if (playerPos != null) {
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(Color.GOLD);
				shapeRenderer.circle(playerPos.x, playerPos.y, playerSize);
				shapeRenderer.setColor(Color.PURPLE);
				shapeRenderer.circle(playerPos.x, playerPos.y, (playerSize * .9f));
				shapeRenderer.setColor(Color.GOLD);
				shapeRenderer.circle(playerPos.x, playerPos.y, (playerSize * .6f));
				shapeRenderer.setColor(Color.TEAL);
				shapeRenderer.circle(playerPos.x, playerPos.y, (playerSize * .4f));
				shapeRenderer.setColor(Color.GOLD);
				shapeRenderer.circle(playerPos.x, playerPos.y, (playerSize * .1f));
				shapeRenderer.end();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void touchDown(float x, float y) {
		grid.touch(x, y);
	}

	public float getE() {
		return e;
	}

	public void setE(float e) {
		this.e = e;
	}

	public float getStepE() {
		return stepE;
	}

	public void setStepE(float stepE) {
		this.stepE = stepE;
	}

	public void cycleMode() {

		mode = (mode + 1) % (modes.length);

	}

	public ArrayList<String> getStats() {
		ArrayList<String> stats = new ArrayList<String>();
		stats.add("Mode:" + modes[mode]);
		stats.add("E:" + e);
		stats.add("StepE:" + stepE);
		stats.add("Step:" + step);
		stats.add("StepsToGo:" + stepsToGo);
		stats.add("Alpha:" + learningRate);
		stats.add("BestPathLength:" + bestPathLength);

		return stats;
	}

	public int getStepsToGo() {
		return stepsToGo;
	}

	public void setStepsToGo(int stepsToGo) {
		this.stepsToGo = stepsToGo;
	}

	public float getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getWorldType() {
		return worldType;
	}

	public void setWorldType(int worldType) {
		this.worldType = worldType;
	}

	public void displayResults() {
		try {

			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("results.txt"), "utf-8"));
			for (int modeIndex = 0; modeIndex < modes.length; modeIndex++) {
				for (int worldTypeIndex = 0; worldTypeIndex < worldTypes.length; worldTypeIndex++) {
					for (int index = 0; index < worldPaths.get(modeIndex).get(worldTypeIndex)
							.size(); index++) {
						writer.write(modes[modeIndex] + "," + worldTypes[worldTypeIndex]
								+ ","
								+ worldPaths.get(modeIndex).get(worldTypeIndex).get(index)
								+ ","
								+ GamePreferences.instance.e
								+ ","
								+ GamePreferences.instance.alpha
								+ ","
								+ GamePreferences.instance.steps
								+ "\n");
					}
				}
			}

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
