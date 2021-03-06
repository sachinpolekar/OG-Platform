/**
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.common.util.ui.toolbar',
    dependencies: ['og.common.util.ui.tooltip'],
    obj: function () {
        return function (obj) {
            var $new_obj = [], html,
                buttons = [
                    {name: 'delete', 'tooltip': 'DELETE', 'level': 'danger'},
                    {name: 'new', 'tooltip': 'NEW'}
                ];
            if (!obj) throw new Error('obj is a required input for toolbar');
            if (!obj.location) throw new Error('You need to supply a selector/location for a toolbar to be placed');
            $.each(obj.buttons, function (i) { // Apply 'off' level to disabled tooltips
                if (obj.buttons[i]['enabled'] === 'OG-disabled') obj.buttons[i]['level'] = 'off';
            });
            html = $.tmpl('<div class="og-${name} og-js-${name} ${enabled}"><span>${name}</span></div>',
                    $.extend(true, buttons, obj.buttons)
            );
            $(obj.location).html(html); // Add the buttons to the page
            // Implement handlers
            $.each(($.extend(true, $new_obj, {'buttons': buttons}, obj)).buttons, function (i, val) {
                $('.OG-js-details-panel .og-js-' + val.name).unbind('mousedown').bind('mousedown', val.handler);
            });
            og.common.util.ui.tooltip(obj.location);
        };
    }
});