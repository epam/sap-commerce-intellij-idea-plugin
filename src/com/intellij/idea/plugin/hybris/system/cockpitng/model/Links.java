// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/links

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/links:linksElemType interface.
 */
public interface Links extends DomElement {

    /**
     * Returns the list of link children.
     *
     * @return the list of link children.
     */
    @NotNull
    @SubTagList("link")
    java.util.List<Link> getLinks();

    /**
     * Adds new child to the list of link children.
     *
     * @return created child
     */
    @SubTagList("link")
    Link addLink();


}
