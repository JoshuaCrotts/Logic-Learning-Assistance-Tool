package com.llat.views;

import com.llat.controller.Controller;
import com.llat.main.Window;
import com.llat.models.localstorage.ItemObject;
import com.llat.models.localstorage.settings.SettingsAdaptor;
import com.llat.models.localstorage.settings.SettingsObject;
import com.llat.models.localstorage.settings.language.LanguageObject;
import com.llat.models.localstorage.settings.theme.ThemeObject;
import com.llat.models.localstorage.uidescription.UIObject;
import com.llat.models.localstorage.uidescription.UIObjectAdaptor;
import com.llat.models.localstorage.uidescription.settingsview.AllLanguage;
import com.llat.models.localstorage.uidescription.settingsview.AllTheme;
import com.llat.tools.ViewManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.llat.views.SettingsView.WIDTH;

public class SettingsView {

    public static final int WIDTH = 750;
    public static final int HEIGHT = 500;

    // view id
    private final static int APPEARANCE_ID = 0;
    private final static int LANGUAGE_ID = 1;
    private final static int ADVANCE_ID = 2;

    private final Controller controller;
    private final AnchorPane parentPane = new AnchorPane();
    private final Stage stage;
    private final Pane leftPane = new Pane();

    private final HBox hBox = new HBox();
    private final VBox vBox2 = new VBox();

    private final Button appearanceButton;
    private final Button languageButton;
    private final Button advanceButton;
    private final ArrayList<Button> categoryButtonsList;

    private final Pane appearancePane;
    private final Pane languagePane;
    private final Pane advancePane;

    private Stage settingsStage;

    SettingsAdaptor sa = new SettingsAdaptor();
    SettingsObject so = (SettingsObject) sa.getData();

    UIObjectAdaptor uioa = new UIObjectAdaptor();
    UIObject uio = (UIObject) uioa.getData();

