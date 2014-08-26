
package com.pryv;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import com.pryv.utils.Logger;

/**
 * Controller for the Stream and Event Forms
 *
 * @author ik
 *
 */
public class FormController {


  /*
   * control buttons
   */
  @FXML
  Button okButton;
  @FXML
  Button cancelButton;

  /*
   * Event fields
   */
  @FXML
  TextField eventNameTextField;

  /*
   * Stream fields
   */
  @FXML
  TextField streamNameTextField;

  // Reference to the main application
  private ExampleApp exampleApp;

  private Logger logger = Logger.getInstance();

  private Mode mode;

  /**
   * The constructor. The constructor is called before the initialize() method.
   */
  public FormController() {
  }

  /**
   * Initializes the controller class. This method is automatically called after
   * the fxml file has been loaded. assigns the listeners on Streams' TreeView
   * and Events' ListView.
   */
  @FXML
  private void initialize() {

    okButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // booya
      }
    });

    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // close
      }
    });
  }

  public void setMainApp(ExampleApp app) {
    exampleApp = app;
  }

  public void setMode(Mode action) {

  }

  public static enum Mode {
    CREATE, EDIT
  };
}
