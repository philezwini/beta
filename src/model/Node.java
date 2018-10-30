package model;

import java.io.Serializable;

public class Node implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private State state;
	private Action action;
	private Node parent;
	private Node yes;
	private Node no;

	public Node(Node parent, Node yes, Node no, State state, Action action) {
		super();
		this.setParent(parent);
		this.setYes(yes);
		this.setNo(no);
		this.state = state;
		this.setAction(action);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public boolean isLeaf() {
		return yes == null && no == null;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getYes() {
		return yes;
	}

	public void setYes(Node yes) {
		this.yes = yes;
	}

	public Node getNo() {
		return no;
	}

	public void setNo(Node no) {
		this.no = no;
	}

	public Node getLeftChild() {
		return yes;
	}

	public Node getRightChild() {
		return no;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
}
