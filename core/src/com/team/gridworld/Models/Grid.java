package com.team.gridworld.Models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Grid {
	private int			rows, cols, width, height, rowHeight, colWidth;
	private Vector2		offset;
	private Tile[][]	tiles;

	public Grid(int rows, int cols, int width, int height) {
		this.tiles = new Tile[cols][rows];
		this.rowHeight = height / rows;
		this.colWidth = width / cols;
		this.rows = rows;
		this.cols = cols;
		this.width = width;
		this.height = height;

		init();
	}

	private void init() {
		// add tiles
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {

				// String title = "" + (col * cols + row);
				String title = col + "," + row;

				float y = -(.5f * width) + (col * colWidth);
				float x = -(.5f * height) + (row * rowHeight);

				boolean[] canMove = new boolean[4];
				if (row < rows) {
					canMove[0] = true;
				} else {
					canMove[0] = false;
				}
				if (col < cols) {
					canMove[1] = true;
				} else {
					canMove[1] = false;
				}
				if (row > 0) {
					canMove[2] = true;
				} else {
					canMove[2] = false;
				}
				if (col > 0) {
					canMove[3] = true;
				} else {
					canMove[3] = false;
				}

				tiles[col][row] = new Tile(y, x, colWidth - 1, rowHeight - 1, canMove, title);
			}
		}

	}

	public Vector2 getOffset() {
		return offset;
	}

	public Tile getTile(int col, int row) {
		if (col < 0 || col >= cols || row < 0 || row >= rows) {
			return null;
		}
		return tiles[col][row];
	}

	public Vector2 getSquareCenter(int col, int row) {
		int x = col * colWidth + (colWidth / 2);
		int y = row * rowHeight + (rowHeight / 2);
		return new Vector2(x, y);
	}

	public void render(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {

		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				tiles[col][row].render(shapeRenderer, spriteBatch);
			}
		}

	}

	public int getRowCount() {
		return rows;
	}

	public int getColCount() {
		return cols;
	}

	public void touch(float x, float y) {
//		System.out.println("Grid.touch(): screen pos = " + x + " " + y);
		int col;
		if (cols % 2 != 0) {
			 col = (int) Math.round((x / colWidth)  + (cols / 2));
		} else {
			 col = (int) Math.floor((x / colWidth)  + (cols / 2));
		}
		 
		int row ;

		if (rows % 2 != 0) {
			row = (int) Math.round((y / rowHeight)) + (rows / 2);
		} else {
			row = (int) Math.floor((y / rowHeight)) + (rows / 2);
		}
		
//		System.out.println("Grid.touch(): grid pos = " + col + " " + row);
		if (col >= 0 && col < cols && row >= 0 && row < rows) {
			tiles[col][row].tap();
		}
	}
}
