package game;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import Entity.Ball;
import Entity.Direction;
import Entity.Player;
import Entity.Position;
import Entity.Team;
import exception.GAException;
import fileIO.FileInputHandler;
import fileIO.FileOutputHandler;
import ga.Selector;
import model.Action;
import model.DTree;
import model.Node;
import model.State;
import physics.Coordinate;
import physics.Physics2D;
import ui.SimulationController;

public class GameState implements Runnable {
	private ArrayList<Player> players;
	private ArrayList<Player> yTeam, bTeam;
	private Ball ball;
	private int bScore, yScore;
	private long matchLength;
	private int elapsed;
	private int generation;

	public GameState(long matchLength) {
		this.setMatchLength(matchLength);
		elapsed = 0;
		yScore = 0;
		bScore = 0;
		generation = 0;
		players = new ArrayList<Player>();
		bTeam = new ArrayList<Player>();
		yTeam = new ArrayList<Player>();
		initialize();
	}

	public void initialize() {
		initBall();

		File history = new File("data/progress/players.dat");
		if (!history.exists()) {
			System.out.println("No history found. Creating new players.");
			initPlayers();
		} else {
			System.out.println("Saved progress found. Loading...");
			try {
				loadPlayers();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		if (elapsed == matchLength) {
			try {
				elapsed = 0;
				updateStats();
				if (!Selector.monitor(yTeam, bTeam)) {
					System.out.println("Done.");// We have reached our stopping condition. We have to stop evolution.
					// Instruct the ExecutorService to shutdown after this thread completes.
					SimulationController.stopGame(1);
					reset(true);
					return;
				}
				generation = Selector.getCurrentGen();
				reset(false);
			} catch (GAException e) {
				e.printStackTrace();
			}
		}

		if (!Physics2D.moveBall(ball)) {
			if (ball.isInLeftGoals()) {
				updateScore(Team.BLACK);
				reset(false);
				return;
			}

			if (ball.isInRightGoals()) {
				updateScore(Team.YELLOW);
				reset(false);
				return;
			}

			if (!ball.isInField()) {
				penalize(ball.getKicker(), 1000);
				ball.setPos(new Coordinate(Physics2D.FIELD_CENTER_X, Physics2D.FIELD_CENTER_Y));
				ball.setFinalPos(ball.getPos());
				return;
			}
		}

		// -- Reward passes. --//
		if ((ball.getPrevKicker() != null) && (!ball.getPrevKicker().equals(ball.getKicker()))
				&& (ball.getPrevKicker().getTeam() == ball.getKicker().getTeam())) {
			reward(ball.getPrevKicker(), 10);
			reward(ball.getKicker(), 10);
		}

		for (Player p : players) {
			/*
			 * if (p.getTimeWindow() == 0) { if (Physics2D.distance(p.getPos().getX(),
			 * p.getPos().getY(), p.getPrevPos().getX(), p.getPrevPos().getY()) < 2) {
			 * penalize(p, 50); } p.setTimeWindow(3); } else { if
			 * (Physics2D.distance(p.getPos().getX(), p.getPos().getY(),
			 * p.getPrevPos().getX(), p.getPrevPos().getY()) < 2) {
			 * p.setTimeWindow(p.getTimeWindow() - 1); } else { p.setPrevPos(p.getPos());
			 * p.setTimeWindow(3); } }
			 */

			try {
				Action a = process(p);
				evaluate(p, a);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		elapsed++;
	}

	private void updateStats() {
		double totalScore = 0;
		double yTeamAvg = 0;
		double bTeamAvg = 0;

		for (Player p : yTeam) {
			totalScore += p.getScore();
		}
		yTeamAvg = totalScore / 10;
		totalScore = 0;
		for (Player p : bTeam) {
			totalScore += p.getScore();
		}
		bTeamAvg = totalScore / 10;

		SimulationController.updateGeneration(generation + "", yTeamAvg, bTeamAvg);
	}

	private void updateScore(Team team) {
		Player scorer = ball.getKicker();
		if (team == Team.BLACK) {
			bScore++;

			if (scorer.getTeam() == Team.YELLOW)
				penalize(scorer, 10000);
			else
				reward(scorer, 10000);

			for (Player p : players) {
				if (!p.equals(scorer)) {
					if (p.getTeam() == Team.BLACK)
						reward(p, 5000);
					else
						penalize(p, 5000);
				}
			}
		} else {
			yScore++;

			if (scorer.getTeam() == Team.BLACK)
				penalize(scorer, 10000);
			else
				reward(scorer, 10000);

			for (Player p : players) {
				if (!p.equals(scorer)) {
					if (p.getTeam() == Team.YELLOW)
						reward(p, 5000);
					else
						penalize(p, 5000);
				}
			}
		}
		SimulationController.updateScore(yScore, bScore);
	}

	private void makeDefaultKick(Player p) {
		Direction direction = p.getDirection();
		Coordinate c = null;
		int d = 70;
		switch (direction) {
		case N:
			c = new Coordinate(p.getPos().getX(), p.getPos().getY() - d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case S:
			c = new Coordinate(p.getPos().getX(), p.getPos().getY() + d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
		case W:
			c = new Coordinate(p.getPos().getX() - d, p.getPos().getY());

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case E:
			c = new Coordinate(p.getPos().getX() + d, p.getPos().getY());

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case NE:

			c = new Coordinate(p.getPos().getX() + d, p.getPos().getY() - d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case NW:

			c = new Coordinate(p.getPos().getX() - d, p.getPos().getY() - d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case SE:
			c = new Coordinate(p.getPos().getX() + d, p.getPos().getY() + d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		case SW:
			c = new Coordinate(p.getPos().getX() - d, p.getPos().getY() + d);

			if (evaluateKick(p, c) > 0) {
				reward(p, 500);
			}

			p.kickBall(ball, c);
			break;
		}
	}

	private int evaluateKick(Player p, Coordinate c) {
		double yGoal = (Physics2D.Y1_GOAL + Physics2D.Y2_GOAL) / 2;

		if (p.getTeam() == Team.BLACK) {
			double oldDistance = Physics2D.distance(ball.getPos().getX(), ball.getPos().getY(), Physics2D.X1_GOAL,
					yGoal);
			double newDistance = Physics2D.distance(c.getX(), c.getY(), Physics2D.X1_GOAL, yGoal);

			return (int) (oldDistance - newDistance);
		}

		double oldDistance = Physics2D.distance(ball.getPos().getX(), ball.getPos().getY(), Physics2D.X2_GOAL, yGoal);
		double newDistance = Physics2D.distance(c.getX(), c.getY(), Physics2D.X2_GOAL, yGoal);
		return (int) (oldDistance - newDistance);
	}

	private Action process(Player p) throws Exception {
		Node cursor = p.getModel().root();
		while (!cursor.isLeaf()) {
			if (currentPState(cursor.getState(), p)) {
				cursor = cursor.getLeftChild();
			} else {
				cursor = cursor.getRightChild();
			}
		}
		return cursor.getAction();
	}

	/*
	 * This is essentially our fitness function. In this function, the player is
	 * rewarded or penalized based on the state that it is in and the action that it
	 * is performing.
	 */
	private void evaluate(Player p, Action a) {
		// We first check to see how close the player is to the ball and give a reward
		// if possible.

		double d = Physics2D.distance(ball.getPos().getX(), ball.getPos().getY(), p.getPos().getX(), p.getPos().getY());

		if (d < 20)
			reward(p, 10);
		if ((d > 20) && (d <= 50))
			reward(p, 5);

		if ((d > 50) && (d <= 100))
			reward(p, 1);

		// The player is running with the ball.
		if (isInPoss(p)) {
			penalizeOpponents(p, 2);
			reward(p, 1000);
			makeDefaultKick(p);
		}
		updatePState(p, a);// The player is simply moving.

		// Penalize collisions.
		if (Physics2D.isInCollision(p, players)) {
			penalize(p, 10);
		}

		// Penalize the player if it moves outside of the field.
		if (!p.isInField()) {
			penalize(p, 50);
		}

		/*
		 * If the ball is currently in the black team's half, reward the yellow team.
		 */

		/*
		 * if (ball.isInField() && (ball.getPos().getX() > Physics2D.FIELD_CENTER_X)) {
		 * for (Player p1 : yTeam) { reward(p1, 10); } for (Player p1 : bTeam) {
		 * penalize(p1, 10); } }
		 * 
		 * // If the ball is currently in the yellow team's half, reward the black team.
		 * 
		 * if (ball.isInField() && (ball.getPos().getX() < Physics2D.FIELD_CENTER_X)) {
		 * for (Player p1 : bTeam) { reward(p1, 10); } for (Player p1 : yTeam) {
		 * penalize(p1, 10); } }
		 */

	}

	private void penalizeOpponents(Player p, int penalty) {
		for (Player p1 : players) {
			if (p.getTeam() != p1.getTeam())
				penalize(p1, penalty);
		}
	}

	private void penalize(Player p, int penalty) {
		p.setScore(p.getScore() - penalty);
	}

	private void reward(Player p, int reward) {
		p.setScore(p.getScore() + reward);
	}

	private void updatePState(Player p, Action a) {
		Coordinate c = null;
		double moveDistance = 2;
		switch (a) {
		case MOVE_E:
			c = new Coordinate(p.getPos().getX() + moveDistance, p.getPos().getY());
			p.setDirection(Direction.E);
			p.move(c, players);
			break;
		case MOVE_W:
			c = new Coordinate(p.getPos().getX() - moveDistance, p.getPos().getY());
			p.setDirection(Direction.W);
			p.move(c, players);
			break;
		case MOVE_S:
			c = new Coordinate(p.getPos().getX(), p.getPos().getY() + moveDistance);
			p.setDirection(Direction.S);
			p.move(c, players);
			break;
		case MOVE_N:
			c = new Coordinate(p.getPos().getX(), p.getPos().getY() - moveDistance);
			p.setDirection(Direction.N);
			p.move(c, players);
			break;
		case MOVE_SE:
			c = new Coordinate(p.getPos().getX() + moveDistance, p.getPos().getY() + moveDistance);
			p.setDirection(Direction.SE);
			p.move(c, players);
			break;
		case MOVE_SW:
			c = new Coordinate(p.getPos().getX() - moveDistance, p.getPos().getY() - moveDistance);
			p.setDirection(Direction.SW);
			p.move(c, players);
			break;
		case MOVE_NE:
			c = new Coordinate(p.getPos().getX() + moveDistance, p.getPos().getY() - moveDistance);

			p.setDirection(Direction.NE);
			p.move(c, players);
			break;
		case MOVE_NW:
			c = new Coordinate(p.getPos().getX() - moveDistance, p.getPos().getY() - moveDistance);
			p.setDirection(Direction.NW);
			p.move(c, players);
			break;
		}
	}

	private boolean currentPState(State state, Player p) throws Exception {
		switch (state) {
		case IN_POSS:
			return isInPoss(p);
		case BALL_N:
			return isBallNorth(p);
		case BALL_S:
			return isBallSouth(p);
		case BALL_W:
			return isBallWest(p);
		case BALL_E:
			return isBallEast(p);
		case TOUCHLINE_N:
			return isTLNorth(p);
		case TOUCHLINE_S:
			return isTLSouth(p);
		case TOUCHLINE_W:
			return isTLWest(p);
		case TOUCHLINE_E:
			return isTLEast(p);
		case TEAM_MATE_N:
			return isTMNorth(p);
		case TEAM_MATE_S:
			return isTMSouth(p);
		case TEAM_MATE_W:
			return isTMWest(p);
		case TEAM_MATE_E:
			return isTMEast(p);
		case OPPONENT_N:
			return oppNorth(p);
		case OPPONENT_S:
			return oppSouth(p);
		case OPPONENT_W:
			return oppWest(p);
		case OPPONENT_E:
			return oppEast(p);
		case OWN_GOALS_CLOSE:
			return isOGClose(p);
		case OPP_GOALS_CLOSE:
			return isOPGClose(p);
		default:
			throw new Exception("The player is in an unrecognized state: " + state);
		}
	}

	private boolean oppEast(Player p) {
		Player p1 = closestOpp(p);
		return p1.getPos().getX() > p.getPos().getX();
	}

	private boolean oppWest(Player p) {
		Player p1 = closestOpp(p);
		return p1.getPos().getX() < p.getPos().getX();
	}

	private boolean oppSouth(Player p) {
		Player p1 = closestOpp(p);
		return p1.getPos().getY() > p.getPos().getY();
	}

	private boolean oppNorth(Player p) {
		Player p1 = closestOpp(p);
		return p1.getPos().getY() < p.getPos().getY();
	}

	private boolean isTMEast(Player p) {
		Player p1 = closestTM(p);
		return p1.getPos().getX() > p.getPos().getX();
	}

	private boolean isTMWest(Player p) {
		Player p1 = closestTM(p);
		return (p1.getPos().getX() < p.getPos().getX());
	}

	private boolean isTMSouth(Player p) {
		Player p1 = closestTM(p);
		return p1.getPos().getY() > p.getPos().getY();
	}

	private boolean isTMNorth(Player p) {
		Player p1 = closestTM(p);
		return p1.getPos().getY() < p.getPos().getY();
	}

	private boolean isTLEast(Player p) {
		return Physics2D.distance(Physics2D.FIELD_MAX_X, p.getPos().getY(), p.getPos().getX(), p.getPos().getY()) <= 10;
	}

	private boolean isTLWest(Player p) {
		return Physics2D.distance(Physics2D.FIELD_MIN_X, p.getPos().getY(), p.getPos().getX(), p.getPos().getY()) <= 10;
	}

	private boolean isTLSouth(Player p) {
		return Physics2D.distance(p.getPos().getX(), Physics2D.FIELD_MAX_Y, p.getPos().getX(), p.getPos().getY()) <= 10;
	}

	private boolean isTLNorth(Player p) {
		return Physics2D.distance(p.getPos().getX(), Physics2D.FIELD_MIN_Y, p.getPos().getX(), p.getPos().getY()) <= 10;
	}

	private boolean isBallEast(Player p) {
		return !isInPoss(p) && (ball.getPos().getX() > p.getPos().getX());
	}

	private boolean isBallWest(Player p) {
		return !isInPoss(p) && (ball.getPos().getX() < p.getPos().getX());
	}

	private boolean isBallSouth(Player p) {
		return !isInPoss(p) && (ball.getPos().getY() > p.getPos().getY());
	}

	private boolean isBallNorth(Player p) {
		return !isInPoss(p) && (ball.getPos().getY() < p.getPos().getY());
	}

	private Player closestTM(Player p) {
		double clD = Double.POSITIVE_INFINITY; // Distance of the closest player;
		Player closest = null;
		for (Player p1 : players) {
			if (!p1.equals(p) && p1.getTeam() == p.getTeam()) {
				double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), p1.getPos().getX(),
						p1.getPos().getY());
				if (d < clD) {
					clD = d;
					closest = p1;
				}
			}
		}
		return closest;
	}

	private Player closestOpp(Player p) {
		double clD = Double.POSITIVE_INFINITY; // Distance of the closest player;
		Player closest = null;
		for (Player p1 : players) {
			if (!p1.equals(p) && p1.getTeam() != p.getTeam()) {
				double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), p1.getPos().getX(),
						p1.getPos().getY());
				if (d < clD) {
					clD = d;
					closest = p1;
				}
			}
		}
		return closest;
	}

	private boolean isInPoss(Player p) {
		double bx = ball.getPos().getX() + 10;
		double by = ball.getPos().getY() + 10;
		double px = p.getPos().getX() + 10;
		double py = p.getPos().getY() + 10;
		double distance = Physics2D.distance(bx, by, px, py);
		boolean b = distance <= 25;
		return b;
	}

	private boolean isOPGClose(Player p) {
		if (p.getTeam() == Team.BLACK) {
			double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), Physics2D.X1_GOAL, Physics2D.Y1_GOAL);
			if (d > 300)
				return false;
		} else {
			double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), Physics2D.X2_GOAL, Physics2D.Y2_GOAL);
			if (d > 300)
				return false;
		}
		return true;
	}

	private boolean isOGClose(Player p) {
		if (p.getTeam() == Team.BLACK) {
			double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), Physics2D.X2_GOAL, Physics2D.Y2_GOAL);
			if (d > 320)
				return false;
		} else {
			double d = Physics2D.distance(p.getPos().getX(), p.getPos().getY(), Physics2D.X1_GOAL, Physics2D.Y1_GOAL);
			if (d > 320)
				return false;
		}
		return true;
	}

	public void reset(boolean fullReset) {
		ball.setPos(new Coordinate(ball.getStartPos().getX(), ball.getStartPos().getY()));
		ball.setFinalPos(ball.getPos());

		players.clear();
		players.addAll(yTeam);
		players.addAll(bTeam);

		for (Player p : players) {
			p.setPos(p.getStartPos());
			p.setDirection(randDir());
			//p.getModel().genDebugCode(p.getId() + "_");
		}

		if (fullReset) {
			generation = 0;
			yScore = 0;
			bScore = 0;
		}
	}

	/*
	 * CREDIT: Yellow Soccer Jersey picture was obtained for free from:
	 * https://pngtree.com/free-icons/soccer jersey.
	 * CREDIT: Black Soccery Jersey
	 * picture was obtained for free from:
	 * https://www.onlinewebfonts.com/icon/445726
	 */
	private void loadPlayers() throws Exception {
		ArrayList<DTree> models = FileInputHandler.loadModels();
		if (models == null || models.size() != 20)
			throw new Exception("Corrupted data file!");

		for (DTree model : models) {
			if (model.getpTeam() == Team.YELLOW) {

				Player p = new Player("img/players/yellow.png", Team.YELLOW, model.getpId(), model.getPStartPos(),
						model.getpPos());
				p.setDirection(randDir());
				p.setModel(model);
				yTeam.add(p);
				players.add(p);
			} else {
				Player p = new Player("img/players/black.png", Team.BLACK, model.getpId(), model.getPStartPos(),
						model.getpPos());
				p.setDirection(randDir());
				p.setModel(model);
				bTeam.add(p);
				players.add(p);
			}
		}
	}

	public void initPlayers() {
		int id = 0;

		for (int i = 0; i < 10; i++) {
			Position pos = detPosition(i);
			Coordinate c = detCoordinate(pos, Team.YELLOW);
			Player p = new Player("img/players/yellow.png", Team.YELLOW, id++, c, pos);
			p.setDirection(randDir());

			DTree model = new DTree(6);
			model.setpId(p.getId());
			model.setPStartPos(p.getStartPos());
			model.setpPos(pos);

			model.setpTeam(p.getTeam());
			//model.genDebugCode(model.getpId() + "");
			p.setModel(model);

			yTeam.add(p);
			players.add(p);
		}

		for (int i = 0; i < 10; i++) {
			Position pos = detPosition(i);
			Coordinate c = detCoordinate(pos, Team.BLACK);
			Player p = new Player("img/players/black.png", Team.BLACK, id++, c, pos);
			p.setDirection(randDir());

			DTree model = new DTree(6);
			model.setpId(p.getId());
			model.setPStartPos(p.getStartPos());
			model.setpPos(pos);

			model.setpTeam(p.getTeam());
			//model.genDebugCode(model.getpId() + "");
			p.setModel(model);

			bTeam.add(p);
			players.add(p);
		}
	}

	private Coordinate detCoordinate(Position pos, Team team) {
		if (team == Team.YELLOW) {
			switch (pos) {
			case LCB:
				return new Coordinate(143, 200);
			case RCB:
				return new Coordinate(143, 350);
			case LB:
				return new Coordinate(188, 90);
			case RB:
				return new Coordinate(188, 451);
			case LCM:
				return new Coordinate(280, 200);
			case RCM:
				return new Coordinate(280, 350);
			case LW:
				return new Coordinate(350, 90);
			case RW:
				return new Coordinate(350, 451);
			case LS:
				return new Coordinate(400, 250);
			case RS:
				return new Coordinate(400, 300);
			}
		} else {
			switch (pos) {
			case LCB:
				return new Coordinate(750, 350);
			case RCB:
				return new Coordinate(750, 200);
			case LB:
				return new Coordinate(700, 451);
			case RB:
				return new Coordinate(700, 90);
			case LCM:
				return new Coordinate(590, 350);
			case RCM:
				return new Coordinate(590, 200);
			case LW:
				return new Coordinate(500, 451);
			case RW:
				return new Coordinate(500, 90);
			case LS:
				return new Coordinate(473, 300);
			case RS:
				return new Coordinate(473, 250);
			}
		}
		return null;
	}

	private Position detPosition(int i) {
		switch (i) {
		case 0:
			return Position.LCB;
		case 1:
			return Position.RCB;
		case 2:
			return Position.LB;
		case 3:
			return Position.RB;
		case 4:
			return Position.RCM;
		case 5:
			return Position.LCM;
		case 6:
			return Position.LW;
		case 7:
			return Position.RW;
		case 8:
			return Position.LS;
		case 9:
			return Position.RS;
		}
		return null;
	}

	private Direction randDir() {
		int roll = new Random().nextInt(8);
		switch (roll) {
		case 0:
			return Direction.E;
		case 1:
			return Direction.W;
		case 2:
			return Direction.S;
		case 3:
			return Direction.N;
		case 4:
			return Direction.SE;
		case 5:
			return Direction.SW;
		case 6:
			return Direction.SE;
		case 7:
			return Direction.SW;
		}
		return null;
	}

	public void initBall() {
		ball = new Ball(new Coordinate(Physics2D.FIELD_CENTER_X, Physics2D.FIELD_CENTER_Y));
	}

	/*
	 * private static Coordinate randCoord(int minX, int minY, int maxX, int maxY) {
	 * Random r = new Random(); int randX = minX + r.nextInt(maxX + 1 - minX); int
	 * randY = minY + r.nextInt(maxY + 1 - minY); double x = (double) randX; double
	 * y = (double) randY; return new Coordinate(x, y); }
	 */

	public void saveState() {
		ArrayList<DTree> models = new ArrayList<>();
		for (Player p : players) {
			models.add(p.getModel());
		}
		FileOutputHandler.saveModels(models);
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public Ball getBall() {
		return ball;
	}

	public void setBall(Ball ball) {
		this.ball = ball;
	}

	public int getbScore() {
		return bScore;
	}

	public void setbScore(int bScore) {
		this.bScore = bScore;
	}

	public int getyScore() {
		return yScore;
	}

	public void setyScore(int yScore) {
		this.yScore = yScore;
	}

	public long getMatchLength() {
		return matchLength;
	}

	public void setMatchLength(long matchLength) {
		this.matchLength = matchLength;
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

}
