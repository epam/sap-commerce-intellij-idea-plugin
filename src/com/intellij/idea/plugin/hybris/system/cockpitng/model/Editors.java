// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:editors interface.
 */
public interface Editors extends DomElement {

    /**
     * Returns the list of group children.
     *
     * @return the list of group children.
     */
    @NotNull
    @SubTagList("group")
    java.util.List<EditorGroup> getGroups();

    /**
     * Adds new child to the list of group children.
     *
     * @return created child
     */
    @SubTagList("group")
    EditorGroup addGroup();


}
