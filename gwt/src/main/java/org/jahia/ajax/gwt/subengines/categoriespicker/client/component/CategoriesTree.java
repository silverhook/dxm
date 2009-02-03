/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.subengines.categoriespicker.client.component;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.CheckChangedEvent;
import com.extjs.gxt.ui.client.event.CheckChangedListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.StoreFilterField;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryService;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryServiceAsync;
import org.jahia.ajax.gwt.subengines.categoriespicker.client.CategoriesPickerEntryPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:00:19
 */
public class CategoriesTree extends ContentPanel {
    private String categoryKey;
    private TreeBinder<GWTJahiaCategoryNode> binder;
    private CategoriesPickerLeftComponent parentComponent;
    private final List<GWTJahiaCategoryNode> selectedCategories;
    private final String categoryLocale;
    private final boolean autoSelectParent;

    public CategoriesTree(String categoryKey, CategoriesPickerLeftComponent parentComponent, List<GWTJahiaCategoryNode> selectedCategories, String categoryLocale, boolean autoSelectParent) {
        this.categoryKey = categoryKey;
        this.parentComponent = parentComponent;
        this.selectedCategories = selectedCategories;
        this.categoryLocale = categoryLocale;
        this.autoSelectParent = autoSelectParent;
    }

    @Override
    protected void onRender (Element parent, int pos) {
        super.onRender(parent, pos);
        setHeading(getResource("categories"));
        setScrollMode(Style.Scroll.AUTO);

        final CategoryServiceAsync service = CategoryService.App.getInstance();

        // data proxy
        RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>> proxy = new RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>>() {
            @Override
            protected void load (GWTJahiaCategoryNode gwtJahiaCategoryNode, AsyncCallback<List<GWTJahiaCategoryNode>> listAsyncCallback) {
                service.ls(gwtJahiaCategoryNode,categoryLocale, listAsyncCallback);
            }
        };
        final CheckChangedListener listener = new CheckChangedListener() {
            @Override
            public void checkChanged (CheckChangedEvent event) {
                super.checkChanged(event);
                final List<GWTJahiaCategoryNode> list = event.getCheckedSelection();
                final List<GWTJahiaCategoryNode> gwtJahiaCategoryNodeList = new ArrayList<GWTJahiaCategoryNode>(list.size()-1);
                parentComponent.removeAllCategories();
                for (GWTJahiaCategoryNode selectedCategory : list) {
                    if(!selectedCategory.getParentKey().equals(selectedCategory.getKey())){
                        gwtJahiaCategoryNodeList.add(selectedCategory);
                    }
                }
                parentComponent.addCategories(gwtJahiaCategoryNodeList);
            }
        };
        // tree loader
        TreeLoader<GWTJahiaCategoryNode> loader = new BaseTreeLoader<GWTJahiaCategoryNode>(proxy) {
            @Override
            public boolean hasChildren (GWTJahiaCategoryNode parent) {
                return !parent.isLeaf();
            }


            public boolean load (GWTJahiaCategoryNode gwtJahiaCategoryNode) {
                if (gwtJahiaCategoryNode != null) {
                    List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes = gwtJahiaCategoryNode.getChildren();
                    for (GWTJahiaCategoryNode currentNode : gwtJahiaCategoryNodes) {
                        loadChildren(currentNode);
                    }
                }
                return super.load(gwtJahiaCategoryNode);
            }

            @Override
            protected void onLoadSuccess (GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
                super.onLoadSuccess(gwtJahiaCategoryNode, gwtJahiaCategoryNodes);
                if (gwtJahiaCategoryNode == null) {
                    for (GWTJahiaCategoryNode currentNode : gwtJahiaCategoryNodes) {
                        loadChildren(currentNode);
                        TreeItem item = (TreeItem) binder.findItem(currentNode);
                        item.setExpanded(true);
                    }
                } else {
                    for (GWTJahiaCategoryNode jahiaCategoryNode : gwtJahiaCategoryNodes) {
                        TreeItem item = (TreeItem) binder.findItem(jahiaCategoryNode);
                            for (GWTJahiaCategoryNode selectedCategory : selectedCategories) {
                                if (selectedCategory.getCategoryId().equals(jahiaCategoryNode.getCategoryId())) {
                                    item.setChecked(true);
                                    item.setExpanded(true);
                                    CheckChangedEvent evt = new CheckChangedEvent(binder, binder.getCheckedSelection());
                                    listener.checkChanged(evt);
                                }
                            }
                    }

                    TreeItem item = (TreeItem) binder.findItem(gwtJahiaCategoryNode);
                    if (item != null) {
                        for (GWTJahiaCategoryNode selectedCategory : selectedCategories) {
                            if (selectedCategory.getCategoryId().equals(gwtJahiaCategoryNode.getCategoryId())) {
                                item.setChecked(true);
                                item.setExpanded(true);
                                CheckChangedEvent evt = new CheckChangedEvent(binder, binder.getCheckedSelection());
                                listener.checkChanged(evt);
                            }
                        }
                    }
                    doLayout();
                }
            }
        };

        // trees store
        TreeStore<GWTJahiaCategoryNode> store = new TreeStore<GWTJahiaCategoryNode>(loader);
        store.setModelComparer(new ModelComparer<GWTJahiaCategoryNode>() {
            public boolean equals (GWTJahiaCategoryNode gwtJahiaCategoryNode, GWTJahiaCategoryNode gwtJahiaCategoryNode1) {
                return gwtJahiaCategoryNode != null && gwtJahiaCategoryNode1 != null && gwtJahiaCategoryNode.getCategoryId().equals(gwtJahiaCategoryNode1.getCategoryId());
            }
        });
        // tree
        final Tree tree = new Tree();
        tree.setCheckable(true);
        if (autoSelectParent) {
            tree.setCheckStyle(Tree.CheckCascade.PARENTS);
        } else {
            tree.setCheckStyle(Tree.CheckCascade.NONE);
        }
        tree.setHeight(400);
        binder = new TreeBinder<GWTJahiaCategoryNode>(tree, store);
        binder.setDisplayProperty("name");
        binder.setAutoLoad(true);
        binder.setCaching(true);

        binder.addCheckListener(listener);
        loader.load(null);
        for (GWTJahiaCategoryNode gwtJahiaCategoryNode : selectedCategories) {
            loader.load(gwtJahiaCategoryNode);
        }
        binder.setSelection(selectedCategories);

        StoreFilterField filter = new StoreFilterField() {
            @Override
            protected boolean doSelect (Store store, ModelData parent, ModelData record, String property, String filter) {
                if (record instanceof GWTJahiaCategoryNode) {
                    GWTJahiaCategoryNode node = (GWTJahiaCategoryNode) record;
                    if(node.getName().toLowerCase().startsWith(filter.toLowerCase())) return true;
                }
                return false;
            }
        };
        filter.bind(store);
        filter.setWidth("100%");
        VerticalPanel panel = new VerticalPanel();
        panel.setAutoHeight(true);
        panel.setWidth("100%");
        setWidth("100%");
        panel.add(filter);
        panel.add(tree);

        add(panel);
//        binder.setCheckedSelection(selectedCategories);
        doLayout();
    }

    private static String getResource (String key) {
        return CategoriesPickerEntryPoint.getResource(key);
    }


}
