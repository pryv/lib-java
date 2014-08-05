package com.pryv;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;

public class StreamsController {
  @FXML
  private TableView<Stream> streamsTable;
  @FXML
  private TableColumn<Stream, String> idColumn;
  @FXML
  private TableColumn<Stream, String> nameColumn;

  @FXML
  private ListView<Event> eventsList;

  @FXML
  private Label idLabel;
  @FXML
  private Label nameLabel;
  @FXML
  private Label parentIdLabel;
  @FXML
  private Label childrenLabel;
  @FXML
  private Label singleActivityLabel;
  @FXML
  private Label clientDataLabel;
  @FXML
  private Label createdLabel;
  @FXML
  private Label createdByLabel;

  // Reference to the main application
  private ExampleApp exampleApp;

  /**
   * The constructor. The constructor is called before the initialize() method.
   */
  public StreamsController() {
  }

  /**
   * Initializes the controller class. This method is automatically called after
   * the fxml file has been loaded.
   */
  @FXML
  private void initialize() {
    // Initialize the person table
    idColumn.setCellValueFactory(new PropertyValueFactory<Stream, String>("id"));
    nameColumn.setCellValueFactory(new PropertyValueFactory<Stream, String>("name"));

    streamsTable.getSelectionModel().selectedItemProperty()
      .addListener(new ChangeListener<Stream>() {

        public void changed(ObservableValue<? extends Stream> observable, Stream oldValue,
          Stream newValue) {
          showStreamDetails(newValue);
        }
      });
  }

  private void showStreamDetails(Stream stream) {
    idLabel.setText(stream.getId());
    nameLabel.setText(stream.getName());
    parentIdLabel.setText(stream.getParentId());
    String childrenIDs = "";
    for (Stream child : stream.getChildren()) {
      childrenIDs = childrenIDs + child.getId() + ", ";
    }
    // childrenIDs.substring(0, childrenIDs.length() - 2);
    childrenLabel.setText(childrenIDs);
    singleActivityLabel.setText(String.valueOf(stream.getSingleActivity()));
    if (stream.getClientData() != null) {
      clientDataLabel.setText(stream.getClientData().keySet().toString());
    }
    createdLabel.setText(String.valueOf(stream.getCreated()));
    createdByLabel.setText(stream.getCreatedBy());

  }

  /**
   * Is called by the main application to give a reference back to itself.
   *
   * @param mainApp
   */
  public void setMainApp(ExampleApp mainApp) {
    this.exampleApp = mainApp;

    // Add observable list data to the table
    streamsTable.setItems(mainApp.getStreamsData());
  }
}
