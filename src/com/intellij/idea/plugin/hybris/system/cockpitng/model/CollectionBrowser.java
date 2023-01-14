// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/collectionbrowser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/collectionbrowser:collection-browserElemType interface.
 */
public interface CollectionBrowser extends DomElement {

    /**
     * Returns the value of the enable-multi-select child.
     *
     * @return the value of the enable-multi-select child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("enable-multi-select")
    GenericAttributeValue<Boolean> getEnableMultiSelect();


    /**
     * Returns the value of the available-molds child.
     *
     * @return the value of the available-molds child.
     */
    @NotNull
    @SubTag("available-molds")
    MoldList getAvailableMolds();


}
