/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.masthead.menu',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.common.layout.resize',
        'og.views.common.layout',
        'og.views.common.state',
        'og.views.configs.viewdefinition'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search = common.search_results.core(),
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            configs,
            resize = og.common.layout.resize,
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add configuration',
                    fields: [
                        {type: 'input', name: 'Name', id: 'name'},
                        {type: 'textarea', name: 'XML', id: 'xml'}
                    ],
                    buttons: {
                        'Ok': function () {
                            api.rest.configs.put({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', html: r.message});
                                    ui.dialog({type: 'input', action: 'close'});
                                    routes.go(routes.hash(module.rules.load_new_configs,
                                            $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                    ));
                                },
                                name: ui.dialog({return_field_value: 'name'}),
                                xml: ui.dialog({return_field_value: 'xml'})
                            });
                        }
                    }
                })},
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete configuration?',
                    message: 'Are you sure you want to permanently delete this configuration?',
                    buttons: {
                        'Delete': function () {
                            $(this).dialog('close');
                            api.rest.configs.del({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', message: r.message});
                                    routes.go(routes.hash(module.rules.load_delete,
                                            $.extend({}, routes.last().args, {deleted: true})
                                    ));
                                }, id: routes.last().args.id
                            });
                        }
                    }
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.OG-js-search', 'page_type': 'configs',
                    'columns': [
                        {
                            id: 'type',
                            name: '<select class="og-js-type-filter" style="width: 140px">'
                                + '  <option value="">Type</option>'
                                + '  <option>CurrencyMatrix</option>'
                                + '  <option>CurveSpecificationBuilderConfiguration</option>'
                                + '  <option>SimpleCurrencyMatrix</option>'
                                + '  <option>TimeSeriesMetaDataConfiguration</option>'
                                + '  <option>ViewDefinition</option>'
                                + '  <option>VolatilitySurfaceSpecification</option>'
                                + '  <option>VolatilitySurfaceDefinition</option>'
                                + '  <option>YieldCurveDefinition</option>'
                                + '</select>',
                            field: 'type', width: 160,
                            filter_type: 'select',
                            filter_type_options: [
                                'CurrencyMatrix',
                                'CurveSpecificationBuilderConfiguration',
                                'SimpleCurrencyMatrix',
                                'HistoricalTimeSeriesRating',
                                'ViewDefinition',
                                'VolatilitySurfaceSpecification',
                                'VolatilitySurfaceDefinition',
                                'YieldCurveDefinition'
                            ]
                        },
                        {id: 'name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    }
                }
            },
            form_generators = {
                viewdefinition: og.views.configs.viewdefinition
            },
            load_configs_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: configs.load, stop: true}]});
                delete args[field];
                configs.search(args);
                routes.go(routes.hash(module.rules.load_configs, args));
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Configs',
                        recent_list: history.get_html('history.configs.recent') || 'no recently viewed configs'
                    }).appendTo($('.OG-js-details-panel .OG-details').empty());
                    ui.toolbar(options.toolbar['default']);
                    $('.OG-js-details-panel .og-box-error').empty().hide(), resize();
                }});
            },
            details_page = function (args) {
                api.rest.configs.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var details_json = result.data, template = details_json.template_data.type.toLowerCase();
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.configs.recent',
                            value: routes.current().hash
                        });
                        if (template in form_generators) return form_generators[template](details_json);
                        api.text({module: module.name + '.' + template, handler: function (template) {
                            var json = details_json.template_data,
                                $warning, warning_message = 'This configuration has been deleted';
                            json.configData = json.configJSON ? JSON.stringify(json.configJSON, null, 4)
                                    : json.configXML ? json.configXML : '';
                            $.tmpl(template, json).appendTo($('.OG-js-details-panel .OG-details').empty());
                            $warning = $('.OG-js-details-panel .og-box-error');
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $warning.html(warning_message).show();
                                resize();
                                $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                            } else {$warning.empty().hide(), resize();}
                            if (json.deleted) $warning.html(warning_message).show(); else $warning.empty().hide();
                            details.favorites();
                            resize({element: '.OG-details-container', offsetpx: -41});
                            resize({element: '.OG-details-container .og-details-content', offsetpx: -48});
                            resize({element: '.OG-details-container [data-og=config-data]', offsetpx: -120});
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_configs, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
                            $('.OG-config .og-js-save-config').click(function () {
                                var data_obj, data = $('.OG-config [data-og=config-data]').val(),
                                    rest_obj = {
                                    handler: function (e) {
                                        if (e.error) return alert(e.message);
                                        $('.og-js-msg').html('saved');
                                        ui.message({location: '.OG-details', message: 'saved'});
                                        setTimeout(function () {
                                            ui.message({location: '.OG-details', destroy: true});
                                            editing = false;
                                        }, 250);
                                        routes.go(routes.hash(module.rules.load_edit_configs, $.extend(args, {
                                            edit: 'true'
                                        })));
                                    },
                                    id: routes.current().args.id,
                                    loading: function () {
                                        $('.og-js-msg').html('saving...');
                                        ui.message({location: '#OG-details', message: 'saving...'});
                                    },
                                    name: $('[data-og-editable=name]').html()
                                };
                                data_obj = data.charAt(0) === '<' ? {xml: data} : {json: data};
                                api.rest.configs.put($.extend(rest_obj, data_obj));
                            });
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({location: '.OG-js-details-panel', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            state = {};
        module.rules = {
            load: {route: '/' + page_name + '/name:?/type:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?', method: module.name + '.load_filter'},
            load_delete: {route: '/' + page_name + '/deleted:/name:?/type:?', method: module.name + '.load_delete'},
            load_configs: {route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_' + page_name},
            load_new_configs: {
                route: '/' + page_name + '/:id/new:/name:?/type:?', method: module.name + '.load_new_' + page_name
            },
            load_edit_configs: {
                route: '/' + page_name + '/:id/edit:/name:?/type:?', method: module.name + '.load_edit_' + page_name
            }
        };
        return configs = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        configs.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_details_page();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        configs.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_configs, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {
                configs.search(args);
                routes.go(routes.hash(module.rules.load, {name: args.name}));
            },
            load_new_configs: load_configs_without.partial('new'),
            load_edit_configs: load_configs_without.partial('edit'),
            load_configs: function (args) {
                check_state({args: args, conditions: [{new_page: configs.load}]});
                configs.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});