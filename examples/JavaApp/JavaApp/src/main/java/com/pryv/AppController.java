package com.pryv;

import java.util.Map;

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

import com.pryv.api.model.Attachment;
import com.pryv.api.model.Event;
import com.pryv.api.model.Stream;
import com.pryv.utils.Logger;

import edu.emory.mathcs.backport.java.util.Arrays;

public class AppController {

  @FXML
  private TreeView<Stream> streamsTreeView;

  @FXML
  private ListView<Event> eventsList;

  /**
   * Streams info labels
   */
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

  /**
   * Events info labels
   */
  @FXML
  private Label eIdLabel;
  @FXML
  private Label eStreamIdLabel;
  @FXML
  private Label eTimeLabel;
  @FXML
  private Label eTypeLabel;
  @FXML
  private Label eCreatedLabel;
  @FXML
  private Label eCreatedByLabel;
  @FXML
  private Label eModifiedLabel;
  @FXML
  private Label eModifiedByLabel;
  @FXML
  private Label eDurationLabel;
  @FXML
  private Label eContentLabel;
  @FXML
  private Label eTagsLabel;
  @FXML
  private Label eReferencesLabel;
  @FXML
  private Label eDescriptionLabel;
  @FXML
  private Label eAttachmentsLabel;
  @FXML
  private Label eClientDataLabel;
  @FXML
  private Label eTrashedLabel;
  @FXML
  private Label eTempRefIdLabel;

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

    streamsTreeView.getSelectionModel().selectedItemProperty()
      .addListener(new ChangeListener<TreeItem<Stream>>() {

        public void changed(ObservableValue<? extends TreeItem<Stream>> observable,
          TreeItem<Stream> oldValue, TreeItem<Stream> newValue) {
          showStreamDetails(newValue.getValue());

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

    eventsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Event>() {

      public void changed(ObservableValue<? extends Event> observable, Event oldValue,
        Event newValue) {
        if (newValue != null) {
          showEventDetails(newValue);
        }
      }

    });
  }

  private String printClientData(Map<String, Object> clientData) {
    StringBuilder sb = new StringBuilder();
    if (clientData != null) {
      sb.setLength(0);
      String separator = "";
      for (String key : clientData.keySet()) {
        sb.append(separator);
        separator = ", ";
        sb.append(key + ": " + clientData.get(key));
      }

    }
    return sb.toString();
  }

  private void showEventDetails(Event event) {
    eIdLabel.setText(event.getId());
    eStreamIdLabel.setText(event.getStreamId());
    if (event.getTime() != null) {
      eTimeLabel.setText(event.getTime().toString());
    }
    eTypeLabel.setText(event.getType());
    if (event.getCreated() != null) {
      eCreatedLabel.setText(event.getCreated().toString());
    }
    eCreatedByLabel.setText(event.getCreatedBy());
    if (event.getModified() != null) {
      eModifiedLabel.setText(event.getModified().toString());
    }
    eModifiedByLabel.setText(event.getModifiedBy());
    if (event.getDuration() != null) {
      eDurationLabel.setText(event.getDuration().toString());
    }
    if (event.getContent() != null) {
      eContentLabel.setText(event.getContent().toString());
    }
    if (event.getTags() != null) {
      eTagsLabel.setText(Arrays.toString(event.getTags().toArray()));
    }
    if (event.getReferences() != null) {
      eReferencesLabel.setText(Arrays.toString(event.getReferences().toArray()));
    }
    eDescriptionLabel.setText(event.getDescription());
    if (event.getAttachments() != null) {
      StringBuilder sb = new StringBuilder();
      String sep = "";
      for (Attachment attach : event.getAttachments()) {
        sb.append(sep);
        sb.append(attach.getId());
      }
    }
    if (event.getClientData() != null) {
      eClientDataLabel.setText(printClientData(event.getClientData()));
    }
    if (event.getTrashed() != null) {
      eTrashedLabel.setText(event.getTrashed().toString());
    }
    if (event.getTempRefId() != null) {
      eTempRefIdLabel.setText(event.getTempRefId());
    }
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
    clientDataLabel.setText(printClientData(stream.getClientData()));
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
