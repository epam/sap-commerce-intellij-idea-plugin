// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/refineBy

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/refineBy:blacklist interface.
 */
public interface Blacklist extends DomElement {

    /**
     * Returns the list of facet children.
     *
     * @return the list of facet children.
     */
    @NotNull
    @SubTagList("facet")
    java.util.List<Facet> getFacets();

    /**
     * Adds new child to the list of facet children.
     *
     * @return created child
     */
    @SubTagList("facet")
    Facet addFacet();


}
