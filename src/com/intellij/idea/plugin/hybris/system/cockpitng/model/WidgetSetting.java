// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetSetting interface.
 * <pre>
 * <h3>Type null:WidgetSetting documentation</h3>
 * Custom widget instance settings.
 * </pre>
 */
public interface WidgetSetting extends DomElement {

    /**
     * Returns the value of the key child.
     * <pre>
     * <h3>Attribute null:key documentation</h3>
     * Setting key.
     * </pre>
     *
     * @return the value of the key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("key")
    @Required
    GenericAttributeValue<String> getKey();


    /**
     * Returns the value of the value child.
     * <pre>
     * <h3>Attribute null:value documentation</h3>
     * Setting value.
     * </pre>
     *
     * @return the value of the value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value")
    GenericAttributeValue<String> getValue();


    /**
     * Returns the value of the type child.
     * <pre>
     * <h3>Attribute null:type documentation</h3>
     * Setting type (String, Boolean, Integer or Double).
     * </pre>
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<Setting> getType();


}
