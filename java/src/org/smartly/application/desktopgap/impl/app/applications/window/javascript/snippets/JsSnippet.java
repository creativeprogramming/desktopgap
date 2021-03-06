package org.smartly.application.desktopgap.impl.app.applications.window.javascript.snippets;

import org.json.JSONObject;
import org.smartly.commons.util.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Code snippet helper
 */
public class JsSnippet {

    private static final String _root = PathUtils.getPackagePath(JsSnippet.class);

    private static final String TPL_PREFIX = "[";
    private static final String TPL_SUFFIX = "]";

    //-- script template parameters--//
    private static final String PARAM_EVENT_NAME = "EVENT_NAME"; // string
    private static final String PARAM_EVENT_DATA = "EVENT_DATA"; // json string
    private static final String PARAM_SHOW = "SHOW"; // boolean
    private static final String PARAM_ELEMENT = "ELEMENT"; // string
    private static final String PARAM_VALUE = "VALUE"; // string

    //-- script shortcuts --//
    private static final String SCRIPT_CONSOLE_LOG = "/js/consoleLog.js";
    private static final String SCRIPT_CONSOLE_ERROR = "/js/consoleError.js";
    private static final String SCRIPT_CONSOLE_WARN = "/js/consoleWarn.js";
    private static final String SCRIPT_DISPATCH_EVENT = "/js/dispatchEvent.js";
    private static final String SCRIPT_SHOW_HIDE_ELEM = "/js/showHideElem.js";
    private static final String SCRIPT_SET_ELEM_VALUE = "/js/setElemValue.js";

    private JsSnippet() {
    }

    // ------------------------------------------------------------------------
    //                      p r i v a t e
    // ------------------------------------------------------------------------

    private String getScript(final String name, final Map<String, Object> params) {
        final String path = PathUtils.concat(_root, name);
        final String text = ClassLoaderUtils.getResourceAsString(path);
        return FormatUtils.formatTemplate(text, TPL_PREFIX, TPL_SUFFIX, params);
    }

    // --------------------------------------------------------------------
    //                      S T A T I C
    // --------------------------------------------------------------------

    private static JsSnippet __instance;

    private static JsSnippet getInstance() {
        if (null == __instance) {
            __instance = new JsSnippet();
        }
        return __instance;
    }

    public static String getConsoleLog(final String message) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_VALUE, message);

        return getInstance().getScript(SCRIPT_CONSOLE_LOG, params);
    }

    public static String getConsoleError(final String message) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_VALUE, message);

        return getInstance().getScript(SCRIPT_CONSOLE_ERROR, params);
    }

    public static String getConsoleWarn(final String message) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_VALUE, message);

        return getInstance().getScript(SCRIPT_CONSOLE_WARN, params);
    }

    public static String getDispatchEvent(final String eventName, final Object data) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_EVENT_NAME, stringify(eventName));
        params.put(PARAM_EVENT_DATA, stringify(data));

        return getInstance().getScript(SCRIPT_DISPATCH_EVENT, params);
    }

    public static String getShowHideElem(final String elementId, final boolean visible) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_ELEMENT, elementId);
        params.put(PARAM_SHOW, visible);

        return getInstance().getScript(SCRIPT_SHOW_HIDE_ELEM, params);
    }

    public static String getSetElemValue(final String elementId, final String value) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_ELEMENT, elementId);
        params.put(PARAM_VALUE, value);

        return getInstance().getScript(SCRIPT_SET_ELEM_VALUE, params);
    }

    // --------------------------------------------------------------------
    //               p r i v a t e
    // --------------------------------------------------------------------

    private static String stringify(final Object object){
        final String text = toString(object);
        return StringEscapeUtils.escapeJavaScript(text);
    }

    private static String toString(final Object object){
        if(null!=object){
            if(object instanceof JSONObject){
                return StringUtils.replace(object.toString(), new String[]{"\n"}, "<br>");
            } else {
                return object.toString();
            }
        }
        return "";
    }

}
