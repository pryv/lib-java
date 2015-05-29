package com.pryv;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class AuthWindowController {

  @FXML
  TextArea authLabel;

  @FXML
  private void initialize() {

  }

  /**
   * sets the URL in the authentication message
   *
   * @param url
   */
  public void setUrl(String url) {
    authLabel.setText("Authenticate in Browser or copy this URL in your browser:\n" + url);
  }
}
