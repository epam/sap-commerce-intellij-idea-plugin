// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetRemoveAllEntry interface.
 * <pre>
 * <h3>Type null:WidgetRemoveAllEntry documentation</h3>
 * Indicates that all children widgets should be removed as a result of the widget extension.
 * </pre>
 */
public interface WidgetRemoveAllEntry extends DomElement {

    /**
     * Returns the value of the contextId child.
     *
     * @return the value of the contextId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("contextId")
    GenericAttributeValue<String> getContextId();


    /**
     * Returns the value of the includeSettings child.
     * <pre>
     * <h3>Attribute null:includeSettings documentation</h3>
     * Indicates if settings of the target widget should be cleared.
     * </pre>
     *
     * @return the value of the includeSettings child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("includeSettings")
    GenericAttributeValue<Boolean> getIncludeSettings();


}
