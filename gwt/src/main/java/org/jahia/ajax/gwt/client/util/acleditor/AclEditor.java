/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.util.acleditor;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.*;

/**
 * User: toto
 * Date: Sep 12, 2008
 * Time: 11:24:32 AM
 */
public class AclEditor {
    private GWTJahiaNodeACL acl;
    private final String context;
    private Button restoreButton;
    private boolean canBreakInheritance = false;
    private String autoAddRole;
    private transient PrincipalModelData autoAddRoleAdded = null;
    private Button breakinheritanceItem;
    private boolean readOnly = false;
    private List<String> displayedRoles;
    private String addUsersLabel = getResource("newUsers.label");
    private String addGroupsLabel = getResource("newGroups.label");
    private List<AclEditor> rolesEditors;
    private List<Grid> grids;

    private Boolean breakAllInheritance;
    private Map<String, List<PrincipalModelData>> initialValues;
    private Map<String, ListStore<PrincipalModelData>> stores;
    private StoreFilter<PrincipalModelData> inheritanceBreakFilter;
    private FormPanel formPanel;

    public AclEditor(GWTJahiaNodeACL acl, String aclContext, Set<String> roles, Set<String> roleGroups, List<AclEditor> rolesEditors) {
        this.acl = acl;
        this.context = aclContext;
        if (rolesEditors == null) {
            rolesEditors = new ArrayList<AclEditor>();
        }
        this.rolesEditors = rolesEditors;
        final Map<String, List<String>> map = acl.getAvailableRoles();
        if ((roleGroups == null || roleGroups.isEmpty()) && (roles == null || roles.isEmpty())) {
            displayedRoles = new ArrayList<String>();
            if (map != null && !map.isEmpty()) {
                for (List<String> l : map.values()) {
                    displayedRoles.addAll(l);
                }
            }
        } else {
            displayedRoles = new ArrayList<String>();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    if ((roleGroups != null && roleGroups.contains(entry.getKey()))) {
                        displayedRoles.addAll(entry.getValue());
                    } else if (roles != null && !roles.isEmpty()) {
                        for (String s : entry.getValue()) {
                            if (roles.contains(s)) {
                                displayedRoles.add(s);
                            }
                        }
                    }
                }
            }
        }
        initialValues = new HashMap<String, List<PrincipalModelData>>();
        stores = new HashMap<String, ListStore<PrincipalModelData>>();

        for (String displayedRole : displayedRoles) {
            final List<PrincipalModelData> values = new ArrayList<PrincipalModelData>();
            initialValues.put(displayedRole, values);
            for (GWTJahiaNodeACE gwtJahiaNodeACE : acl.getAce()) {
                final boolean set = gwtJahiaNodeACE.getRoles().containsKey(displayedRole);
                final boolean inherited = gwtJahiaNodeACE.getInheritedRoles().containsKey(displayedRole) && gwtJahiaNodeACE.getInheritedRoles().get(displayedRole);
                if (set || inherited) {
                    PrincipalModelData entry = new PrincipalModelData(gwtJahiaNodeACE.getPrincipalKey(), gwtJahiaNodeACE.getPrincipalType(), gwtJahiaNodeACE.getPrincipal(), gwtJahiaNodeACE.getPrincipalDisplayName(), !set);
                    if (inherited) {
                        entry.setInheritedFrom(gwtJahiaNodeACE.getInheritedFrom());
                    } else {
                        entry.setInheritedFrom("");
                    }
                    if (set && !gwtJahiaNodeACE.getRoles().get(displayedRole)) {
                        entry.setRemoved(true);
                    }
                    values.add(entry);
                }
            }
            final ListStore<PrincipalModelData> store = new ListStore<PrincipalModelData>();
            for (PrincipalModelData value : values) {
                store.add(value.getClone());
            }

            stores.put(displayedRole, store);
        }


        breakinheritanceItem = new Button();
        breakinheritanceItem.setEnabled(!readOnly && canBreakInheritance);
        breakinheritanceItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                final boolean newValue = !breakAllInheritance;
                if (autoAddRole != null && stores.containsKey(autoAddRole)) {
                    if (newValue) {
                        autoAddRoleAdded = new PrincipalModelData(JahiaGWTParameters.getCurrentUser(), 'u', JahiaGWTParameters.getCurrentUser(), JahiaGWTParameters.getCurrentUser(), false);
                        stores.get(autoAddRole).add(autoAddRoleAdded);
                    } else if (autoAddRoleAdded != null) {
                        stores.get(autoAddRole).remove(autoAddRoleAdded);
                        autoAddRoleAdded = null;
                    }
                }
                for (AclEditor roleEditor : AclEditor.this.rolesEditors) {
                    roleEditor.setBreakAllInheritance(newValue);
                    roleEditor.setDirty();
                    for (Grid grid : grids) {
                        resizeGrid(grid);
                    }
                }
            }
        });

        restoreButton = new Button(getResource("label.restore"));
        restoreButton.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
        restoreButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                for (AclEditor rolesEditor : AclEditor.this.rolesEditors) {
                    rolesEditor.setBreakAllInheritance(AclEditor.this.acl.isBreakAllInheritance());
                }
                setBreakInheritanceLabel();
                for (Map.Entry<String, List<PrincipalModelData>> entry : initialValues.entrySet()) {
                    stores.get(entry.getKey()).removeAll();
                    for (PrincipalModelData data : entry.getValue()) {
                        stores.get(entry.getKey()).add(data.getClone());
                    }
                }
                for (Grid grid : grids) {
                    grid.show();
                    resizeGrid(grid);
                }
                restoreButton.setEnabled(false);
            }
        });
        restoreButton.setEnabled(false);

        inheritanceBreakFilter = new StoreFilter<PrincipalModelData>() {
            @Override
            public boolean select(Store<PrincipalModelData> store, PrincipalModelData parent, PrincipalModelData item, String property) {
                return !item.getInherited() && !item.getRemoved();
            }
        };

        boolean breakAllInheritance = acl.isBreakAllInheritance();
        if (!rolesEditors.isEmpty() && rolesEditors.iterator().next().getBreakAllInheritance() != breakAllInheritance) {
            breakAllInheritance = rolesEditors.iterator().next().getBreakAllInheritance();
            setDirty();
        }
        setBreakAllInheritance(breakAllInheritance);

        rolesEditors.add(this);
    }

    public Boolean getBreakAllInheritance() {
        return breakAllInheritance;
    }

    public void setBreakAllInheritance(Boolean breakAllInheritance) {
        if (this.breakAllInheritance == breakAllInheritance) {
            return;
        }
        this.breakAllInheritance = breakAllInheritance;
        setBreakInheritanceLabel();

        if (breakAllInheritance) {
            for (ListStore<PrincipalModelData> listStore : stores.values()) {
                listStore.addFilter(inheritanceBreakFilter);
                listStore.applyFilters(null);
            }
        } else {
            for (ListStore<PrincipalModelData> listStore : stores.values()) {
                listStore.removeFilter(inheritanceBreakFilter);
                List<PrincipalModelData> l = new ArrayList<PrincipalModelData>();
                boolean duplicate = false;
                for (PrincipalModelData data : listStore.getModels()) {
                    if (!l.contains(data)) {
                        l.add(data);
                    } else {
                        duplicate = true;
                        PrincipalModelData otherData = l.get(l.indexOf(data));
                        if (data.getInherited()) {
                            l.remove(otherData);
                            l.add(data);
                        } else if (data.getInheritedFrom() != null && data.getRemoved()) {
                            data.setRemoved(false);
                            l.remove(otherData);
                            l.add(data);
                        }
                    }
                }
                if (duplicate) {
                    listStore.removeAll();
                    listStore.add(l);
                }
            }
        }
    }

    public List<GWTJahiaNodeACE> getEntries() {
        Map<String, GWTJahiaNodeACE> r = new HashMap<String, GWTJahiaNodeACE>();
        for (GWTJahiaNodeACE ace : acl.getAce()) {
            final GWTJahiaNodeACE value = ace.cloneObject();
            value.getRoles().keySet().retainAll(displayedRoles);
            for (String role : displayedRoles) {
                if (value.getRoles().containsKey(role)) {
                    value.getRoles().put(role,false);
                }
            }
            value.getInheritedRoles().keySet().retainAll(displayedRoles);
            r.put(ace.getPrincipalType() + ace.getPrincipal(), value);
        }

        for (Map.Entry<String, ListStore<PrincipalModelData>> entry : stores.entrySet()) {
            for (PrincipalModelData data : entry.getValue().getModels()) {
                String key = data.getType() + data.getName();
                if (!r.containsKey(key)) {
                    GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
                    ace.setPrincipal(data.getName());
                    ace.setPrincipalType(data.getType());
                    ace.setPrincipalKey(data.getKey());
                    ace.setPrincipalDisplayName(data.getDisplayName());
                    ace.setRoles(new HashMap<String, Boolean>());
                    ace.setInheritedRoles(new HashMap<String, Boolean>());
                    r.put(key, ace);
                }
                if (!data.getInherited() || data.getRemoved()) {
                    r.get(key).getRoles().put(entry.getKey(), !data.getRemoved());
                }
            }
        }
        return new ArrayList<GWTJahiaNodeACE>(r.values());
    }

    public void setCanBreakInheritance(boolean canBreakInheritance) {
        this.canBreakInheritance = canBreakInheritance;
        breakinheritanceItem.setEnabled(!readOnly && canBreakInheritance);
    }

    public void setAutoAddRole(String autoAddRole) {
        this.autoAddRole = autoAddRole;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getAddUsersLabel() {
        if (addUsersLabel == null) {
            return getResource("newUsers.label");
        }
        return addUsersLabel;
    }

    public void setAddUsersLabel(String addUsersLabel) {
        this.addUsersLabel = addUsersLabel;
    }

    public String getAddGroupsLabel() {
        if (addGroupsLabel == null) {
            return getResource("newGroups.label");
        }
        return addGroupsLabel;
    }

    public void setAddGroupsLabel(String addGroupsLabel) {
        this.addGroupsLabel = addGroupsLabel;
    }

    public void addNewAclPanel(final LayoutContainer c) {
        formPanel = new FormPanel();
        formPanel.setScrollMode(Style.Scroll.AUTO);

        grids = new ArrayList<Grid>();

        for (final String displayedRole : displayedRoles) {
            FieldSet fs = new FieldSet();
            fs.setHeadingHtml(acl.getRolesLabels().get(displayedRole));

            List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

            final ColumnConfig principalType = new ColumnConfig("type", 30);
            principalType.setRenderer(new GridCellRenderer() {
                @Override
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                    Character type = model.get(property);
                    Image html;
                    if (type.equals('u')) {
                        html = StandardIconsProvider.STANDARD_ICONS.user().createImage();
                    } else {
                        html = StandardIconsProvider.STANDARD_ICONS.group().createImage();
                    }
                    return html;
                }
            });
            configs.add(principalType);

            final ColumnConfig displayName = new ColumnConfig("displayName", 100);
            displayName.setRenderer(new GridCellRenderer<PrincipalModelData>() {
                @Override
                public Object render(PrincipalModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                    if (model.getRemoved()) {
                        return "<span class=\"markedForDeletion\">" + model.get(property) + "</span>" ;
                    } else {
                        return model.get(property);
                    }
                }
            });
            configs.add(displayName);


            final ColumnConfig removedColumnConfig = new ColumnConfig("removed", 50);
            removedColumnConfig.setRenderer(new GridCellRenderer<PrincipalModelData>() {
                @Override
                public Object render(final PrincipalModelData model, String property, ColumnData config, int rowIndex, int colIndex, final ListStore store, final Grid grid) {
                    Button button = new Button();
                    button.setBorders(false);
                    button.setEnabled(!readOnly);
                    if (!model.getRemoved()) {
                        button.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
                        button.setToolTip(getResource("label.remove"));
                        if (model.getInheritedFrom() != null && !model.getInheritedFrom().equals("") && !breakAllInheritance) {
                            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent event) {
//                                    model.setInherited(false);
                                    model.setRemoved(true);
                                    grid.getView().refresh(false);
                                    setDirty();
                                }
                            });
                        } else {
                            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent event) {
                                    store.remove(model);
                                    resizeGrid(grid);
                                    setDirty();
                                }
                            });
                        }
                        return button;
                    } else {
                        button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
                        button.setToolTip(getResource("label.restore"));
                        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
//                                    model.setInherited(true);
                                model.setRemoved(false);
                                grid.getView().refresh(false);
                                setDirty();
                            }
                        });
                        return button;
                    }
                }
            });
            configs.add(removedColumnConfig);

            final ColumnConfig inheritedColumnConfig = new ColumnConfig("inheritedFrom", 300);
            inheritedColumnConfig.setRenderer(new GridCellRenderer<PrincipalModelData>() {
                @Override
                public Object render(final PrincipalModelData model, String property, ColumnData config, int rowIndex, int colIndex, final ListStore store, final Grid grid) {
                    if (!breakAllInheritance) {
                        return model.getInheritedFrom();
                    } else {
                        return "";
                    }
                }
            });
            configs.add(inheritedColumnConfig);

            final ListStore<PrincipalModelData> listStore = stores.get(displayedRole);
            Grid<PrincipalModelData> g = new Grid<PrincipalModelData>(listStore, new ColumnModel(configs));
            g.getView().setAdjustForHScroll(false);
            g.setHideHeaders(true);
            g.setAutoExpandMax(1500);
            g.setAutoExpandColumn("displayName");
            grids.add(g);
            fs.add(g);

            final UserGroupAdder userGroupAdder = new UserGroupAdder(listStore,g);

            ToolBar toolBar = new ToolBar();
            Button addUsersToolItem = new Button(getAddUsersLabel());
            addUsersToolItem.setIcon(StandardIconsProvider.STANDARD_ICONS.user());
            addUsersToolItem.setEnabled(!readOnly);
            addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, context);
                }
            });
            toolBar.add(addUsersToolItem);

            addUsersToolItem = new Button(getAddGroupsLabel());
            addUsersToolItem.setIcon(StandardIconsProvider.STANDARD_ICONS.group());
            addUsersToolItem.setEnabled(!readOnly);
            addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, context);
                }
            });
            toolBar.add(addUsersToolItem);

            fs.add(toolBar);
            formPanel.add(fs);
        }

        ToolBar toolBar = new ToolBar();

        toolBar.add(breakinheritanceItem);
        toolBar.add(new FillToolItem());
        toolBar.add(restoreButton);
        formPanel.setTopComponent(toolBar);

        c.add(formPanel);
        c.layout();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                for (Grid grid : grids) {
                    resizeGrid(grid);
                }
            }
        });
    }


    /**
     * Set break inheritance label
     */
    private void setBreakInheritanceLabel() {
        if (breakAllInheritance) {
            breakinheritanceItem.setText(getResource("org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"));
        } else {
            breakinheritanceItem.setText(getResource("org.jahia.engines.rights.ManageRights.breakAllInheritance.label"));
        }
    }

    /**
     * Set dirty
     */
    public void setDirty() {
        restoreButton.setEnabled(true);
    }

    public List<String> getDisplayedRoles() {
        return displayedRoles;
    }

    /**
     * Get ACL object
     *
     * @return
     */
    public GWTJahiaNodeACL getAcl() {
        GWTJahiaNodeACL acl =  new GWTJahiaNodeACL(getEntries());
        acl.setBreakAllInheritance(breakAllInheritance);
        return acl;
    }

    /**
     * Get resource bundle
     *
     * @param key
     * @return
     */
    public String getResource(String key) {
        return Messages.get(key);
    }

    class PrincipalModelData extends BaseModelData {

        private PrincipalModelData(Map<String, Object> properties) {
            super(properties);
        }

        public PrincipalModelData(String key, Character type, String name, String displayName, Boolean inherited) {
            setKey(key);
            setType(type);
            setName(name);
            setDisplayName(displayName);
            setInherited(inherited);
            setRemoved(false);
        }

        public PrincipalModelData getClone() {
            return new PrincipalModelData(getProperties());
        }

        public String getName() {
            return get("name");
        }

        public void setName(String name) {
            set("name", name);
        }

        public String getDisplayName() {
            return get("displayName");
        }

        public void setDisplayName(String displayName) {
            set("displayName", displayName);
        }

        public Character getType() {
            return get("type");
        }

        public void setType(Character type) {
            set("type", type);
        }

        public String getKey() {
            return get("key");
        }

        public void setKey(String key) {
            set("key", key);
        }

        public Boolean getInherited() {
            return get("inherited");
        }

        public void setInherited(Boolean inherited) {
            set("inherited", inherited);
        }

        public String getInheritedFrom() {
            return get("inheritedFrom");
        }

        public void setInheritedFrom(String inheritedFrom) {
            set("inheritedFrom", inheritedFrom);
        }

        public Boolean getRemoved() {
            return get("removed");
        }

        public void setRemoved(Boolean removed) {
            set("removed", removed);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PrincipalModelData that = (PrincipalModelData) o;

            if (getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null) return false;
            if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = getKey() != null ? getKey().hashCode() : 0;
            result = 31 * result + (getType() != null ? getType().hashCode() : 0);
            return result;
        }
    }

    private void resizeGrid(Grid grid) {
        int h = 0;
        if (grid.getStore().getModels().size() > 0) {
            grid.show();
        } else {
            grid.hide();
        }
        for (int i = 0; i < grid.getStore().getModels().size(); i++) {
            h += grid.getView().getRow(i).getOffsetHeight();
        }
         grid.setHeight(h);
    }

    private class UserGroupAdder implements org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder {
        private final ListStore<PrincipalModelData> store;
        private final Grid<PrincipalModelData> grid;


        public UserGroupAdder(ListStore<PrincipalModelData> store, Grid<PrincipalModelData> grid) {
            this.store = store;
            this.grid = grid;
        }

        public void addUsers(List<GWTJahiaUser> users) {
            for (GWTJahiaUser user : users) {
                PrincipalModelData entry = new PrincipalModelData(user.getKey(), 'u', user.getName(), user.getDisplay(), false);

                if (!store.contains(entry)) {
                    store.add(entry);
                }
            }
            resizeGrid(grid);
            setDirty();
        }

        public void addGroups(List<GWTJahiaGroup> groups) {
            for (GWTJahiaGroup group : groups) {
                PrincipalModelData entry = new PrincipalModelData(group.getKey(), 'g', group.getName(), group.getDisplay(), false);
                if (!store.contains(entry)) {
                    store.add(entry);
                }
            }
            resizeGrid(grid);
            setDirty();
        }

    }
}
