// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:AdditionalParamType interface.
 */
public interface AdditionalParam extends DomElement {

    /**
     * Returns the value of the key child.
     *
     * @return the value of the key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("key")
    GenericAttributeValue<String> getKey();


    /**
     * Returns the value of the value child.
     *
     * @return the value of the value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value")
    GenericAttributeValue<String> getValue();


}
