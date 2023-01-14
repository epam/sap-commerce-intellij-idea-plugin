// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/refineBy

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/refineBy:facet-configElemType interface.
 */
public interface FacetConfig extends DomElement {

    /**
     * Returns the value of the facets child.
     *
     * @return the value of the facets child.
     */
    @NotNull
    @SubTag("facets")
    Facets getFacets();


    /**
     * Returns the value of the blacklist child.
     *
     * @return the value of the blacklist child.
     */
    @NotNull
    @SubTag("blacklist")
    Blacklist getBlacklist();


}
