// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/compareview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:editor-area-ctx interface.
 */
public interface EditorAreaCtx extends DomElement {

    /**
     * Returns the value of the ctx child.
     *
     * @return the value of the ctx child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("ctx")
    GenericAttributeValue<String> getCtx();


    /**
     * Returns the value of the use child.
     *
     * @return the value of the use child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("use")
    GenericAttributeValue<Boolean> getUse();


}
