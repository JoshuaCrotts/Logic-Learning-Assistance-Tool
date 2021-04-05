package com.llat.controller;

import com.llat.input.interpreters.LLATParserInterpreter;
import com.llat.tools.EventBus;
import com.llat.tools.MouseManager;
import com.llat.tools.ViewManager;
import com.llat.views.ApplicationView;
import com.llat.views.LoginView;
import com.llat.views.ParseTreeView;
import com.llat.views.SymbolButton;
import com.llat.views.events.SolveButtonEvent;
import com.llat.views.events.SymbolDescriptionEvent;
import com.llat.views.events.SymbolInputEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private Stage stage;
    private LLATParserInterpreter llatParserInterpreter = new LLATParserInterpreter();

    public Controller(Stage _stage) {
        this.stage = _stage;
        this.stage.getScene().getStylesheets().add(ViewManager.getDefaultStyle());
    }

    public void initialize(URL _url, ResourceBundle _rb) {
    }

    /**
     * @param _viewName
     */
    public void changeViewTo(int _viewName) {
        Pane parentPane = this.getView(_viewName);
        this.stage.getScene().setRoot(parentPane);
    }

    /**
     * @param _symbolButton
     */
    public void setSymbolInputButtonOnAction(SymbolButton _symbolButton) {
        _symbolButton.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.PRIMARY)
                EventBus.throwEvent(new SymbolInputEvent(((SymbolButton) event.getSource()).getDefaultSymbol().getSymbol().getApplied()));
            if (event.getButton() == MouseButton.SECONDARY)
                EventBus.throwEvent(new SymbolDescriptionEvent(((SymbolButton) event.getSource()).getDefaultSymbol()));
        });
    }

    /**
     * @param _canvas
     */
    public void setPaneToZoomable(Canvas _canvas) {
        _canvas.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                double modifier = 0;
                if (scrollEvent.getDeltaY() < 0) {
                    modifier = .1;
                } else {
                    modifier = -.1;
                }
                if (_canvas.getScaleX() + modifier < ParseTreeView.MAXSCALE && _canvas.getScaleX() + modifier > ParseTreeView.MINSCALE + .1) {
                    _canvas.setScaleX(_canvas.getScaleX() + modifier);
                }
                if (_canvas.getScaleY() + modifier < ParseTreeView.MAXSCALE && _canvas.getScaleY() + modifier > ParseTreeView.MINSCALE + .1) {
                    _canvas.setScaleY(_canvas.getScaleY() + modifier);
                }
            }
        });
    }

    /**
     * @param _canvas
     */
    public void setPaneToPannable(Canvas _canvas) {
        _canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                MouseManager curMouse = new MouseManager(mouseEvent.getX(), mouseEvent.getY());
                _canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        double xMovement = _canvas.getTranslateX() + (curMouse.getCurX() - mouseEvent.getX());
                        if (xMovement > 0 - _canvas.getWidth() / 2 && xMovement < _canvas.getWidth() - (_canvas.getWidth() / 2)) {
                            _canvas.setTranslateX(_canvas.getTranslateX() + (curMouse.getCurX() - mouseEvent.getX()) / (2 / _canvas.getScaleX()));
                        }
                        double yMovement = _canvas.getTranslateY() + (curMouse.getCurY() - mouseEvent.getY());
                        if (yMovement > 0 - _canvas.getHeight() / 2 && yMovement < _canvas.getHeight() - (_canvas.getHeight() / 2)) {
                            _canvas.setTranslateY(_canvas.getTranslateY() + (curMouse.getCurY() - mouseEvent.getY()) / (2 / _canvas.getScaleX()));
                        }
                        curMouse.setCurX(mouseEvent.getX());
                        curMouse.setCurY(mouseEvent.getY());
                    }
                });
            }
        });
    }

    /**
     * @param _button
     */
    public void setSolveButtonOnAction(Button _button) {
        _button.setOnAction((event) -> {
            EventBus.throwEvent(new SolveButtonEvent());
        });
    }

    public Stage getStage() {
        return this.stage;
    }

    private Pane getView(int _viewName) {
        Pane parentPane = new Pane();
        switch (_viewName) {
            case ViewManager.MAINAPPLICATION: {
                parentPane = (new ApplicationView(this).getParentPane());
                break;
            }
            case ViewManager.LOGIN:
                parentPane = (new LoginView(this)).getParentPane();
                break;
            default:
                // Update this to error view.
                parentPane = new Pane();
        }
        ;
        return parentPane;
    }

}
