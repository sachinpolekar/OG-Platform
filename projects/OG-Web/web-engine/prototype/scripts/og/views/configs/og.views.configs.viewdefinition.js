/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.configs.viewdefinition',
    dependencies: [
        'og.common.util.ui.message',
        'og.common.util.ui.Form',
        'og.views.forms.Constraints',
        'og.views.forms.Dropdown'
    ],
    obj: function () {
        var ui = og.common.util.ui, forms = og.views.forms,
            RMDF = 'resultModelDefinition',
            SETS = 'calculationConfiguration',
            DEFP = 'defaultProperties',
            COLS = 'portfolioRequirementsBySecurityType',
            REQS = 'portfolioRequirement',
            SECU = 'securityType'
            REQO = 'requiredOutput',
            CONS = 'constraints';
        return function (data) {
            og.dev.log('data!', data.template_data.configJSON);
            var id_count = 0, prefix = 'view_def_', master = data.template_data.configJSON, column_set_tabs,
                form = new ui.Form({
                    module: 'og.views.forms.view-definition',
                    data: master,
                    selector: '.OG-details',
                    extras: {name: master.name},
                    processor: function (data) { // remove undefineds that we added
                        if (!data[SETS]) return;
                        data[SETS] = data[SETS].filter(function (set) {return set !== undefined;});
                        data[SETS].forEach(function (set, set_idx) {
                            if (!set[COLS]) return;
                            set[COLS] = set[COLS].filter(function (col) {return col !== undefined;});
                            set[COLS].forEach(function (col, col_idx) {
                                if (!col[REQS]) return;
                                col[REQS] = col[REQS].filter(function (req) {return req !== undefined;});
                            });
                        });
                    },
                    handlers: [
                        {type: 'form:load', handler: function () {
                            og.common.layout.resize({element: '.OG-details-container', offsetpx: -41});
                            og.common.layout.resize({element: '.OG-details-container .og-details-content', offsetpx: -48});
                            og.common.layout.resize({element: '.OG-details-container [data-og=config-data]', offsetpx: -120});
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                        }},
                        {type: 'form:submit', handler: function (result) {
                            og.dev.log(result.data);
                        }}
                    ]
                });
            form.children = [
                new form.Block({ // form item_0
                    module: 'og.views.forms.view-definition-identifier-currency',
                    children: [
                        new forms.Dropdown({
                            form: form, resource: 'portfolios', index: 'identifier', value: master.identifier,
                            placeholder: 'Please choose a portfolio...', fields: [0, 2]
                        })
                    ],
                    handlers: [{type: 'form:load', handler: function () {
                        $('#' + form.id + ' select[name=currency]').val(master.currency);
                    }}]
                }),
                (function () { // form item_1
                    var result_def = master[RMDF], ids = [], fields;
                    fields = ['primitive', 'security', 'position', 'aggregatePosition', 'trade'].map(function (val) {
                        return ids.push(prefix + id_count++), val + 'OutputMode';
                    });
                    return new form.Block({
                        module: 'og.views.forms.view-definition-result-model-definition',
                        extras: fields.reduce(function (acc, val, idx) {return acc[val] = ids[idx], acc;}, {}),
                        handlers: [{type: 'form:load', handler: function () {
                            fields.forEach(function (field, idx) {
                                $('#' + ids[idx]).attr('name', RMDF + '.' + field).val(result_def[field]);
                            });
                        }}]
                    })
                })(),
                new form.Block({ // form item_2
                    module: 'og.views.forms.view-definition-execution-parameters',
                    handlers: [{type: 'form:load', handler: function () {
                        ['DeltaCalcPeriod', 'FullCalcPeriod'].forEach(function (suffix) {
                            ['min', 'max'].forEach(function (prefix) {
                                $('#' + prefix + suffix).val(master[prefix + suffix]);
                            });
                        });
                    }}]
                })
            ];
            (function () {
                var column_sets = new form.Block,
                    new_col_set = function (set, set_idx) {
                        var id = prefix + id_count++, column_set = new form.Block({
                            wrap: '<div class="og-js-colset-holder" id="' + id + '">' +
                                    '<div class="og-js-colset">{{html html}}</div>' +
                                    '<span class="OG-icon og-icon-add og-js-add-col-val">Add column value</span>' +
                                '</div>',
                            handlers: [
                                {
                                    type: 'click', selector: 'div#' + id + ' .og-js-add-col-val',
                                    handler: function (e) { // add a column value
                                        var $div = $('#' + id + ' div.og-js-colset'), block, col = {};
                                        col[SECU] = '';
                                        if (!set[COLS]) set[COLS] = [col]; else set[COLS].push(col);
                                        column_set.children.push(block = new_col_val(col, set[COLS].length - 1));
                                        block.html(function (html) {$div.append($(html)), block.load();});
                                        return false;
                                    }
                                },
                                {
                                    type: 'click', selector: 'div#' + id + ' .og-js-rem-col-val',
                                    handler: function (e) { // remove a column value
                                        var $div = $(e.target).parent('div:first'),
                                            cols = $div.find('select:first').attr('name').split('.').slice(0, -1),
                                            index = cols.pop();
                                        cols.reduce(function (a, v) {return a[v];}, master)[index] = undefined;
                                        $div.remove();
                                        return false;
                                    }
                                },
                                {
                                    type: 'click', selector: 'div#' + id + ' .og-js-rem-port-req',
                                    handler: function (e) { // remove a portfolio requirement
                                        var $li = $(e.target).parents('li:first'),
                                            reqs = $li.find('select:first').attr('name').split('.').slice(0, -1),
                                            index = reqs.pop();
                                        reqs.reduce(function (a, v) {return a[v];}, master)[index] = undefined;
                                        $li.remove();
                                        return false;
                                    }
                                },
                                {
                                    type: 'click', selector: 'div#' + id + ' .og-js-rem-colset',
                                    handler: function (e) { // remove a column set
                                        var $set = $(e.target).parents('div.og-js-colset-holder:first'),
                                            length = $('#' + form.id + ' div.og-js-colset-holder').length,
                                            index = $('#' + form.id + ' div.og-js-colset-holder').index($set),
                                            next = index ? index - 1 : index + 1, is_last = next === length,
                                            $tab = $('#' + form.id + ' a.og-js-colset-tab:eq(' + index + ')'),
                                            is_active = $tab.is('.og-active'), $next_tab, $next_set;
                                        master[SETS][set_idx] = undefined;
                                        if (!is_last) {
                                            $next_tab = $('#' + form.id + ' a.og-js-colset-tab:eq(' + next + ')');
                                            $next_set = $('#' + form.id + ' div.og-js-colset-holder:eq(' + next + ')');
                                        }
                                        $tab.remove(), $set.remove();
                                        if (is_last || !is_active) return false;
                                        $next_tab.addClass('og-active');
                                        $next_set.show();
                                        return false;
                                    }
                                }
                            ]
                        }),
                        new_col_val = function (col, col_idx) {
                            var col_id = prefix + id_count++,
                                new_port_req = function (req, req_idx) {
                                    var sel_name = [SETS, set_idx, COLS, col_idx, REQS, req_idx, REQO].join('.'),
                                        cons_name = [SETS, set_idx, COLS, col_idx, REQS, req_idx, CONS].join('.');
                                    return new form.Block({
                                        module: 'og.views.forms.view-definition-portfolio-requirement',
                                        extras: {title: 'Portfolio Requirement ' + (req_idx + 1), name: sel_name},
                                        children: [
                                            new forms.Dropdown({
                                                form: form, resource: 'valuerequirementnames', index: sel_name,
                                                value: req[REQO], rest_options: {meta: true},
                                                placeholder: 'Please select...'
                                            }),
                                            new forms.Constraints({form: form, data: req[CONS], index: cons_name})
                                        ]
                                    });
                                },
                                reqs_block = reqs_block = new form.Block({
                                    wrap: '<ul class="og-js-port-req og-portfolio-requirements">{{html html}}</ul>'
                                });
                            if (col[REQS]) Array.prototype.push.apply(reqs_block.children, col[REQS].map(new_port_req));
                            return new form.Block({
                                module: 'og.views.forms.view-definition-column-values',
                                extras: {id: col_id},
                                handlers: [{
                                    type: 'click', selector: 'div#' + col_id + ' .og-js-add-port-req',
                                    handler: function (e) { // add a portfolio requirement
                                        var $ul = $(e.target).parents('div:first').find('ul.og-js-port-req'),
                                            block, req = {};
                                        req[CONS] = {}, req[REQO] = '';
                                        if (!col[REQS]) col[REQS] = [req]; else col[REQS].push(req);
                                        reqs_block.children.push(block = new_port_req(req, col[REQS].length - 1));
                                        block.html(function (html) {$ul.append($(html)), block.load();});
                                        return false;
                                    }
                                }],
                                children: [
                                    new forms.Dropdown({
                                        form: form, resource: 'securities', rest_options: {meta: true},
                                        index: [SETS, set_idx, COLS, col_idx, SECU].join('.'),
                                        value: col[SECU],
                                        placeholder: 'Please select...'
                                    }),
                                    reqs_block
                                ]
                            });
                        };
                        column_set.children = [
                            new form.Block({ // column set top (name, default properties, etc)
                                module: 'og.views.forms.view-definition-column-set-top',
                                extras: {name: [SETS, set_idx, 'name'].join('.'), value: set.name},
                                children: [new forms.Constraints({
                                    form: form, data: set[DEFP], index: [SETS, set_idx, DEFP].join('.')
                                })],
                                handlers: [{
                                    selector: 'input[name=' + [SETS, set_idx, 'name'].join('.') + ']', type: 'keyup',
                                    handler: function (e) {
                                        $('#' + form.id + ' a.og-js-colset-tab.og-active span').text($(e.target).val());
                                    }
                                }]
                            })
                        ];
                        if (set[COLS]) Array.prototype.push.apply(column_set.children, set[COLS].map(new_col_val));
                        return column_set;
                    };
                form.children.push(
                    new form.Block({ // form item_3
                        module: 'og.views.forms.tabs',
                        extras: {
                            tabs: master[SETS].reduce(function (acc, set, idx) {
                                return acc + '<a class="og-tab og-js-colset-tab' + (idx ? '' : ' og-active') +
                                    '" href="#"><span>' + set.name + '</span></a>';
                            }, '')
                        },
                        handlers: [
                            {type: 'form:load', handler: function () {
                                $('#' + form.id + ' a.og-js-colset-tab').each(function (idx, tab) {
                                    $('#' + form.id + ' div.og-js-colset-holder:gt(0)').hide();
                                });
                            }},
                            {type: 'click', selector: 'a.og-js-colset-tab', handler: function (e) {
                                var $target = $(e.target), active_cl = 'a.og-active', new_cl = 'a.og-js-new',
                                    is_active = $target.is(active_cl) || $target.parent(active_cl).length,
                                    is_new = $target.is(new_cl) || $target.parent(new_cl).length, index;
                                if (is_active) return false;
                                if (is_new) { // add a column set
                                    var $div = $('#' + form.id + ' div.og-js-colsets');
                                    og.dev.log('add a column set', $div);
                                    return false;
                                };
                                if (!$target.is('a.og-js-colset-tab')) $target = $target.parent('a.og-js-colset-tab');
                                index = $('#' + form.id + ' a.og-js-colset-tab').index($target);
                                $('#' + form.id + ' div.og-js-colset-holder').each(function (idx, set) {
                                    $(set)[idx === index ? 'show' : 'hide']();
                                });
                                $target.addClass('og-active').siblings('.og-active').removeClass('og-active');
                                return false;
                            }}
                        ]
                    }),
                    column_sets // form item_4
                );
                Array.prototype.push.apply(column_sets.children, master[SETS].map(new_col_set));
            })()
            // new form.Block({module: 'og.views.forms.view-definition-specific-requirements-fields'}),
            // new form.Block({module: 'og.views.forms.constraints'}),
            // new form.Block({module: 'og.views.forms.constraints'}),
            // new form.Block({
            //     module: 'og.views.forms.view-definition-resolution-rule-transform-fields'
            // })
            form.dom();
        };
    }
});