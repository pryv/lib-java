package com.pryv;

import java.util.ArrayList;
import java.util.List;
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

  // fields with @FXML annotations correspond to Views names in .fxml files. If
  // they are refactored, their id need to be manually changed in the .fxml
  @FXML
  private TreeView<Stream> streamsTreeView;

  @FXML
  private ListView<Event> eventsListView;

  /**
   * Streams info labels
   */
  private List<Label> streamsLabels;
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
  private List<Label> eventsLabels;
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
   * the fxml file has been loaded. assigns the listeners on Streams' TreeView
   * and Events' ListView.
   */
  @FXML
  private void initialize() {

    bindEventsLabels();
    bindStreamsLabels();

    streamsTreeView.setShowRoot(false);

    // display Stream name in TreeView elements
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

    // load Stream's details on selection
    streamsTreeView.getSelectionModel().selectedItemProperty()
      .addListener(new ChangeListener<TreeItem<Stream>>() {
        public void changed(ObservableValue<? extends TreeItem<Stream>> observable,
          TreeItem<Stream> oldValue, TreeItem<Stream> newValue) {
          showStreamDetails(newValue.getValue());
        }
      });

    // display Events' id in ListView rows
    eventsListView.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {
      public ListCell<Event> call(ListView<Event> p) {
        ListCell<Event> cell = new ListCell<Event>() {
          @Override
          protected void updateItem(Event t, boolean bln) {
            super.updateItem(t, bln);
            if (t != null) {
              setText(t.getId());
            } else {
              setText("");
            }
          }
        };
        return cell;
      }
    });

    // load Event's details on selection
    eventsListView.getSelectionModel().selectedItemProperty()
      .addListener(new ChangeListener<Event>() {
        public void changed(ObservableValue<? extends Event> observable, Event oldValue,
          Event newValue) {
          if (newValue != null) {
            showEventDetails(newValue);
          }
        }
      });
  }

  /**
   * display Events details in labels
   *
   * @param event
   */
  private void showEventDetails(Event event) {

    clearEventsLabels();
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

  /**
   *
   * Streams
   *
   */

  /**
   * set rootStream as root TreeItem of TreeView
   *
   * @param rootStream
   */
  public void showStreams(TreeItem<Stream> rootStream) {
    logger.log("AppController: treeView created");
    streamsTreeView.setRoot(rootStream);
  }

  /**
   * load stream's details in label and load stream's Events
   *
   * @param stream
   */
  private void showStreamDetails(Stream stream) {

    clearStreamLabels();

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
    exampleApp.getEventsForStreamId(stream.getId());
    clearEventsLabels();
  }

  /**
   * clear Labels
   */

  private void clearEventsLabels() {
    for (Label label : eventsLabels) {
      label.setText("empty");
    }
  }

  private void clearStreamLabels() {
    for (Label label : streamsLabels) {
      label.setText("empty");
    }
  }

  private void bindEventsLabels() {
    eventsLabels = new ArrayList<Label>();
    eventsLabels.add(eAttachmentsLabel);
    eventsLabels.add(eClientDataLabel);
    eventsLabels.add(eContentLabel);
    eventsLabels.add(eCreatedByLabel);
    eventsLabels.add(eCreatedLabel);
    eventsLabels.add(eDescriptionLabel);
    eventsLabels.add(eDurationLabel);
    eventsLabels.add(eIdLabel);
    eventsLabels.add(eModifiedByLabel);
    eventsLabels.add(eModifiedLabel);
    eventsLabels.add(eReferencesLabel);
    eventsLabels.add(eStreamIdLabel);
    eventsLabels.add(eTagsLabel);
    eventsLabels.add(eTempRefIdLabel);
    eventsLabels.add(eTimeLabel);
    eventsLabels.add(eTrashedLabel);
    eventsLabels.add(eTypeLabel);
  }

  private void bindStreamsLabels() {
    streamsLabels = new ArrayList<Label>();
    streamsLabels.add(idLabel);
    streamsLabels.add(nameLabel);
    streamsLabels.add(parentIdLabel);
    streamsLabels.add(childrenLabel);
    streamsLabels.add(singleActivityLabel);
    streamsLabels.add(clientDataLabel);
    streamsLabels.add(createdByLabel);
    streamsLabels.add(createdLabel);
  }

  /**
   * Is called by the main application to give a reference back to itself.
   *
   * @param mainApp
   */
  public void setMainApp(ExampleApp mainApp) {
    this.exampleApp = mainApp;

    // Add observable list data to the table
    eventsListView.setItems(exampleApp.getEventsList());
  }

  /**
   * format client data to printable.
   *
   * @param clientData
   *          the client data to format
   * @return client data in readable form as a String.
   */
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

  public ListView<Event> getEventsListView() {
    return eventsListView;
  }
}
