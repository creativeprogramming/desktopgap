(function (window) {

    var document = window.document
        , defined = window.defined // func defined()
        ;
    // ------------------------------------------------------------------------
    //                      D E V I C E
    // ------------------------------------------------------------------------

    /**
     * The device object describes the device's hardware and software
     * <p/>
     * Properties
     * <p/>
     * <ul>
     *     <li>device.name</li>
     *     <li>device.platform</li>
     *     <li>device.uuid</li>
     *     <li>device.version</li>
     * </ul>
     * Variable Scope
     * <p/>
     * Since device is assigned to the window object, it is implicitly in the global scope.
     * <p/>
     * // These reference the same `device`
     * var phoneName = window.device.name;
     * var phoneName = device.name;
     */
    var device = {

        init: function () {
            var self = this;
            document.addEventListener('deviceready', function () {
                self['name'] = getDevice('name');
                self['platform'] = getDevice('platform');
                self['uuid'] = getDevice('uuid');
                self['version'] = getDevice('version');
            }, false);
            return self;
        },

        name: 'undefined',

        platform: 'undefined',

        uuid: 'undefined',

        version: 'undefined'

    };

    function getDevice(property) {
        var bridge = defined('bridge');
        if (!!bridge) {
            return bridge.device(property);
        }
        return 'undefined';
    }


    // --------------------------------------------------------------------
    //               exports
    // --------------------------------------------------------------------

    var exports = device.init();

    return exports;

})(this);