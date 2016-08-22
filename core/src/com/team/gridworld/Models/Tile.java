package com.team.gridworld.Models;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.team.gridworld.Assets;

public class Tile {
	private ArrayList<Line>	lines;
	private float			width, height;
	private float			x, y;
	private String			title;
	private boolean			isExit, isMovable, isEmpty;
	public boolean[]		canMove;
	public float[]			QValues;
	private float			exitValue;

	public Tile(float x, float y, float width, float height, boolean[] canMove, String title) {
		super();
		this.lines = new ArrayList<Line>();
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.title = title;
		this.isExit = false;
		this.isMovable = false;
		this.isEmpty = true;
		this.QValues = new float[4];
		this.exitValue = 0;
		for (int index = 0; index < 4; index++) {
			this.QValues[index] = 0;
		}
		this.canMove = canMove;

		this.makeBorder();
		this.makeMovable();
		// this.makeExit();
		// this.makeInnerSquare(Color.GREEN);

	}

	public void makeBorder() {
		makeBorder(Color.WHITE);
	}

	public void makeBorder(Color color) {
		// draw a smaller square within the square
		float p1x = x - width;
		p1x += p1x;
		float p1y = y - height;
		p1y += p1y;
		float p2x = (float) (p1x + width);
		float p2y = (float) (p1y + height);

		lines.add(new Line(new Vector2(x, y), new Vector2(
				x + width, y), color));
		lines.add(new Line(new Vector2(x + width, y), new Vector2(
				x + width, y + height), color));
		lines.add(new Line(new Vector2(x + width, y + height), new Vector2(
				x, y + height), color));
		lines.add(new Line(new Vector2(x, y + height), new Vector2(
				x, y), color));

	}

	public void makeExit() {
		makeExit(100, Color.GREEN);
	}

	public void makeExit(int value, Color color) {

		isEmpty = false;
		isMovable = false;
		isExit = true;

		QValues = new float[1];
		for (int index = 0; index < QValues.length; index++) {
			QValues[index] = 0;
		}

		exitValue = value;

		float bezel = width * .1f;

		// draw a smaller square within the square
		float p1x = x;
		p1x += bezel;
		float p1y = y;
		p1y += bezel;
		float p2x = (float) (p1x + (width) - (bezel * 2));
		float p2y = (float) (p1y + (height) - (bezel * 2));

		lines.add(new Line(new Vector2(p1x, p1y), new Vector2(
				p2x, p1y), color));

		lines.add(new Line(new Vector2(p2x, p1y), new Vector2(
				p2x, p2y), color));

		lines.add(new Line(new Vector2(p2x, p2y), new Vector2(
				p1x, p2y), color));

		lines.add(new Line(new Vector2(p1x, p2y), new Vector2(
				p1x, p1y), color));

	}

	public void makeMovable() {
		makeMovable(Color.LIGHT_GRAY);
	}

	public void makeMovable(Color color) {

		isEmpty = false;
		isMovable = true;
		isExit = false;

		QValues = new float[4];
		for (int index = 0; index < QValues.length; index++) {
			QValues[index] = 0;
		}

		// draw a line from the center to each corner
		// top left
		lines.add(new Line(getCenter(), new Vector2(x, y), color));
		lines.add(new Line(getCenter(), new Vector2(
				x + width, y), color));
		lines.add(new Line(getCenter(), new Vector2(x, y + height)));
		lines.add(new Line(getCenter(), new Vector2(
				x + width, y + height), color));
	}

	public Vector2 getCenter() {
		float centerX = x + (width / 2);
		float centerY = y + (height / 2);
		return new Vector2(centerX, centerY);
	}

	public void tap() {
		// System.out.println("Tile.tap()");
		if (isEmpty) {
			lines = new ArrayList<Line>();
			makeBorder();
			makeMovable();
		} else if (isMovable) {
			lines = new ArrayList<Line>();
			makeBorder();
			makeExit();
		} else if (isExit && exitValue >= 0) {
			lines = new ArrayList<Line>();
			makeBorder();
			makeExit(-100, Color.RED);
		} else if (isExit && exitValue < 0) {
			lines = new ArrayList<Line>();
			makeBorder();
			isExit = false;
			isEmpty = true;
		}
	}

