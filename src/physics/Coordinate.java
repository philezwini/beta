package physics;

import java.io.Serializable;

public class Coordinate implements Serializable{
	private static final long serialVersionUID = 1L;
	private double x;
	private double y;
	public Coordinate(double x, double y) {
		this.setX(x);
		this.setY(y);
	}
	public Coordinate() {
		x = -1;
		y = -1;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	@Override
	public String toString() {
		return "Coordinate [x=" + x + ", y=" + y + "]";
	}
}
