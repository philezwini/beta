package Entity;

import java.io.Serializable;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import physics.Coordinate;
import physics.Physics2D;
import physics.Velocity;

public abstract class Entity implements Serializable {
	private static final long serialVersionUID = 1L;

	protected double xf;
	protected double yf;
	protected double x;
	protected double y;
	protected Coordinate startPos;
	protected double mass;
	protected double acceleration;
	protected Velocity velocity;
	protected Velocity initVelocity;
	protected ImageView view;
	protected boolean isFirstKick;
	protected static boolean updateView;

	public Entity(String filePath, Coordinate startPos) {
		this.x = startPos.getX();
		this.y = startPos.getY();
		this.xf = x;
		this.yf = y;
		this.velocity = new Velocity(0, 0);
		this.initVelocity = new Velocity(0, 0);
		this.acceleration = 0;
		setFirstKick(true);
		makeView(filePath, x, y);
		updateView = false;
	}

	protected abstract void makeView(String filePath, double xi, double yi);

	public abstract boolean isInField();

	public void updateView() {
		Platform.runLater(() -> {
			view.setX(x);
			view.setY(y);
		});
	}

	public Velocity getInitVelocity() {
		return initVelocity;
	}

	public void setInitVelocity(Velocity initVelocity) {
		this.initVelocity = initVelocity;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public Velocity getVelocity() {
		return velocity;
	}

	public void setVelocity(Velocity velocity) {
		this.velocity = velocity;
	}

	public ImageView getView() {
		return view;
	}

	public boolean shouldMove() {
		double d = Physics2D.distance(x, y, xf, yf);
		return d > 3;
	}

	public Coordinate getPos() {
		return new Coordinate(x, y);
	}

	public void setPos(Coordinate pos) {
		this.x = pos.getX();
		this.y = pos.getY();
		if (updateView) {
			Platform.runLater(() -> {
				updateView();
			});
		}
	}

	public Coordinate getStartPos() {
		return startPos;
	}

	public void setStartPos(Coordinate startPos) {
		this.startPos = startPos;
	}

	public Coordinate getFinalPos() {
		return new Coordinate(xf, yf);
	}

	public void setFinalPos(Coordinate c) {
		xf = c.getX();
		yf = c.getY();
	}

	public boolean isFirstKick() {
		return isFirstKick;
	}

	public void setFirstKick(boolean isFirstKick) {
		this.isFirstKick = isFirstKick;
	}

	public static void enableView(boolean b) {
		if (!b)
			updateView = false;
		else
			updateView = true;
	}
}
