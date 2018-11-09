package model;

import java.io.Serializable;
import java.util.Random;

import Entity.Position;
import Entity.Team;
import fileIO.FileOutputHandler;
import physics.Coordinate;

public class DTree implements Serializable {
	private static final long serialVersionUID = 1L;

	private Node root;

	private int depth, counter, pId;
	private Coordinate pStartPos;
	private Team pTeam;
	private Position pPos;

	public DTree(int depth) {
		super();
		this.depth = depth;
		root = null;

		counter = 0;
		pId = 0;
		initialize();
	}

	public DTree() {
		super();
		this.depth = 0;
		root = null;

		counter = 0;
		pId = 0;
	}

	public void initialize() {
		root = randInt(null); // The root is an internal node without a parent.
		grow(root, 0);
	}

	private void grow(Node node, int currentDepth) {
		if (currentDepth == depth - 1) {
			try {
				addLeftChild(node, randLeaf(node));
				addRightChild(node, randLeaf(node));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		try {
			addLeftChild(node, randInt(node));
			addRightChild(node, randInt(node));
		} catch (Exception e) {
			e.printStackTrace();
		}

		grow(node.getLeftChild(), currentDepth + 1);
		grow(node.getRightChild(), currentDepth + 1);
	}

	public static Node randInt(Node parent) {
		return new Node(parent, null, null, randState(), null);
	}

	private void addRightChild(Node node, Node no) throws Exception {
		if (node.getNo() != null)
			throw new Exception("Right child already exists.");

		node.setNo(no);
		no.setParent(node);
	}

	private void addLeftChild(Node node, Node yes) throws Exception {
		if (node.getYes() != null)
			throw new Exception("Left child already exists.");

		node.setYes(yes);
		yes.setParent(node);
	}

	public static Node randLeaf(Node parent) {
		return new Node(parent, null, null, null, randMove());
	}

	public static Action randMove() {
		int roll = new Random().nextInt(8);
		switch (roll) {
		case 0:
			return Action.MOVE_S;
		case 1:
			return Action.MOVE_SE;
		case 2:
			return Action.MOVE_SW;
		case 3:
			return Action.MOVE_E;
		case 4:
			return Action.MOVE_W;
		case 5:
			return Action.MOVE_NE;
		case 6:
			return Action.MOVE_NW;
		case 7:
			return Action.MOVE_N;
		/*case 8:
			return Action.KICK_S;
		case 9:
			return Action.KICK_SE;
		case 10:
			return Action.KICK_SW;
		case 11:
			return Action.KICK_E;
		case 12:
			return Action.KICK_W;
		case 13:
			return Action.KICK_NE;
		case 14:
			return Action.KICK_NW;
		case 15:
			return Action.KICK_N;
		case 16:
			return Action.MOVE_TO_POSS;*/
		}
		return null;
	}

	public static State randState() {
		int roll = new Random().nextInt(19);
		switch (roll) {
		case 0:
			return State.BALL_N;
		case 1:
			return State.BALL_S;
		case 2:
			return State.BALL_W;
		case 3:
			return State.BALL_E;
		case 4:
			return State.OPP_GOALS_CLOSE;
		case 5:
			return State.OWN_GOALS_CLOSE;
		case 6:
			return State.TOUCHLINE_N;
		case 7:
			return State.TOUCHLINE_S;
		case 8:
			return State.TOUCHLINE_W;
		case 9:
			return State.TOUCHLINE_E;
		case 10:
			return State.TEAM_MATE_N;
		case 11:
			return State.TEAM_MATE_S;
		case 12:
			return State.TEAM_MATE_W;
		case 13:
			return State.TEAM_MATE_E;
		case 14:
			return State.OPPONENT_N;
		case 15:
			return State.OPPONENT_S;
		case 16:
			return State.OPPONENT_W;
		case 17:
			return State.OPPONENT_E;
		case 18:
			return State.IN_POSS;
		}
		return null;
	}

	public Node root() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public DTree clone() {
		Node tempRoot = new Node(null, null, null, root.getState(), null);
		clone(tempRoot, root);
		DTree clone = new DTree();
		clone.setRoot(tempRoot);
		return clone;
	}

	private void clone(Node tempNode, Node node) {
		if (node.isLeaf())
			return;

		Node tempYes = new Node(tempNode, null, null, null, null);
		tempYes.setState(node.getLeftChild().getState());
		tempYes.setAction(node.getLeftChild().getAction());

		Node tempNo = new Node(tempNode, null, null, null, null);
		tempNo.setState(node.getRightChild().getState());
		tempNo.setAction(node.getRightChild().getAction());

		try {
			addLeftChild(tempNode, tempYes);
			addRightChild(tempNode, tempNo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		clone(tempYes, node.getLeftChild());
		clone(tempNo, node.getRightChild());
	}

	public int getDepth() {
		return depth;
	}

	public void genDebugCode(String aId) {
		String debug = "digraph DFA {\n";
		debug += "rankdir=UD;\n";
		debug += "size=\"10,5;\"\n";
		// debug += "splines=false;\n";
		debug += "node [shape = circle];\n";
		debug += "S0 -> S1 [label = \"[C: " + root.getState() + "?]\"];\n";
		counter = 1;
		debug += genStates(root, 1);
		debug += "}";
		writeToFile(debug, aId);
	}

	private String genStates(Node parent, int parentCounter) {
		String debug = "";
		if (parent.isLeaf()) {
			return debug;
		}

		debug += "S" + parentCounter + " -> S" + ++counter + " [label = \"[C: " + parent.getLeftChild().getState()
				+ ", Yes?: " + parent.getLeftChild().getAction() + "]\"];\n";

		debug += genStates(parent.getLeftChild(), counter);

		debug += "S" + parentCounter + " -> S" + ++counter + " [label = \"[C: " + parent.getRightChild().getState()
				+ ", No?: " + parent.getRightChild().getAction() + "]\"];\n";

		debug += genStates(parent.getRightChild(), counter);

		return debug;
	}

	private void writeToFile(String debugCode, String fileName) {
		FileOutputHandler.writeDebugCodeToFile(debugCode, "data/debug/" + fileName + ".dot");
	}

	public int getpId() {
		return pId;
	}

	public void setpId(int pId) {
		this.pId = pId;
	}

	public Coordinate getPStartPos() {
		return pStartPos;
	}

	public void setPStartPos(Coordinate startPos) {
		this.pStartPos = startPos;
	}

	public Team getpTeam() {
		return pTeam;
	}

	public void setpTeam(Team pTeam) {
		this.pTeam = pTeam;
	}

	public Position getpPos() {
		return pPos;
	}

	public void setpPos(Position pPos) {
		this.pPos = pPos;
	}
}
