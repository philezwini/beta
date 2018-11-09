package ga;

import java.util.ArrayList;
import java.util.Random;

import Entity.Player;
import exception.GAException;
import model.DTree;
import model.Node;

public class Selector {
	private static final int TEAM_SIZE = 10; // The allowed size for each team.
	private static final double MUT_STEP_SIZE = 0.01; // Mutation step size.

	private static int maxNumGen;

	private static int currentGen; // The generation counter.
	private static int overallBest; // The overall best fitness.
	private static double mutationRate = 0.5; // The probability that mutation will happen.

	public static void initialize(int maxNumGen) {
		overallBest = (int) Double.NEGATIVE_INFINITY; // Make sure the overall best fitness is as low as possible.
		currentGen = 0;// Initialize the generation counter to 0;
		setCurrentGen(0);
		Selector.maxNumGen = maxNumGen;
	}

	public static boolean monitor(ArrayList<Player> yTeam, ArrayList<Player> bTeam) throws GAException {
		if (currentGen == maxNumGen) {
			currentGen = 0; // Reset generation counter.
			return false;
		}

		if (bTeam.size() != TEAM_SIZE || yTeam.size() != TEAM_SIZE)
			throw new GAException("Invalid team sizes: (" + bTeam.size() + ", " + yTeam.size() + ")");

		// randSelect() and fittestMembers() are both used to perform tournament
		// selection. 7 random players are chosen from each team and the best 5 are
		// selected form reproduction.
		ArrayList<Player> ySubset = randSelect(yTeam);
		ArrayList<Player> bSubset = randSelect(bTeam);

		// Select the fittest players from the randomly generated subsets.
		ArrayList<Player> yf = fittestMembers(ySubset);
		ArrayList<Player> bf = fittestMembers(bSubset);

		// Take the players not selected by fittestMembers() back to their original
		// lists.
		while (ySubset.size() > 0)
			yTeam.add(ySubset.remove(0));

		while (bSubset.size() > 0)
			bTeam.add(bSubset.remove(0));

		try {
			evolve(yTeam, bTeam, yf, bf);
		} catch (Exception e) {
			e.printStackTrace();
		}

		newGen();
		return true;
	}

	private static ArrayList<Player> randSelect(ArrayList<Player> team) {
		ArrayList<Player> subset = new ArrayList<>();

		Random r = new Random();
		int roll = -1;
		while (subset.size() != 7) {
			for (int j = 0; j < team.size(); j++) {
				roll = r.nextInt(2);
				if (roll == 1) {
					subset.add(team.remove(j));
					break;
				}
			}
		}
		return subset;
	}

	private static void evolve(ArrayList<Player> yTeam, ArrayList<Player> bTeam, ArrayList<Player> yf,
			ArrayList<Player> bf) throws Exception {

		ArrayList<DTree> newYTeam = reproduce(yf);
		ArrayList<DTree> newBTeam = reproduce(bf);

		for (Player p : yTeam) {
			p.reset();

			DTree m = newYTeam.remove(0);
			m.setpId(p.getId());
			m.setPStartPos(p.getStartPos());
			m.setpPos(p.getDefPos());
			m.setpTeam(p.getTeam());
			p.setModel(m);
		}

		for (Player p : yf) {
			p.setScore(0);
			yTeam.add(p);
		}

		for (Player p : bTeam) {
			p.reset();
			DTree m = newBTeam.remove(0);
			m.setpId(p.getId());
			m.setPStartPos(p.getStartPos());
			m.setpTeam(p.getTeam());
			p.setModel(m);
		}

		for (Player p : bf) {
			p.setScore(0);
			bTeam.add(p);
		}

		if (newYTeam.size() != 0 || newBTeam.size() != 0)
			throw new Exception("More decision trees than anticipated the anticipated amount: " + newYTeam.size());
	}

	private static ArrayList<DTree> reproduce(ArrayList<Player> parents) {
		ArrayList<DTree> offSpring = new ArrayList<>();
		//boolean newOverallBest = false; // If we have a new overall best we need to increase the mutation rate.

		// --The following calculation ensures that the resulting offspring, when added
		// to the number of parents, will always sum up to TEAM_SIZE. --//
		int numOffspring = TEAM_SIZE - parents.size();
		int l = parents.size();

		/*for (Player p : parents) {
			if (p.getScore() > overallBest) {
				System.out.println("new overall best found.");
				newOverallBest = true;
				overallBest = p.getScore(); // Update the overall best fitness.
			}
		}

		if (newOverallBest) {
			if (mutationRate < 1) {
				mutationRate += MUT_STEP_SIZE; // Increase the mutation rate because we are still exploring the search
												// space.
				System.out.println("New increased mutation rate: " + mutationRate);
			}

			if (mutationRate > 1) {
				mutationRate = 1;
			}

		} else {
			if (mutationRate > 0) {
				mutationRate -= MUT_STEP_SIZE;// Decrease the mutation rate because we are converging.
				System.out.println("New decreased mutation rate: " + mutationRate);

			}

			if (mutationRate < 0) {
				mutationRate = 0;
			}
		}*/

		for (int i = 0; i < numOffspring; i++) {
			Player parent1 = parents.get(i % l);
			Player parent2 = parents.get((i + 1) % l);
			DTree o = crossover(parent1.getModel(), parent2.getModel());

			// double mutChance = new Random().nextDouble();
			// if (mutChance < mutationRate)
			mutate(o);

			offSpring.add(o);
		}
		return offSpring;
	}

