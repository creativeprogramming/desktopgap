package org.smartly.application.desktopgap.impl.app.applications.window;

import org.smartly.application.desktopgap.impl.app.DesktopController;
import org.smartly.application.desktopgap.impl.app.applications.events.AppCloseEvent;
import org.smartly.application.desktopgap.impl.app.applications.events.AppOpenEvent;
import org.smartly.application.desktopgap.impl.app.applications.events.IDesktopGapEvents;
import org.smartly.application.desktopgap.impl.app.applications.window.frame.AppFrame;
import org.smartly.application.desktopgap.impl.app.applications.window.appbridge.AppBridge;
import org.smartly.application.desktopgap.impl.app.applications.window.applibs.AppLibs;
import org.smartly.commons.event.Event;
import org.smartly.commons.event.EventEmitter;
import org.smartly.commons.event.IEventListener;
import org.smartly.commons.logging.Level;
import org.smartly.commons.logging.Logger;
import org.smartly.commons.logging.LoggingRepository;
import org.smartly.commons.logging.util.LoggingUtils;
import org.smartly.commons.util.PathUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Application Wrapper
 */
public final class AppInstance
        extends EventEmitter
        implements IEventListener {

    private final DesktopController _controller;
    private final AppManifest _manifest;
    private final AppRegistry _registry;
    private final AppLocalization _i18n;
    private final AppWindows _windows; // frames manager
    private final List<AppFrame> _children; // children frames
    private final AppBridge _bridge;
    private final AppLibs _libs; // external tools and plugins

    public AppInstance(final DesktopController controller,
                       final AppManifest manifest) throws IOException {
        _controller = controller;
        _manifest = manifest;
        _registry = new AppRegistry(_manifest);
        _windows = new AppWindows(this);
        _windows.addEventListener(this);
        _children = new ArrayList<AppFrame>();
        _i18n = new AppLocalization(this);

        // creates bridge
        _bridge = new AppBridge(this);
        _bridge.registerDefault();

        // register plugins
        _libs = new AppLibs(this, _bridge);

        this.initLogger();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("id: ").append(this.getId());
        result.append(", ");
        result.append("name: ").append(this.getName());
        result.append(", ");
        result.append("install_dir: ").append(this.getInstallDir());

        return result.toString();
    }

    public DesktopController getDesktop() {
        return _controller;
    }

    /**
     * Returns plugin/libs manager instance.
     * Use it to register Frame Tools
     * @return AppLibs.
     */
    public AppLibs getLibs(){
        return _libs;
    }

    public String getAbsolutePath(final String path) {
        if (PathUtils.isAbsolute(path)) {
            return path;
        }
        return PathUtils.merge(this.getInstallDir(), path);
    }

    public AppRegistry getRegistry() {
        return _registry;
    }

    public AppManifest getManifest() {
        return _manifest;
    }

    public AppBridge getBridge() {
        return _bridge;
    }

    public AppLocalization getI18n() {
        return _i18n;
    }

    public String getId() {
        return _manifest.getAppId();
    }

    public boolean isValid() {
        return _manifest.isValid();
    }

    public String getName() {
        return _manifest.getAppName();
    }

    public String getInstallDir() {
        return _manifest.getInstallDir();
    }

    public AppFrame open(final String winId) {
        return _windows.open(winId);
    }

    public AppInstance open() {
        _windows.open(null); // open main
        return this;
    }

    public void close() {
        _windows.close(null); // close all
    }

    public void kill() {
        _windows.kill(null); // close all
    }

    public Logger getLogger() {
        return LoggingUtils.getLogger(this.getId());
    }

    // --------------------------------------------------------------------
    //          Launch Applications or  Application's Window
    // --------------------------------------------------------------------

    public AppFrame launchApp(final String appId) {
        return this.launchApp(appId, appId, "", null, false);
    }

    /**
     * Launch another app and returns instance
     *
     * @param appId App identifier
     * @param winId (Optional) window instance identifier (only for multiple instance of same frame)
     * @return AppInstance
     */
    public AppFrame launchApp(final String appId,
                              final String winId,
                              final String title,
                              final Object args,
                              final boolean isChild) {
        AppFrame child = null;
        try {
            if (null != _controller) {
                child = _controller.launch(appId, winId);
            }
        } catch (Throwable t) {
            this.getLogger().log(Level.SEVERE, null, t);
        }

        // add child to internal child list
        if (null != child && isChild) {
            child.setTitle(title);
            child.putArguments(args);
            if (isChild) {
                _children.add(child);
            }
        }

        return child;
    }

    // --------------------------------------------------------------------
    //                      IEventListener
    // --------------------------------------------------------------------

    @Override
    public void on(final Event event) {
        if (event.getName().equalsIgnoreCase(IDesktopGapEvents.FRAME_CLOSE)) {
            // CLOSE
            this.handleCloseFrame((AppFrame) event.getSender());
        } else if (event.getName().equalsIgnoreCase(IDesktopGapEvents.FRAME_OPEN)) {
            // OPEN
            this.handleOpenFrame((AppFrame) event.getSender());
        }
    }

    // ------------------------------------------------------------------------
    //                      p r i v a t e
    // ------------------------------------------------------------------------


    private void initLogger() {
        final String id = _manifest.getAppId();
        final String installDir = _manifest.getInstallDir();
        LoggingRepository.getInstance().setAbsoluteLogFileName(id, PathUtils.concat(installDir, "application.log"));
    }

    private void handleCloseFrame(final AppFrame frame) {
        // set frame properties (auto saved)
        if (frame.isMaximized()) {
            this.getRegistry().setMaximized(frame.getId(), true);
        } else {
            this.getRegistry().setMaximized(frame.getId(), false);
            this.getRegistry().setX(frame.getId(), frame.getX());
            this.getRegistry().setY(frame.getId(), frame.getY());
            this.getRegistry().setWidth(frame.getId(), frame.getWidth());
            this.getRegistry().setHeight(frame.getId(), frame.getHeight());
        }

        // close children
        for (final AppFrame child : _children) {
            try {
                child.close();
            } catch (Throwable ignored) {
            }
        }

        // is last frame?
        if (_windows.isEmpty()) {
            this.emit(new AppCloseEvent(this));
        }
    }

    private void handleOpenFrame(final AppFrame frame) {
        if (frame.isMain()) {
            this.emit(new AppOpenEvent(this));
        }
    }

}
