// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:viewElemType interface.
 */
public interface View extends DomElement {

    /**
     * Returns the value of the simple content.
     *
     * @return the value of the simple content.
     */
    @NotNull
    @Required
    String getValue();

    /**
     * Sets the value of the simple content.
     *
     * @param value the new value to set
     */
    void setValue(@NotNull String value);


    /**
     * Returns the value of the src child.
     *
     * @return the value of the src child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("src")
    GenericAttributeValue<String> getSrcAttr();


    /**
     * Returns the value of the lang child.
     *
     * @return the value of the lang child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("lang")
    @Required
    GenericAttributeValue<Lang> getLang();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    GenericAttributeValue<String> getIdAttr();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the renderer child.
     *
     * @return the value of the renderer child.
     */
    @NotNull
    @SubTag("renderer")
    Renderer getRenderer();


    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the src child.
     *
     * @return the value of the src child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("src")
    @Required
    GenericAttributeValue<String> getSrc();


}
