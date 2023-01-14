// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/simplelist

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/simplelist:list-slot interface.
 */
public interface ListSlot extends DomElement {

    /**
     * Returns the value of the field child.
     *
     * @return the value of the field child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("field")
    @Required
    GenericAttributeValue<String> getField();


}
