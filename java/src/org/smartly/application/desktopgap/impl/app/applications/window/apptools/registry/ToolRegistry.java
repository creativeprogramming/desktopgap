package org.smartly.application.desktopgap.impl.app.applications.window.apptools.registry;

import org.smartly.application.desktopgap.impl.app.IDesktopConstants;
import org.smartly.application.desktopgap.impl.app.applications.window.AppInstance;
import org.smartly.application.desktopgap.impl.app.applications.window.AppRegistry;
import org.smartly.application.desktopgap.impl.app.applications.window.frame.AppFrame;
import org.smartly.application.desktopgap.impl.app.applications.window.apptools.AbstractTool;
import org.smartly.application.desktopgap.impl.app.applications.window.apptools.AbstractTool;
import org.smartly.commons.util.StringUtils;

/**
 * Application registry.
 * Store persistent data for application.
 */
public final class ToolRegistry extends AbstractTool {

    public static final String NAME = "registry";

    private static final String APP_DATA = IDesktopConstants.APP_DATA;

    private final AppRegistry _registry;

    public ToolRegistry(final AppInstance app) {
        super(app);
        _registry = app.getRegistry();
    }

    public String getToolName(){
        return NAME;
    }

    public String put(final String key, final String value) {
        if(null!=value){
            _registry.set(getKey(key), value);
            return value;
        }
        return "";
    }

    public String get(final String key) {
        final Object result = _registry.get(getKey(key));
        return null != result ? result.toString() : "";
    }

    public String remove(final String key){
        final Object result = _registry.remove(getKey(key));
        return null != result ? result.toString() : "";
    }

    // ------------------------------------------------------------------------
    //                      p r i v a t e
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    //                      S T A T I C
    // ------------------------------------------------------------------------

    private static String getKey(final String key) {
        if (!key.startsWith(APP_DATA)) {
            return StringUtils.concatDot(APP_DATA, key);
        }
        return key;
    }

}