    public SettingsView(Controller controller) {
        this.controller = controller;
        this.stage = this.controller.getStage();
        appearancePane = new SettingsPane(this.controller, this.controller.getUiObject().getSettingsView().getCategories().getAppearance().getLabel()).getParentPane();
        languagePane = new SettingsPane(this.controller, this.controller.getUiObject().getSettingsView().getCategories().getLanguage().getLabel()).getParentPane();
        advancePane = new SettingsPane(this.controller, this.controller.getUiObject().getSettingsView().getCategories().getAdvanced().getLabel()).getParentPane();
        appearanceButton = new Button(this.controller.getUiObject().getSettingsView().getCategories().getAppearance().getLabel());
        languageButton = new Button(this.controller.getUiObject().getSettingsView().getCategories().getLanguage().getLabel());
        advanceButton = new Button(this.controller.getUiObject().getSettingsView().getCategories().getAdvanced().getLabel());
        categoryButtonsList = new ArrayList<Button>() {
            {
                {
                    add(appearanceButton);
                    add(languageButton);
                    add(advanceButton);
                }
            }
        };
        Button cancelButton = new Button(this.controller.getUiObject().getSettingsView().getCancel());
        Button saveButton = new Button(this.controller.getUiObject().getSettingsView().getSave());
        // setting the vBox size
        this.hBox.widthProperty().addListener(((observable, oldValue, newValue) -> {
            this.hBox.setMinWidth(stage.getWidth());
            this.hBox.setMinHeight(stage.getHeight() * .01);
            this.hBox.setMaxHeight(stage.getHeight());
        }));
        // setting the buttons
        this.appearanceButton.setId("settingsCategoryOnPress");
        this.languageButton.setId("settingsCategory");
        this.advanceButton.setId("settingsCategory");
        this.leftPane.setId("leftPane");
        this.parentPane.setId("parentPane");
        this.appearancePane.setId("settingsMainPane");
        this.languagePane.setId("settingsMainPane");
        this.advancePane.setId("settingsMainPane");

        this.appearanceButton.widthProperty().addListener(((observable, oldValue, newValue) -> {
            this.appearanceButton.setMinWidth(vBox2.getLayoutBounds().getWidth());
        }));
        this.languageButton.widthProperty().addListener(((observable, oldValue, newValue) -> {
            this.languageButton.setMinWidth(vBox2.getLayoutBounds().getWidth());
        }));
        this.advanceButton.widthProperty().addListener(((observable, oldValue, newValue) -> {
            this.advanceButton.setMinWidth(vBox2.getLayoutBounds().getWidth());
        }));

        this.appearanceSetUp();
        this.languageSetUp();
        // adding the view panes to the list pane.
        List<Pane> paneList = new ArrayList<>();
        paneList.add(appearancePane);
        paneList.add(languagePane);
        paneList.add(advancePane);

        this.leftPane.heightProperty().addListener(((observable, oldValue, newValue) -> {
            this.leftPane.setMinHeight(stage.getScene().getHeight() * 0.454);
        }));
        this.hBox.getChildren().addAll(leftPane, paneList.get(APPEARANCE_ID));
        this.languageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hBox.getChildren().remove(1);
                hBox.getChildren().add(languagePane);
                onPress(languageButton, languagePane);

            }
        });
        this.appearanceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hBox.getChildren().remove(1);
                hBox.getChildren().add(appearancePane);
                onPress(appearanceButton, appearancePane);
            }
        });
        this.advanceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hBox.getChildren().remove(1);
                hBox.getChildren().add(advancePane);
                onPress(advanceButton, advancePane);
            }
        });

        Pane bottomPane = new Pane();
        HBox bottomPaneHbox = new HBox();
        bottomPane.setStyle("-fx-background-color: black");

        // Adding buttons to the vBox.
        this.vBox2.getChildren().addAll(appearanceButton, languageButton, advanceButton);
        this.leftPane.getChildren().addAll(vBox2);

        // Bottom pane buttons.
        cancelButton.setId("bottonPaneButtons");
        saveButton.setId("bottonPaneButtons");

        cancelButton.setOnAction(e -> {
            this.settingsStage.close();
        });
        saveButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Are you sure?");
            alert.setContentText("Saving the current changes will require the application to restart. Are you sure you want to continue?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                updateLocalStorage();
                this.settingsStage.close();
                this.stage.close();
                new Window(new Stage());
            } else {
                // ... user chose CANCEL or closed the dialog
            }
        });

        bottomPaneHbox.setSpacing(10);
        bottomPaneHbox.setMargin(cancelButton, new Insets(5, 10, 10, 550));
        bottomPaneHbox.setMargin(saveButton, new Insets(5, 20, 10, 20));
        bottomPaneHbox.setId("bottomPaneHbox");
        bottomPaneHbox.getChildren().addAll(cancelButton, saveButton);

        VBox vBox1 = new VBox();
        vBox1.getChildren().addAll(hBox, bottomPaneHbox);
        this.parentPane.getChildren().addAll(vBox1);

        Scene secondScene = new Scene(parentPane, 720, 500);
        secondScene.getStylesheets().add(ViewManager.getDefaultStyle("settings.css"));

        // New window (Stage)
        this.settingsStage = new Stage();
        this.settingsStage.setTitle("Settings");
        this.settingsStage.setScene(secondScene);

        // Specifies the modality for new window.
        this.settingsStage.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        this.settingsStage.initOwner(stage);
        this.settingsStage.setResizable(false);

    }

    private void updateLocalStorage() {
        sa.update(so);
    }

    /**
     *
     * @param btn
     * @param pane
     */
    public void onPress(Button btn, Pane pane) {
        this.hBox.getChildren().remove(1);
        this.hBox.getChildren().add(pane);
        for (Button button : this.categoryButtonsList) {
            if (btn == button) {
                button.setId("settingsCategoryOnPress");
            } else {
                button.setId("settingsCategory");
            }
        }
    }

    public AnchorPane getParentPane() {
        return this.parentPane;
    }

    public Stage getStage() {
        return settingsStage;
    }

    /**
     *
     */
    public void appearanceSetUp() {
        String appliedTheme = so.getTheme().getApplied().getName();
        List<ThemeObject> themeList = uio.getSettingsView().getCategories().getAppearance().getTheme().getAllThemes();
        Label themeLabel = new Label(this.controller.getUiObject().getSettingsView().getCategories().getAppearance().getTheme().getLabel());
        HBox appearanceHBox = new HBox();
        MenuButton themeMenu = new MenuButton(appliedTheme);
        // Populate the theme menu.
        for (ThemeObject themeObject : themeList) {
            CustomMenuItem menuItem = new CustomMenuItem(themeObject);
            themeMenu.getItems().add(menuItem);

            menuItem.setOnAction(e -> {
                themeMenu.setText(menuItem.getText());
                this.so.getTheme().setApplied((ThemeObject) menuItem.getContent());
            });
        }

        themeLabel.setId("themeLabel");
        themeMenu.setId("themeMenu");

        // Setting the space between the nodes of a VBox pane.
        appearanceHBox.setSpacing(10);
        // Setting the margin to the nodes.
        appearanceHBox.setMargin(themeLabel, new Insets(150, 20, 20, 20));
        appearanceHBox.setMargin(themeMenu, new Insets(150, 20, 20, 20));

        appearanceHBox.getChildren().addAll(themeLabel, themeMenu);
        this.appearancePane.getChildren().addAll(appearanceHBox);
    }

    /**
     *
     */
    public void languageSetUp() {
        String appliedLang = this.so.getLanguage().getApplied().getName();
        List<LanguageObject> langList = this.uio.getSettingsView().getCategories().getLanguage().getLanguageContent().getAllLanguages();

        Label langLabel = new Label(this.controller.getUiObject().getSettingsView().getCategories().getLanguage().getLabel());
        HBox langHBox = new HBox();
        MenuButton langMenu = new MenuButton(appliedLang);
        // Populate the theme menu.
        for (LanguageObject languageObject : langList) {
            CustomMenuItem menuItem = new CustomMenuItem(languageObject);
            langMenu.getItems().add(menuItem);

            menuItem.setOnAction(e -> {
                langMenu.setText(menuItem.getText());
                so.getLanguage().setApplied((LanguageObject) menuItem.getContent());
            });
        }

        langLabel.setId("langLabel");
        langMenu.setId("langMenu");

        // Setting the space between the nodes of a VBox pane.
        langHBox.setSpacing(10);
        // Setting the margin to the nodes.
        langHBox.setMargin(langLabel, new Insets(150, 20, 20, 20));
        langHBox.setMargin(langMenu, new Insets(150, 20, 20, 20));

        langHBox.getChildren().addAll(langLabel, langMenu);
        this.languagePane.getChildren().addAll(langHBox);
    }

    /**
     *
     */
    public void advanceSetUp() {
    }

    /**
     *
     */
    private static class SettingsPane {

        private final Controller controller;
        private final Stage stage;
        private final Pane parentPane = new Pane();
        private final VBox newVBox = new VBox();
        private final Label paneTitle = new Label();

        public SettingsPane(Controller _controller, String _title) {
            this.controller = _controller;
            this.stage = controller.getStage();
            this.paneTitle.setId("categoryTitle");
            this.paneTitle.setText(_title);
            this.newVBox.getChildren().add(this.paneTitle);
            this.newVBox.setAlignment(Pos.CENTER);
            this.newVBox.widthProperty().addListener(((observable, oldValue, newValue) -> {
                this.newVBox.setMinWidth(WIDTH * 0.7);
            }));

            this.newVBox.setId("vBoxTest");
            this.parentPane.widthProperty().addListener(((observable, oldValue, newValue) -> {
                this.parentPane.setMinWidth(this.stage.getScene().getWidth());
            }));

            this.parentPane.getChildren().addAll(this.newVBox);
        }
        public Pane getParentPane() {
            return this.parentPane;
        }
    }

    /**
     *
     */
    private static class CustomMenuItem extends MenuItem {

        private ItemObject content;

        public CustomMenuItem(ItemObject content) {
            this.setText(content.getName());
            this.content = content;
        }

        public ItemObject getContent() {
            return content;
        }

        public void setContent(ItemObject content) {
            this.content = content;
        }
    }
}
