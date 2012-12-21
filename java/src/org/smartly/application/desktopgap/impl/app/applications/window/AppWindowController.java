package org.smartly.application.desktopgap.impl.app.applications.window;

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
import netscape.javascript.JSObject;
import org.smartly.application.desktopgap.impl.app.applications.window.javascript.AppBridge;
import org.smartly.application.desktopgap.impl.app.utils.DOM;
import org.smartly.application.desktopgap.impl.app.utils.fx.FX;
import org.smartly.commons.cryptograph.MD5;
import org.smartly.commons.logging.Level;
import org.smartly.commons.logging.Logger;
import org.smartly.commons.util.PathUtils;
import org.smartly.commons.util.StringUtils;

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

    private AppWindow _window;
    private AppWindowAreaManager _areaManager;
    private String _location;
    private String _old_location;

    // --------------------------------------------------------------------
    //               Constructor
    // --------------------------------------------------------------------

    public AppWindowController() {

    }

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        // FX.draggable(win_top);
        FX.sizable(container);

    }

    // --------------------------------------------------------------------
    //               event handlers
    // --------------------------------------------------------------------

    @FXML
    private void btn_close_click(MouseEvent event) {
        //System.out.println("You clicked me!");
        Stage stage = (Stage) btn_close.getScene().getWindow();
        //stage.close();
        if (null != _window) {
            _window.close();
        }
    }

    // --------------------------------------------------------------------
    //               Properties
    // --------------------------------------------------------------------

    public void initialize(final AppWindow window) {
        _window = window;
        _areaManager = new AppWindowAreaManager(container);

        this.initBrowser(win_browser);
        this.navigate(_window.getIndex());
    }

    public AppWindowAreaManager getAreas() {
        return _areaManager;
    }
    // ------------------------------------------------------------------------
    //                      p r i v a t e
    // ------------------------------------------------------------------------

    private Logger getLogger() {
        return _window.getApp().getLogger();
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
                final String uri = stripProtocol(url);
                final AppManifest manifest = _window.getManifest();
                final String frame = _window.getFrame();
                //-- creates navigation page name --//
                final String md5 = MD5.encode(uri);
                final String output_file = manifest.getAbsoluteAppPath(md5.concat(".html"));

                //-- insert page into frame --//
                DOM.insertInFrame(manifest, frame, uri, output_file);

                // navigate page
                win_browser.getEngine().load("file:///" + output_file);
            } catch (Throwable t) {
                this.getLogger().log(Level.SEVERE, null, t);
            }
        }
    }


    private void initBrowser(final WebView browser) {
        // disable context menu
        browser.setContextMenuEnabled(false);

        //-- handlers --//
        final WebEngine engine = browser.getEngine();
        this.initJavascript(engine);
        this.handleAlert(engine);
        this.handlePrompt(engine);
        this.handleLoading(engine);
        this.handlePopups(engine);

        final URL loading = getClass().getResource("loading.html");
        final String url = loading.toExternalForm();
        win_browser.getEngine().load(url);
    }

    private void initJavascript(final WebEngine engine) {
        try {
            //-- get reference to javascript window object --//
            final Object obj = engine.executeScript(AppBridge.DESKTOPGAP_INSTANCE);
            if (obj instanceof JSObject) {
                final JSObject win = (JSObject) obj;
                // can add custom java objects
                win.setMember(AppBridge.NAME, new AppBridge(_window));
            }
        } catch (Throwable t) {
            _window.getApp().getLogger().log(Level.SEVERE, null, t);
        }
    }

    private void handleAlert(final WebEngine engine) {
        engine.setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(final WebEvent<String> stringWebEvent) {
                if (null != stringWebEvent) {
                    final String data = stringWebEvent.getData();
                    System.out.println(data);
                }
            }
        });
    }

    private void handlePrompt(final WebEngine engine) {
        engine.setPromptHandler(new Callback<PromptData, String>() {
            @Override
            public String call(final PromptData promptData) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
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
                        System.out.println(newState);

                        if (newState == Worker.State.CANCELLED) {
                            // navigation cancelled by user
                            _location = _old_location;
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
                            _old_location = stripProtocol(_location);
                            _location = stripProtocol(engine.getLocation());
                            if (null != _old_location && !_old_location.equalsIgnoreCase(_location)) {
                                //-- changing page --//
                                // System.out.println("FROM: " + _old_location + " TO: " + _location);
                               LOOP
                                setLocation(_location);
                            }
                        } else if (newState == Worker.State.SUCCEEDED) {
                            initJavascript(engine);
                            engine.executeScript(AppBridge.DESKTOPGAP_INIT_FUNC);
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

    private static String stripProtocol(final String url) {
        if (StringUtils.hasText(url)) {
            return PathUtils.toUnixPath(url).replace("file:///", "");
        }
        return "";
    }
}
