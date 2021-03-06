package org.smartly.application.desktopgap.impl.app.applications.window.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.smartly.application.desktopgap.impl.app.IDesktopConstants;
import org.smartly.application.desktopgap.impl.app.applications.window.AppManifest;
import org.smartly.application.desktopgap.impl.app.applications.window.frame.AppFrame;
import org.smartly.application.desktopgap.impl.app.applications.window.javascript.JsEngine;
import org.smartly.application.desktopgap.impl.app.applications.window.javascript.snippets.JsSnippet;
import org.smartly.application.desktopgap.impl.app.server.WebServer;
import org.smartly.application.desktopgap.impl.app.utils.URLUtils;
import org.smartly.commons.logging.Level;
import org.smartly.commons.logging.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FX Controller
 */
public class AppWindowController implements Initializable {

    // --------------------------------------------------------------------
    //               FX Components
    // --------------------------------------------------------------------

    @FXML
    private AnchorPane container;

    @FXML
    private WebView win_browser;

    //-- win buttons --//
    @FXML
    private ImageView btn_close;

    // --------------------------------------------------------------------
    //               fields
    // --------------------------------------------------------------------

    private AppFrame _frame;
    private AppWindowAreaManager _areaManager;
    private JsEngine _jsengine;
    private String _location;
    private String _old_location;

    // --------------------------------------------------------------------
    //               Constructor
    // --------------------------------------------------------------------

    public AppWindowController() {

    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        // ready
    }

    // --------------------------------------------------------------------
    //               event handlers
    // --------------------------------------------------------------------

    @FXML
    private void btn_close_click(MouseEvent event) {
        //System.out.println("You clicked me!");
        Stage stage = (Stage) btn_close.getScene().getWindow();
        //stage.close();
        if (null != _frame) {
            _frame.close();
        }
    }

    // --------------------------------------------------------------------
    //               Properties
    // --------------------------------------------------------------------

    public void initialize(final AppFrame frame) {
        if (null == _frame) {
            _frame = frame;
            _areaManager = new AppWindowAreaManager(_frame, container);
            _jsengine = frame.getJavascriptEngine();

            this.initBrowser(win_browser);
            this.navigate(_frame.getIndex());
        }
    }

    public AppWindowAreaManager getAreas() {
        return _areaManager;
    }

    // ------------------------------------------------------------------------
    //                      p r i v a t e
    // ------------------------------------------------------------------------

    private Logger getLogger() {
        return _frame.getApp().getLogger();
    }

    private void navigate(final String url) {
        if (null != win_browser) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setLocation(url);
                }
            });
        }
    }

    private void setLocation(final String url) {
        if (null != win_browser) {
            try {
                // remove old
                // AppWindowUrl.delete(_old_location);

                // final AppWindowUrl uri = new AppWindowUrl(_frame, url);
                // navigate page
                _location = WebServer.getHttpPath(url); //uri.getUrl();
                _location = URLUtils.addParamToUrl(_location, IDesktopConstants.PARAM_APPID, _frame.getApp().getId());
                _location = URLUtils.addParamToUrl(_location, IDesktopConstants.PARAM_FRAMEID, _frame.getId());
                win_browser.getEngine().load(_location);
            } catch (Throwable t) {
                this.getLogger().log(Level.SEVERE, null, t);
            }
        }
    }


    private void initBrowser(final WebView browser) {
        final AppManifest manifest = _frame.getManifest();

        // disable/enable context menu
        browser.setContextMenuEnabled(manifest.hasContextMenu());

        //-- handlers --//
        final WebEngine engine = browser.getEngine();
        _jsengine.handleLoading(engine);

        // disable alert(), prompt(), confirm()
        this.handleAlert(engine);  // replaced with console.warn()
        this.handlePrompt(engine);
        this.handleConfirm(engine);

        this.handleLoading(engine);
        this.handlePopups(engine);
    }

    private void handleAlert(final WebEngine engine) {
        engine.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(final WebEvent<String> stringWebEvent) {
                if (null != stringWebEvent) {
                    final String data = stringWebEvent.getData();
                    _jsengine.whenReady(JsSnippet.getConsoleWarn(data));
                }
            }
        });
    }

    private void handlePrompt(final WebEngine engine) {
        engine.setPromptHandler(new Callback<PromptData, String>() {
            @Override
            public String call(final PromptData promptData) {
                return "";
            }
        });
    }

    private void handleConfirm(final WebEngine engine) {
        engine.setConfirmHandler(new Callback<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                return false;
            }
        });
    }


    private void handleLoading(final WebEngine engine) {
        // process page loading
        engine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov,
                                        Worker.State oldState, Worker.State newState) {
                        // debug info
                        //System.out.println(newState);
                        try {
                            if (newState == Worker.State.CANCELLED) {
                                // navigation cancelled by user
                                //_location = _old_location;
                            } else if (newState == Worker.State.FAILED) {
                                // navigation failed
                                _location = _old_location;
                            } else if (newState == Worker.State.READY) {
                                // browser ready
                                //System.out.println(engine.getLocation());
                            } else if (newState == Worker.State.SCHEDULED) {
                                // browser scheduled navigation
                                //System.out.println(engine.getLocation());
                            } else if (newState == Worker.State.RUNNING) {
                                // browser is loading data
                                //System.out.println(engine.getLocation());
                                _old_location = _location;
                                _location = engine.getLocation();
                                if (!AppWindowUrl.equals(_old_location, _location)) {
                                    //-- changing page --//
                                    // System.out.println("FROM: " + _old_location + " TO: " + _location);

                                    navigate(_location);
                                }
                            } else if (newState == Worker.State.SUCCEEDED) {
                                //_jsengine.init();
                                //_jsengine.showHideElem(_frame.getManifest().getButtonsMap());
                                //_jsengine.dispatchReady();

                                //-- remove page --//
                                //AppWindowUrl.delete(_location);
                            }
                        } catch (Throwable t) {
                            getLogger().log(Level.SEVERE, null, t);
                        }
                    }
                }
        );

    }

    private void handlePopups(final WebEngine engine) {
        engine.setCreatePopupHandler(
                new Callback<PopupFeatures, WebEngine>() {

                    @Override
                    public WebEngine call(PopupFeatures config) {
                        /*final WebView smallView = new WebView();
                        smallView.setFontScale(0.8);
                        if (!toolBar.getChildren().contains(smallView)) {
                            toolBar.getChildren().add(smallView);
                        }
                        return smallView.getEngine();*/
                        return engine;
                    }

                }
        );

    }


    // --------------------------------------------------------------------
    //               S T A T I C
    // --------------------------------------------------------------------


}
