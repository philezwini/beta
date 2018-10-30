package Entity;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import physics.Coordinate;
import physics.Physics2D;

public class Ball extends Entity {
	private static final long serialVersionUID = 1L;

	private Player kicker, prevKicker;
	private Coordinate startPos;

	public Ball(Coordinate startPos) {
		super("img/balls/ball.png", startPos);
		this.mass = 0.43;
		this.startPos = startPos;
		setKicker(null);
	}

	@Override
	protected void makeView(String filePath, double xi, double yi) {
		File ballFile = new File(filePath); // Extract image file.
		Image ballImg = new Image(ballFile.toURI().toString(), 20, 20, false, true); // Get the image using its URI.
		view = new ImageView(ballImg);
		view.setX(xi);
		view.setY(yi);
	}

	public boolean isInLeftGoals() {
		boolean v = (y >= Physics2D.Y1_GOAL) && (y <= Physics2D.Y2_GOAL) && (x <= Physics2D.X1_GOAL);
		return v;
	}

	public boolean isInRightGoals() {
		boolean v = (y >= Physics2D.Y1_GOAL) && (y <= Physics2D.Y2_GOAL) && (x >= Physics2D.X2_GOAL);
		return v;
	}

	@Override
	public boolean shouldMove() {
		boolean s = super.shouldMove() && isInField();

		return s;
	}

	@Override
	public boolean isInField() {
		boolean b = ((x >= Physics2D.FIELD_MIN_X) && (y >= Physics2D.FIELD_MIN_Y) && (x <= Physics2D.FIELD_MAX_X)
				&& (y <= Physics2D.FIELD_MAX_Y));
		return b;
	}

	public Player getKicker() {
		return kicker;
	}

	public void setKicker(Player kicker) {
		this.kicker = kicker;
	}

	public Coordinate getStartPos() {
		return startPos;
	}

	public void setStartPos(Coordinate startPos) {
		this.startPos = startPos;
	}

	public Player getPrevKicker() {
		return prevKicker;
	}

	public void setPrevKicker(Player prevKicker) {
		this.prevKicker = prevKicker;
	}
}
