package physics;

import java.io.Serializable;

public class Velocity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private double speed;
	private double angle;
	public Velocity(double speed, double angle) {
		super();
		this.speed = speed;
		this.angle = angle;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
}
