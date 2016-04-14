package com.pryv;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import com.pryv.model.Stream;
import com.pryv.utils.Logger;

/**
 * Controller for the DeleteStreamDialog
 *
 * @author ik
 *
 */
public class DeleteDialogController {

  @FXML
  private Label deleteMessage;
  @FXML
  private CheckBox deleteMergeCheckBox;
  @FXML
  private Button deleteYesButton;
  @FXML
  private Button deleteNoButton;

  // Reference to the main application
  private ExampleApp exampleApp;

  private Logger logger = Logger.getInstance();

  private Stream streamToDelete;

  /**
   * The constructor. The constructor is called before the initialize() method.
   */
  public DeleteDialogController() {
  }

  /**
   * Initializes the controller class. This method is automatically called after
   * the fxml file has been loaded.
   */
  @FXML
  private void initialize() {

    deleteYesButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        if (deleteMergeCheckBox.isSelected()) {
          exampleApp.deleteStream(streamToDelete, true);
        } else {
          exampleApp.deleteStream(streamToDelete, false);
        }
        closeWindow();
      }
    });

    deleteNoButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        closeWindow();
      }
    });

  }

  private void closeWindow() {
    Stage stage = (Stage) deleteYesButton.getScene().getWindow();
    stage.close();
  }

  public void setMainApp(ExampleApp app) {
    exampleApp = app;
  }

  public void setStream(Stream stream) {
    deleteMessage.setText("Do you really want to delete this stream: \'" + stream.getName() + "\'");
    streamToDelete = stream;
  }
}
