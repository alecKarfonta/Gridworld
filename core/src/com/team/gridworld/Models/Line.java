package com.team.gridworld.Models;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Line {
	private ArrayList<Vector2> points;
	private Color color;

	public Line (Vector2 p1, Vector2 p2) {
		this(p1, p2, Color.DARK_GRAY);
		
	}
	
	public Line (Vector2 p1, Vector2 p2, Color color) {
		this.points = new ArrayList<Vector2>();
		this.points.add(p1);
		this.points.add(p2);
		this.color = color;
	}
	public Line (ArrayList<Vector2> points) {
		this.points = points;
	}
	
	public int getPointCount() {
		return points.size();
	}
	
	public Vector2 getPoint(int index) {
		return points.get(index);
	}
	
	public Vector2 p1() {
		return points.get(0);
	}
	public Vector2 p2() {
		return points.get(1);
	}
	public ArrayList<Vector2> getPoints() {
		return points;
	}
	public void setPoints(ArrayList<Vector2> points) {
		this.points = points;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	
	
}