	public void render(ShapeRenderer renderer, SpriteBatch spriteBatch) {
		spriteBatch.begin();
		// show tile title
		// Assets.instance.fonts.defaultSmall.draw(spriteBatch,
		// title,
		// x + 5,
		// y + 15);

		// get the max value of the next tile
		float maxValue = Integer.MIN_VALUE;
		int maxIndex = -1;

		// loop over nextValues
		for (int index = 0; index < QValues.length; index++) {
			// find the max
			if (QValues[index] > maxValue) {
				maxValue = QValues[index];
				maxIndex = index;
			}
		}

		// show values
		if (isExit) {
			Assets.instance.fonts.defaultSmall.setColor(getQValueColor(exitValue));
			Assets.instance.fonts.defaultSmall.draw(spriteBatch,
					"" + (int) exitValue,
					getCenter().x,
					getCenter().y);
		} else if (isMovable) {
			Assets.instance.fonts.defaultSmall.setColor(getQValueColor(QValues[0]));

			Assets.instance.fonts.defaultSmall.draw(spriteBatch,
					"" + (int) QValues[0] + (maxIndex == 0 ? "*" : ""),
					getCenter().x,
					getCenter().y + (height * .25f));

			Assets.instance.fonts.defaultSmall.setColor(getQValueColor(QValues[1]));
			Assets.instance.fonts.defaultSmall.draw(spriteBatch,
					"" + (int) QValues[1] + (maxIndex == 1 ? "*" : ""),
					getCenter().x + (width * .25f),
					getCenter().y);

			Assets.instance.fonts.defaultSmall.setColor(getQValueColor(QValues[2]));
			Assets.instance.fonts.defaultSmall.draw(spriteBatch,
					"" + (int) QValues[2] + (maxIndex == 2 ? "*" : ""),
					getCenter().x,
					getCenter().y - (height * .25f));

			Assets.instance.fonts.defaultSmall.setColor(getQValueColor(QValues[3]));
			Assets.instance.fonts.defaultSmall.draw(spriteBatch,
					"" + (int) QValues[3] + (maxIndex == 3 ? "*" : ""),
					getCenter().x - (width * .25f),
					getCenter().y);
		}

		spriteBatch.end();

		spriteBatch.setColor(Color.BLUE);

		// renderer.setColor(Color.BLUE);

		renderer.begin(ShapeType.Line);
		for (Line line : lines) {
			renderer.setColor(line.getColor());
			renderer.line(line.p1(), line.p2());
		}
		renderer.end();

	}

	public ArrayList<Line> getLines() {
		return lines;
	}

	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean getIsExit() {
		return isExit;
	}

	public void setIsExit(boolean isExit) {
		this.isExit = isExit;
	}

	public boolean getIsMovable() {
		return isMovable;
	}

	public void setIsMovable(boolean isMovable) {
		this.isMovable = isMovable;
	}

	public boolean getIsEmpty() {
		return isEmpty;
	}

	public void setIsEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public float[] getQValues() {
		return this.QValues;
	}

	public void setQValues(float[] values) {
		this.QValues = values;
	}

	public void updateQValue(int index, float value, float rate) {
		if (index > this.QValues.length - 1) {
			return;
		}
		float newQ = ((1 - rate) * this.QValues[index]) + (rate * value);
		this.QValues[index] = newQ;
	}

	public void setQValue(int index, float value) {
		if (index > this.QValues.length - 1) {
			return;
		}
		this.QValues[index] = value;
	}

	public float getQValue(int index) {
		if (index > this.QValues.length - 1) {
			return 0;
		}
		return this.QValues[index];
	}

	public float getExitValue() {
		return exitValue;
	}

	public void setExitValue(float exitValue) {
		this.exitValue = exitValue;
	}

	public Color getQValueColor(float value) {
		Color color;
		if (value > 0) {
			color = Color.WHITE.cpy().lerp(Color.GREEN, value / 100);
		} else {
			color = Color.WHITE.cpy().lerp(Color.RED, Math.abs(value) / 100);
		}

		return color;

	}

}
