package com.llat.views;

import com.llat.controller.Controller;
import com.llat.database.DatabaseAdapter;
import com.llat.database.UserObject;
import com.llat.tools.ViewManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {
    private Region aboveLogoRegion = new Region();
    private Region belowLogoRegion = new Region();
    private Region belowEmailRegion = new Region();
    private Region belowPasswordFieldRegion = new Region();
    private Region belowLoginButtonRegion = new Region();
    private Controller controller;
    private AnchorPane parentPane = new AnchorPane();
    private Stage stage;
    private VBox loginVBox = new VBox();
    private Label logoImage = new Label();
    private Label emailInputLabel = new Label("Email");
    private TextField emailField = new TextField();
    private Label passwordInputLabel = new Label("Password");
    private PasswordField passwordField = new PasswordField();
    private Button loginButton = new Button("Login");
    private Button returnButton = new Button("Return to Application");

    public LoginView(Controller _controller) {
        this.controller = _controller;
        this.stage = _controller.getStage();
        // Setting VBox loginVBox properties.
        this.loginVBox.setId("loginVBox");
        this.stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.loginVBox.setMinWidth(newVal.doubleValue() * .30);
        });
        this.stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.loginVBox.setMinHeight(newVal.doubleValue());
        });
        this.stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.loginVBox.setLayoutX((newVal.doubleValue() * .50) - this.loginVBox.getMinWidth() / 2);
        });
        this.stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.loginVBox.setLayoutY((newVal.doubleValue() * .50) - this.loginVBox.getMinHeight() / 2);
        });
        // Setting Region aboveLogoRegion properties.
        this.loginVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.aboveLogoRegion.setMinHeight(newVal.doubleValue() * .05);
        });
        // Setting Label logoImage properties.
        this.logoImage.setId("loginLogoLabel");
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (this.loginVBox.getHeight() > this.loginVBox.getWidth()) {
                this.logoImage.setMinWidth(newVal.doubleValue() * .40);
                this.logoImage.setMinHeight(newVal.doubleValue() * .40);
            } else {
                this.logoImage.setMinWidth(this.loginVBox.getHeight() * .40);
                this.logoImage.setMinHeight(this.loginVBox.getHeight() * .40);
            }
        });

        this.loginVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (this.loginVBox.getHeight() > this.loginVBox.getWidth()) {
                this.logoImage.setMinWidth(this.loginVBox.getWidth() * .40);
                this.logoImage.setMinHeight(this.loginVBox.getWidth() * .40);
            } else {
                this.logoImage.setMinWidth(newVal.doubleValue() * .40);
                this.logoImage.setMinHeight(newVal.doubleValue() * .40);
            }
        });
        // Setting Region belowLogoRegion properties.
        this.loginVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            this.belowLogoRegion.setMinHeight(newVal.doubleValue() * .05);
        });
        // Setting Label emailInputLabel properties.
        this.emailInputLabel.setId("emailInputLabel");
        // Setting TextField emailField properties.
        this.emailField.setPromptText("Enter your email.");
        this.emailField.setFocusTraversable(false);
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.emailField.setMaxWidth(newVal.doubleValue() * .70);
        });
        // Setting Region belowEmailRegion properties.
        this.loginVBox.heightProperty().addListener(((obs, oldVal, newVal) -> {
            this.belowEmailRegion.setMinHeight(newVal.doubleValue() * .10);
        }));
        // Setting Label passwordInputLabel properties.
        this.passwordInputLabel.setId("passwordInputLabel");
        // Setting PasswordField passwordField properties.
        this.passwordField.setPromptText("Enter your password.");
        this.passwordField.setFocusTraversable(false);
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.passwordField.setMaxWidth(newVal.doubleValue() * .70);
        });
        // Setting Region belowPasswordFieldRegion properties.
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.belowPasswordFieldRegion.setMinHeight(newVal.doubleValue() * .10);
        });
        // Setting Button loginButton properties.
        this.loginButton.setId("loginButton");
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.loginButton.setMinWidth(newVal.doubleValue() * .30);
        });
        this.loginButton.setOnAction((event1 -> {
            DatabaseAdapter ad = new DatabaseAdapter();
            String email = this.emailField.getText();
            String pass = this.passwordField.getText();
            UserObject user = ad.Login(email, pass);
            if (!(user == null)) {
                System.out.println("User logged");
                this.controller.setUser(user);
                this.controller.changeViewTo(ViewManager.MAINAPPLICATION);
            } else {
                System.out.println("pass or email is wrong");
            }
        }));
        // Setting Region belowLoginButtonRegion properties.
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.belowLoginButtonRegion.setMinHeight(newVal.doubleValue() * .10);
        });
        // Setting Button returnButton properties.
        this.returnButton.setId("returnButton");
        this.loginVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            this.returnButton.setMinWidth(newVal.doubleValue() * .30);
        });
        this.returnButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                controller.changeViewTo(ViewManager.MAINAPPLICATION);            }
        });
        this.loginVBox.getChildren().addAll(this.aboveLogoRegion, this.logoImage, belowLogoRegion, this.emailInputLabel, this.emailField, this.belowEmailRegion, this.passwordInputLabel, this.passwordField, this.belowPasswordFieldRegion, this.loginButton, this.belowLoginButtonRegion, this.returnButton);
        // Setting AnchorPane parentPane properties.
        this.parentPane.setId("loginViewParentPane");
        // Adding children nodes to their parents nodes.
        this.parentPane.getChildren().addAll(this.loginVBox);
    }

    public Pane getParentPane() {
        return this.parentPane;
    }

}
