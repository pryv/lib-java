package com.pryv;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import resources.TestCredentials;

import com.pryv.api.model.Permission;
import com.pryv.auth.AuthController;
import com.pryv.auth.AuthControllerImpl;

public class JavaApp extends Application {

  private Connection connection;
  private List<Permission> permissions;
  private String streamId1 = "pics";
  private Permission.Level perm1 = Permission.Level.contribute;
  private String defaultName1 = "ddd";
  private Permission testPermission1 = new Permission(streamId1, perm1, defaultName1);
  private String streamId2 = "vids";
  private Permission.Level perm2 = Permission.Level.read;
  private String defaultName2 = "eee";
  private Permission testPermission2 = new Permission(streamId2, perm2, defaultName2);
  private String lang = "en";
  private String returnURL = "fakeURL";

  private TextField reqAppIdTextField;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Java Example App");

    StackPane root = new StackPane();
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    Text scenetitle = new Text("Welcome");
    scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
    grid.add(scenetitle, 0, 0, 2, 1);

    Label reqAppLabel = new Label("Requesting App Id:");
    grid.add(reqAppLabel, 0, 1);

    reqAppIdTextField = new TextField();
    grid.add(reqAppIdTextField, 1, 1);

    HBox hbBtn = new HBox(10);
    Button authenticateBtn = new Button();
    authenticateBtn.setText("Authenticate");
    Button loginWithTestCredsBtn = new Button("testLogin");

    hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
    hbBtn.getChildren().add(authenticateBtn);
    hbBtn.getChildren().add(loginWithTestCredsBtn);
    grid.add(hbBtn, 1, 4);
    final Text actiontarget = new Text();
    grid.add(actiontarget, 1, 6);

    loginWithTestCredsBtn.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent arg0) {
        actiontarget.setFill(Color.FIREBRICK);
        actiontarget.setText("signing in with test credentials");

        Connection connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN);
      }
    });

    authenticateBtn.setOnAction(new EventHandler<ActionEvent>() {

      public void handle(ActionEvent event) {
        actiontarget.setFill(Color.FIREBRICK);
        actiontarget.setText("Authentication started");
        System.out.println("authenticate pressed");
        permissions = new ArrayList<Permission>();
        // permissions.add(testPermission1);
        // permissions.add(testPermission2);
        AuthController authenticator =
          new AuthControllerImpl(reqAppIdTextField.getText(), permissions, "en", null);
        authenticator.signIn();
      }
    });

    Scene scene = new Scene(grid, 600, 500);
    primaryStage.setScene(scene);
    primaryStage.show();
  }
}