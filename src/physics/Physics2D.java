package physics;

import java.util.ArrayList;

import Entity.Ball;
import Entity.Entity;
import Entity.Player;

public class Physics2D {
	public static final double FIELD_MIN_X = 64;
	public static final double FIELD_MAX_X = 816;
	public static final double FIELD_MIN_Y = 27;
	public static final double FIELD_MAX_Y = 502;
	public static final double FIELD_CENTER_X = 441;
	public static final double FIELD_CENTER_Y = 263;
	public static final double FIELD_WIDTH = FIELD_MAX_X - FIELD_MIN_X;
	public static final double FIELD_HEIGHT = FIELD_MAX_Y - FIELD_MIN_Y;

	public static final double X1_GOAL = FIELD_MIN_X;
	public static final double X2_GOAL = FIELD_MAX_X;

	public static final double Y1_GOAL = 233;
	public static final double Y2_GOAL = 299;

	public static boolean isInCollision(Player p, ArrayList<Player> players) {
		for (Player p1 : players) {
			if (!p1.equals(p) && inContact(p1, p)) {
				double pdx = p.getPos().getX() - p.getFinalPos().getX();
				double pdy = p.getPos().getY() - p.getFinalPos().getY();
				double p1dx = p1.getPos().getX() - p1.getFinalPos().getX();
				double p1dy = p1.getPos().getY() - p1.getFinalPos().getY();
				bounceBackwards(p, p1, pdx, pdy, p1dx, p1dy);
				return true;
			}
		}
		return false;
	}

	public static void bounceBackwards(Player p1, Player p2, double p1Dx, double p1Dy, double p2Dx, double p2Dy) {
		double scFactor = 2;
		double xf = p1.getPos().getX() + p1Dx * scFactor;
		double yf = p1.getPos().getY() + p1Dy * scFactor;

		if (isInField(xf, yf))
			p1.setPos(new Coordinate(xf, yf));

		xf = p2.getPos().getX() - p2Dx * scFactor;
		yf = p2.getPos().getY() - p2Dy * scFactor;

		if (isInField(xf, yf))
			p2.setPos(new Coordinate(xf, yf));
	}

	private static boolean isInField(double x, double y) {
		return ((x >= FIELD_MIN_X) && (y >= FIELD_MIN_Y) && (x < FIELD_MAX_X) && (y < FIELD_MAX_Y));
	}

	public static void movePlayer(Player p, ArrayList<Player> players) {
		if (p.isInField() && !isInCollision(p, players)) {
			p.setPos(p.getFinalPos());
		}
	}

	public static boolean moveBall(Ball b) {
		if (b.shouldMove()) {
			double vi = 0.0008;
			b.getVelocity().setSpeed(vi);
			moveEntity(b);
			return true;
		}
		return false;
	}

	private static void moveEntity(Entity e) {
		double distance = distance(e.getPos().getX(), e.getPos().getY(), e.getFinalPos().getX(),
				e.getFinalPos().getY());
		double dx = Math.abs(e.getFinalPos().getX() - e.getPos().getX());
		double theta = Math.acos(dx / distance);
		distance = e.getVelocity().getSpeed() + 0.5 * e.getAcceleration() * Math.pow(0.001, 2);
		double pDistance = toPixels(distance);
		updateCoordinates(e, pDistance, theta);
	}

	private static void updateCoordinates(Entity e, double distance, double angle) {
		calculateCoords(e, distance, angle);
	}

	private static void calculateCoords(Entity e, double distance, double angle) {
		double xNew = 0;
		double yNew = 0;
		if (e.getFinalPos().getX() > e.getPos().getX())
			xNew = e.getPos().getX() + distance * Math.cos(angle);

		if (e.getFinalPos().getX() < e.getPos().getX())
			xNew = e.getPos().getX() - distance * Math.cos(angle);

		if (e.getFinalPos().getY() > e.getPos().getY())
			yNew = e.getPos().getY() + distance * Math.sin(angle);

		if (e.getFinalPos().getY() < e.getPos().getY())
			yNew = e.getPos().getY() - distance * Math.sin(angle);

		if (e.getFinalPos().getX() == e.getPos().getX())
			xNew = e.getPos().getX();

		if (e.getFinalPos().getY() == e.getPos().getY())
			yNew = e.getPos().getY();

		e.setPos(new Coordinate(xNew, yNew));
	}

	public static boolean inContact(Player p1, Player p2) {
		double x1 = p1.getPos().getX() + 10;
		double y1 = p1.getPos().getY() + 10;
		double x2 = p2.getPos().getX() + 10;
		double y2 = p2.getPos().getY() + 10;
		double d = Physics2D.distance(x1, y1, x2, y2);
		return d <= 20;
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		double arg1 = Math.pow(x2 - x1, 2);
		double arg2 = Math.pow(y2 - y1, 2);
		return Math.sqrt(arg1 + arg2);
	}

	private static double toPixels(double meters) {
		return meters / 0.00026458;
	}

}
