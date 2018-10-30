package ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Entity.Ball;
import Entity.Entity;
import Entity.Player;
import ga.Selector;
import game.GameState;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

//This is a helper class for the Simulation FXML file.
public class SimulationController extends Controller {
	private static Ball ball;
	private static ArrayList<Player> players;
	private GameState gState;
	private boolean train;
	private static ScheduledExecutorService es;

	// Control variables for existing FXML nodes.
	@FXML
	private BorderPane rootPane;
	private static BorderPane s_rootPane;

	@FXML
	private AnchorPane aPane;

	@FXML
	private ImageView fieldView;

	@FXML
	private ToggleButton tTrain;

	// The statitc fields are used to control the static fxml variables in a static
	// way.
	@FXML
	private TextField tYScore, tBScore, tGen, tMaxNumGen;
	private static TextField s_tYScore, s_tBScore, s_tGen;

	@FXML
	private LineChart<String, Double> chart;
	private static LineChart<String, Double> s_chart;

	// Function for handing when the user clicks the "Close" menu item.
	@FXML
	private void closeMenuItemClick() {
		Stage s = (Stage) rootPane.getScene().getWindow();
		closeWindow(s, true); // Call parent function that handles program termination.
	}

	// -- This method shuts down the executor service after the currently running
	// thread finishes. --//
	public static void stopGame(int status) {
		if (status == 0) {
			// Stop execution even though stopping conditions of the GA have not been met.
			es.shutdown();
		} else {
			// Stop execution and notify the user that the stopping conditions of the GA
			// have been met.
			es.shutdown();
			Platform.runLater(() -> {
				Stage s = (Stage) s_rootPane.getScene().getWindow();
				makeCustomAlert(s, "Training Complete.", "The stopping conditions of the algorithm have been met.");
			});
		}
	}

	@FXML
	private void btnStartClick() {
		startSim();
	}

	@FXML
	private void btnStopClick() {
		stopGame(0);
	}

	@FXML
	private void btnSaveClick() {
		gState.saveState();
	}

	@FXML
	private void btnGraphicsClick() {
		if (tTrain.isSelected()) {
			System.out.println("Train Mode On");
			train = true;
			Entity.enableView(false);
		} else {
			System.out.println("Train Mode Off");
			train = false;
			Entity.enableView(true);
		}
	}

	private void startSim() {
		int maxNumGen = Integer.parseInt(tMaxNumGen.getText());
		Selector.setNumGen(maxNumGen);

		es = Executors.newScheduledThreadPool(1);
		if (train) {
			// The intervals between two executions must be shorter.
			es.scheduleAtFixedRate(gState, 0, 500, TimeUnit.MICROSECONDS);
		} else {
			// The intervals should be longer so that the simulation can be watched..
			es.scheduleAtFixedRate(gState, 0, 20, TimeUnit.MILLISECONDS);
		}
	}

	@FXML
	void btnClickClick(MouseEvent e) {
		System.out.println("x = " + e.getSceneX() + ", y = " + e.getSceneY());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// -- Set the initial values for the text boxes. --//
		tYScore.setText(0 + "");
		tBScore.setText(0 + "");
		tGen.setText(0 + "");
		tMaxNumGen.setText(200 + "");

		// --Make the static variables point to the same place as the original ones.--//
		s_tYScore = tYScore;
		s_tBScore = tBScore;
		s_tGen = tGen;
		s_chart = chart;
		s_rootPane = rootPane;

		// -- Create two series for the yellow and black team. --//
		XYChart.Series<String, Double> ySeries = new Series<String, Double>();
		XYChart.Series<String, Double> bSeries = new Series<String, Double>();
		ySeries.setName("Team A");
		bSeries.setName("Team B");
		s_chart.getData().add(ySeries);
		s_chart.getData().add(bSeries);

		// --Initialise the program with the following parameters. --//
		Selector.initialize(Integer.parseInt(tMaxNumGen.getText()));
		gState = new GameState(1000);
		train = false;
		Entity.enableView(true);

		// -- Add the players and the ball to the AnchorPaane. --//
		players = gState.getPlayers();
		ball = gState.getBall();

		for (Player p : players) {
			aPane.getChildren().add(p.getView());
		}
		aPane.getChildren().add(ball.getView());
	}

	public static void updateScore(int yScore, int bScore) {
		Platform.runLater(() -> {
			s_tYScore.setText(yScore + "");
			s_tBScore.setText(bScore + "");
		});
	}

	public static void updateGeneration(String generation, double yTeamAvgFitness, double bTeamAvgFitness) {
		XYChart.Data<String, Double> yData = new Data<String, Double>(generation, yTeamAvgFitness);
		XYChart.Data<String, Double> bData = new Data<String, Double>(generation, bTeamAvgFitness);

		ObservableList<XYChart.Data<String, Double>> yDataPoints = s_chart.getData().get(0).getData();
		ObservableList<XYChart.Data<String, Double>> bDataPoints = s_chart.getData().get(1).getData();

		Platform.runLater(() -> {
			s_tGen.setText(generation + "");
			s_chart.getData().get(0).getData().add(yData);
			s_chart.getData().get(1).getData().add(bData);

			// Check if it is time to move the axis to the right.
			if (yDataPoints.size() > 20)
				yDataPoints.remove(0);

			if (bDataPoints.size() > 20)
				bDataPoints.remove(0);
		});
	}
}