	private static DTree crossover(DTree parent1, DTree parent2) {
		Node root = new Node(null, null, null, parent1.root().getState(), null);
		Node lChild = new Node(root, null, null, null, null);
		Node rChild = new Node(root, null, null, null, null);
		root.setYes(lChild);
		root.setNo(rChild);
		buildSubTree(lChild, parent1.root().getLeftChild());
		buildSubTree(rChild, parent2.root().getRightChild());

		DTree offSpring = new DTree();
		offSpring.setRoot(root);
		return offSpring;
	}

	private static void buildSubTree(Node oNode, Node pNode) {
		if (pNode.isLeaf()) {
			oNode.setAction(pNode.getAction());
			return;
		}

		oNode.setState(pNode.getState());
		Node lChild = new Node(oNode, null, null, null, null);
		Node rChild = new Node(oNode, null, null, null, null);
		oNode.setYes(lChild);
		oNode.setNo(rChild);

		buildSubTree(lChild, pNode.getLeftChild());
		buildSubTree(rChild, pNode.getRightChild());
	}

	private static void mutate(DTree model) {
		Node cursor = model.root();
		int roll = new Random().nextInt(2);
		if (roll == 1)
			walk(cursor.getLeftChild());
		else
			walk(cursor.getRightChild());
	}

	private static void walk(Node cursor) {
		Random r = new Random();
		int roll = r.nextInt(2);

		if (cursor.isLeaf()) {
			if (roll == 1) {
				// We will mutate the node.
				cursor.setAction(DTree.randMove()); // Simply mutate the value inside of the node.

				/*
				 * roll = r.nextInt(2); if (roll == 0) cursor.setAction(DTree.randMove()); //
				 * Simply mutate the value inside of the node. else if (roll == 1)
				 * mutReplaceWithSubTree(cursor);// We replace this entire subtree with a //
				 * randomly generated subtree.
				 */
			}
			return;
		}

		if (roll == 1) {
			// We will mmutate the node.
			cursor.setState(DTree.randState());// Simply mutate the value inside of the node.

			/*
			 * roll = r.nextInt(2); if (roll == 0) cursor.setState(DTree.randState());//
			 * Simply mutate the value inside of the node. else if (roll == 1)
			 * mutReplaceWithSubTree(cursor);// We replace this entire subtree with a
			 * randomly generated subtree.
			 */
		}

		// First check to ensure that cursor still has children.
		if (cursor.getLeftChild() != null && cursor.getRightChild() != null) {
			roll = r.nextInt(2);
			if (roll == 0)
				walk(cursor.getLeftChild());
			else
				walk(cursor.getRightChild());
		}
	}

	/*
	 * private static void mutReplaceWithNode(Node cursor) { if
	 * (cursor.equals(cursor.getParent().getYes())) { Node termNode = new
	 * Node(cursor.getParent(), null, null, null, DTree.randMove());
	 * termNode.getParent().setYes(termNode);
	 * 
	 * cursor.setParent(null); cursor.setYes(null); cursor.setNo(null); cursor =
	 * null; }
	 * 
	 * else { Node termNode = new Node(cursor.getParent(), null, null, null,
	 * DTree.randMove()); termNode.getParent().setNo(termNode);
	 * 
	 * cursor.setParent(null); cursor.setYes(null); cursor.setNo(null); cursor =
	 * null; } }
	 */

	private static void mutReplaceWithSubTree(Node cursor) {
		Node sTree = randSubTree(cursor.getParent());
		if (cursor.equals(cursor.getParent().getYes()))
			cursor.getParent().setYes(sTree);
		else
			cursor.getParent().setNo(sTree);

		cursor.setParent(null);
		cursor.setYes(null);
		cursor.setNo(null);
		cursor = null;
	}

	private static Node randSubTree(Node parent) {
		Node root = new Node(parent, null, null, DTree.randState(), null);
		Node yes = new Node(root, null, null, null, DTree.randMove());
		Node no = new Node(root, null, null, null, DTree.randMove());
		root.setYes(yes);
		root.setNo(no);
		return root;
	}

	private static ArrayList<Player> fittestMembers(ArrayList<Player> team) {
		ArrayList<Player> fittest = new ArrayList<>();
		int max = (int) Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		int numIter = TEAM_SIZE / 2;
		for (int i = 0; i < numIter; i++) {
			for (int j = 0; j < team.size(); j++) {
				if (team.get(j).getScore() > max) {
					max = team.get(j).getScore();
					maxIndex = j;
				}
			}
			fittest.add(team.remove(maxIndex));
			maxIndex = -1;
			max = (int) Double.NEGATIVE_INFINITY;
		}
		return fittest;
	}

	private static void newGen() {
		setCurrentGen(getCurrentGen() + 1);
	}

	public static int getCurrentGen() {
		return currentGen;
	}

	private static void setCurrentGen(int currentGen) {
		Selector.currentGen = currentGen;
	}

	public static int getMaxNumGen() {
		return maxNumGen;
	}

	public static void setNumGen(int maxNumGen) {
		Selector.maxNumGen = maxNumGen;
	}
}
