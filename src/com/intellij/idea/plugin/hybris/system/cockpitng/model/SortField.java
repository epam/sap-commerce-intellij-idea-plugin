// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/advancedsearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/advancedsearch:SortFieldType interface.
 */
public interface SortField extends DomElement, Field {

    /**
     * Returns the value of the asc child.
     *
     * @return the value of the asc child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("asc")
    @Required
    GenericAttributeValue<Boolean> getAscAttr();


    /**
     * Returns the value of the asc child.
     *
     * @return the value of the asc child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("asc")
    @Required
    GenericAttributeValue<Boolean> getAsc();

}
