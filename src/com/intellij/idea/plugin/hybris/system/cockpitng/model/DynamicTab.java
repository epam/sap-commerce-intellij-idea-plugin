// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicTab interface.
 */
public interface DynamicTab extends DomElement, AbstractDynamicElement {

    /**
     * Returns the value of the gotoTabId child.
     *
     * @return the value of the gotoTabId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("gotoTabId")
    GenericAttributeValue<String> getGotoTabId();


    /**
     * Returns the value of the fallbackGotoTabId child.
     *
     * @return the value of the fallbackGotoTabId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("fallbackGotoTabId")
    GenericAttributeValue<String> getFallbackGotoTabId();


    /**
     * Returns the value of the gotoTabIf child.
     *
     * @return the value of the gotoTabIf child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("gotoTabIf")
    GenericAttributeValue<String> getGotoTabIf();

}
