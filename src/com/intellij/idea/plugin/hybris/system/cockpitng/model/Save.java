// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:SaveType interface.
 */
public interface Save extends DomElement {

    /**
     * Returns the value of the property child.
     *
     * @return the value of the property child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("property")
    @Required
    GenericAttributeValue<String> getProperty();


}
