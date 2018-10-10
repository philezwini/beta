package Entity;

import java.io.File;
import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.DTree;
import physics.Coordinate;
import physics.Physics2D;

public class Player extends Entity {
	private static final long serialVersionUID = 1L;

	private Direction direction;
	private Position defPos; // The players default position according to the team's strategy.
	private int id;
	private Team team;
	private int maturity;
	private int score;
	private DTree model;
	private Coordinate prevPos;
	private int timeWindow;

	public Player(String filePath, Team team, int id, Coordinate startPos, Position defPos) {
		super(filePath, startPos);
		this.mass = 70;
		this.setId(id);
		this.setTeam(team);
		this.score = 0;
		this.startPos = startPos;
		this.prevPos = startPos;
		this.setDefPos(defPos);
		timeWindow = 3;
	}

	@Override
	protected void makeView(String filePath, double xi, double yi) {
		File file = new File(filePath); // Extract image file.
		Image img = new Image(file.toURI().toString(), 20, 20, false, true); // Get the image using its URI.
		view = new ImageView(img);
		view.setX(xi);
		view.setY(yi);
	}

	public void kickBall(Ball b, Coordinate c) {
		if (b.getKicker() != null) {
			b.setPrevKicker(b.getKicker());
		}

		b.setKicker(this);
		b.setFinalPos(c);
		Physics2D.moveBall(b);
	}

	public void move(Coordinate c, ArrayList<Player> players) {
		setFinalPos(new Coordinate(c.getX(), c.getY()));
		Physics2D.movePlayer(this, players);
	}

	@Override
	public boolean isInField() {
		return ((xf >= Physics2D.FIELD_MIN_X) && (yf >= Physics2D.FIELD_MIN_Y) && (xf < Physics2D.FIELD_MAX_X)
				&& (yf < Physics2D.FIELD_MAX_Y));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public int getMaturity() {
		return maturity;
	}

	public void setMaturity(int maturity) {
		this.maturity = maturity;
	}

	public void reset() {
		score = 0;
		model = null;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public DTree getModel() {
		return model;
	}

	public void setModel(DTree model) {
		this.model = model;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Position getDefPos() {
		return defPos;
	}

	public void setDefPos(Position defPos) {
		this.defPos = defPos;
	}

	public Coordinate getPrevPos() {
		return prevPos;
	}

	public void setPrevPos(Coordinate prevPos) {
		this.prevPos = prevPos;
	}

	public int getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(int timeWindow) {
		this.timeWindow = timeWindow;
	}
}
