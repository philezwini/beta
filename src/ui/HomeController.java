package ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HomeController extends Controller {

	@FXML
	BorderPane rootPane;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	// Function for handling when the user clicks the "Credits" menu.
	@FXML
	private void creditsMenuItemClick() {
		String credits = "Soccer field image obtained for free from: https://www.vecteezy.com/\n\n";
		credits += "Yellow soccer jersey image obtained for free from: https://pngtree.com/free-icons/soccer jersey\n\n";
		credits += "Black soccery jersey image obtained for free from: https://www.onlinewebfonts.com/icon/445726\n\n";
		credits += "Logo designed for free at www.canva.com\n\n";
		Stage s = (Stage) rootPane.getScene().getWindow();
		makeCustomAlert(s, "Credits", credits);
	}

	@FXML
	private void startMenuItemClick() {
		try {
			Parent parent = FXMLLoader.load(getClass().getResource("Simulation.fxml"));
			Stage s = (Stage)rootPane.getScene().getWindow();
			s.setScene(new Scene(parent));
			s.setTitle("StratFinder");
			s.setOnCloseRequest(e -> {
				e.consume(); // Consume the event so we can handle it manually.
				Controller.closeWindow(s, true);
			});
			s.setMaximized(true);
			s.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Function for handling when the user clicks the "Close" menu item.
	@FXML
	private void closeMenuItemClick() {
		Stage s = (Stage) rootPane.getScene().getWindow();
		closeWindow(s, true); // Call parent function that handles program termination.
	}
}
