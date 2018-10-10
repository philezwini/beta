package ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;

import Entity.Ball;
import Entity.Entity;
import Entity.Player;
import ga.Selector;
import game.GameState;
import javafx.application.Platform;
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
	// Control variables for existing FXML nodes.
	@FXML
	private BorderPane rootPane;

	@FXML
	private AnchorPane aPane;

	@FXML
	private ImageView fieldView;

	@FXML
	private ToggleButton tGraphics;

	@FXML
	private TextField tYScore, tBScore, tGen;
	private static TextField s_tYScore, s_tBScore, s_tGen;

	@FXML
	private LineChart<String, Double> chart; // Chart for visualizing the average fitness over time.
	private static LineChart<String, Double> s_chart;

	// Function for handing when the user clicks the "Close" menu item.
	@FXML
	private void closeMenuItemClick() {
		// saveModels();
		Stage s = (Stage) rootPane.getScene().getWindow();
		closeWindow(s, true); // Call parent function that handles program // termination.
	}

	private void stopGame() {
		gState.cancel();
	}

	@FXML
	private void btnStartClick() {
		startSim();
	}

	@FXML
	private void btnStopClick() {
		stopGame();
	}

	@FXML
	private void btnSaveClick() {
		gState.saveState();
	}

	@FXML
	private void btnGraphicsClick() {
		if (tGraphics.isSelected()) {
			System.out.println("Graphics. On");
			Entity.enableView(true);
		} else {
			System.out.println("Graphics Off");
			Entity.enableView(false);
		}
	}

	private void startSim() {
		// Schedule the GameState thread to run in fixed 20millisecond intervals.
		new Timer().scheduleAtFixedRate(gState, 0, 20);
	}

	@FXML
	void btnClickClick(MouseEvent e) {
		System.out.println("x = " + e.getSceneX() + ", y = " + e.getSceneY());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Selector.initialize();
		gState = new GameState(1000);

		// --Make the static variables point to the same place as the original ones.--//
		s_tYScore = tYScore;
		s_tBScore = tBScore;
		s_tGen = tGen;
		s_chart = chart;

		s_tYScore.setText(gState.getyScore() + "");
		s_tBScore.setText(gState.getbScore() + "");
		s_tGen.setText(gState.getGeneration() + "");

		XYChart.Series<String, Double> ySeries = new Series<String, Double>();
		XYChart.Series<String, Double> bSeries = new Series<String, Double>();
		ySeries.setName("Team A");
		bSeries.setName("Team B");

		s_chart.getData().add(ySeries);
		s_chart.getData().add(bSeries);

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

		Platform.runLater(() -> {
			s_tGen.setText(generation + "");
			s_chart.getData().get(0).getData().add(yData);
			s_chart.getData().get(1).getData().add(bData);
		});
	}
}
