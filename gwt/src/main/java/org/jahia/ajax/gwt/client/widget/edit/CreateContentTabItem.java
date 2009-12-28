package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * Side panel tab that allows creation of new content items using drag and drop.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class CreateContentTabItem extends SidePanelTabItem {

    private ContentTypeTree contentTypeTree;
    private CreateGridDragSource gridDragSource;

    CreateContentTabItem() {
        setIcon(ContentModelIconProvider.CONTENT_ICONS.content());        
        setLayout(new FitLayout());
        contentTypeTree = new ContentTypeTree(null, null, "jnt:content", null, true, false, 400, 0, 25);

        add(contentTypeTree);
        gridDragSource = new CreateGridDragSource(contentTypeTree.getTreeGrid());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        contentTypeTree.setLinker(linker);
        gridDragSource.addDNDListener(linker.getDndListener());
    }

    public void refresh() {
        contentTypeTree.refresh();
    }
}
