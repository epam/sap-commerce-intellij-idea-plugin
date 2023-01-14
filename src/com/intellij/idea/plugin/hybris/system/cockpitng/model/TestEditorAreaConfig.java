// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/test

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/test:testEditorAreaConfig interface.
 */
public interface TestEditorAreaConfig extends DomElement {

    /**
     * Returns the list of groups children.
     *
     * @return the list of groups children.
     */
    @NotNull
    @SubTagList("groups")
    java.util.List<Group> getGroupses();

    /**
     * Adds new child to the list of groups children.
     *
     * @return created child
     */
    @SubTagList("groups")
    Group addGroups();


}
