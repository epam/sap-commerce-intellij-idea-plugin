// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/viewSwitcher

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/viewSwitcher:view-switcherElemType interface.
 */
public interface ViewSwitcher extends DomElement {

    /**
     * Returns the list of authority children.
     *
     * @return the list of authority children.
     */
    @NotNull
    @SubTagList("authority")
    java.util.List<Authority> getAuthorities();

    /**
     * Adds new child to the list of authority children.
     *
     * @return created child
     */
    @SubTagList("authority")
    Authority addAuthority();


}
