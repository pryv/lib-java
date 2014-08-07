package com.pryv;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

public class AppController {

  @FXML
  private TreeView<Stream> streamsTreeView;

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

  private Logger logger = Logger.getInstance();

  /**
   * The constructor. The constructor is called before the initialize() method.
   */
  public AppController() {
  }

  /**
   * Initializes the controller class. This method is automatically called after
   * the fxml file has been loaded.
   */
  @FXML
  private void initialize() {
    streamsTreeView.setShowRoot(false);
    streamsTreeView.setCellFactory(new Callback<TreeView<Stream>, TreeCell<Stream>>() {


      public TreeCell<Stream> call(TreeView<Stream> p) {

        TreeCell<Stream> cell = new TreeCell<Stream>() {

          @Override
          protected void updateItem(Stream t, boolean bln) {
            super.updateItem(t, bln);
            if (t != null) {
              setText(t.getName());
            }
          }

        };

        return cell;
      }
    });

    eventsList.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {

      public ListCell<Event> call(ListView<Event> p) {

        ListCell<Event> cell = new ListCell<Event>() {

          @Override
          protected void updateItem(Event t, boolean bln) {
            super.updateItem(t, bln);
            if (t != null) {
              setText(t.getId());
            }
          }

        };

        return cell;
      }
    });

    streamsTreeView.getSelectionModel().selectedItemProperty()
      .addListener(new ChangeListener<TreeItem<Stream>>() {

        public void changed(ObservableValue<? extends TreeItem<Stream>> observable,
          TreeItem<Stream> oldValue, TreeItem<Stream> newValue) {
          showStreamDetails(newValue.getValue());

        }
      });
  }

  private void showStreamDetails(Stream stream) {

    // stream details
    idLabel.setText(stream.getId());
    nameLabel.setText(stream.getName());
    parentIdLabel.setText(stream.getParentId());
    StringBuilder sb = new StringBuilder();
    String separator = "";
    for (Stream child : stream.getChildren()) {
      sb.append(separator);
      sb.append(child.getId());
      separator = ", ";
    }
    String childrenIDs = sb.toString();
    childrenLabel.setText(childrenIDs);
    singleActivityLabel.setText(String.valueOf(stream.getSingleActivity()));
    if (stream.getClientData() != null) {
      sb.setLength(0);
      separator = "";
      for (String key : stream.getClientData().keySet()) {
        sb.append(separator);
        separator = ", ";
        sb.append(key + ": " + stream.getClientData().get(key));
      }
      clientDataLabel.setText(sb.toString());
    }
    createdLabel.setText(String.valueOf(stream.getCreated()));
    createdByLabel.setText(stream.getCreatedBy());

    // fetch events
    exampleApp.getEvents(stream.getId());
  }

  /**
   * Is called by the main application to give a reference back to itself.
   *
   * @param mainApp
   */
  public void setMainApp(ExampleApp mainApp) {
    this.exampleApp = mainApp;

    // Add observable list data to the table
    eventsList.setItems(exampleApp.getEventsList());

  }

  public void showStreams(TreeItem<Stream> rootStream) {
    logger.log("AppController: treeView created");
    streamsTreeView.setRoot(rootStream);
    for (TreeItem<Stream> strItem : rootStream.getChildren()) {
      logger.log("AppController: nodes: " + strItem.getValue().getId());
    }
  }
}
