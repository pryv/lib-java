package com.pryv;

import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.pryv.model.Event;
import com.pryv.model.Stream;
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
   * details
   */
  @FXML
  Label titleLabel;
  @FXML
  Label idLabel;

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
  @FXML
  ComboBox<Stream> parentComboBox;
  @FXML
  ComboBox<Boolean> singleActivityComboBox;

  // Reference to the main application
  private ExampleApp exampleApp;

  private Logger logger = Logger.getInstance();

  private Mode mode;

  private Event editedEvent;
  private Stream editedStream;

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

    if (parentComboBox != null) {

      parentComboBox.setButtonCell(new ListCell<Stream>() {
        @Override
        protected void updateItem(Stream t, boolean bln) {
          super.updateItem(t, bln);
          if (t != null) {
            setText(t.getName());
          } else {
            setText(null);
          }
        }
      });

      parentComboBox.setCellFactory(new Callback<ListView<Stream>, ListCell<Stream>>() {
        public ListCell<Stream> call(ListView<Stream> param) {
          final ListCell<Stream> cell = new ListCell<Stream>() {

            @Override
            public void updateItem(Stream item, boolean empty) {
              super.updateItem(item, empty);
              if (item != null) {
                setText(item.getName());
              } else {
                setText(null);
              }
            }
          };
          return cell;
        }
      });
    }

    if (singleActivityComboBox != null) {
      ObservableList<Boolean> saOptions = FXCollections.observableArrayList(true, false);
      singleActivityComboBox.setItems(saOptions);
    }

    okButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        if (mode.equals(Mode.CREATE)) {
          if (editedEvent != null) {
            createEvent();
          } else if (editedStream != null) {
            createStream();
          }
        } else if (mode.equals(Mode.EDIT)) {
          if (editedEvent != null) {
            updateEvent();
          } else if (editedStream != null) {
            updateStream();
          }
        } else {
          // not possible
        }
        closeWindow();
      }
    });

    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // close
        closeWindow();
      }
    });
  }

  /**
   * loads the streams to display in the parent stream combo box.
   *
   * @param streams
   */
  public void loadParentStreams(Collection<Stream> streams) {
    parentComboBox.getItems().addAll(streams);
  }

  private void createEvent() {
    exampleApp.createEvent(editedEvent);
  }

  private void createStream() {
    editedStream.setSingleActivity(singleActivityComboBox.getValue());
    exampleApp.createStream(editedStream);
  }

  private void updateEvent() {
    exampleApp.updateEvent(editedEvent);

  }

  private void updateStream() {
    editedStream.setName(streamNameTextField.getText());
    editedStream.setParentId(parentComboBox.getPromptText());
    exampleApp.updateStream(editedStream);
  }

  private void closeWindow() {
    Stage stage = (Stage) cancelButton.getScene().getWindow();
    stage.close();
  }

  public void setMainApp(ExampleApp app) {
    exampleApp = app;
  }

  public void setMode(Mode action) {
    mode = action;
    if (mode.equals(Mode.CREATE)) {
      titleLabel.setText("Create ");
    } else {
      titleLabel.setText("Update ");
    }
  }

  public void setEvent(Event event) {
    titleLabel.setText(titleLabel.getText() + "Event:");
    editedEvent = event;
    if (event != null) {
      idLabel.setText(event.getId());
    }
  }

  public void setStream(Stream stream) {
    titleLabel.setText(titleLabel.getText() + "Stream:");
    editedStream = stream;
    if (stream != null) {
      idLabel.setText(stream.getId());
      streamNameTextField.setText(stream.getName());
    }
  }

  public static enum Mode {
    CREATE, EDIT
  };
}
