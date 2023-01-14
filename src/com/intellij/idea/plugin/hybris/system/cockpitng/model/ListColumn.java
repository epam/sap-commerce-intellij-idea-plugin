// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/listView

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/listView:list-column interface.
 */
public interface ListColumn extends DomElement, Positioned {

    /**
     * Returns the value of the qualifier child.
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the auto-extract child.
     *
     * @return the value of the auto-extract child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("auto-extract")
    GenericAttributeValue<Boolean> getAutoExtract();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the sortable child.
     *
     * @return the value of the sortable child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sortable")
    GenericAttributeValue<Boolean> getSortable();


    /**
     * Returns the value of the class child.
     *
     * @return the value of the class child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("class")
    GenericAttributeValue<String> getClazz();


    /**
     * Returns the value of the width child.
     *
     * @return the value of the width child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("width")
    GenericAttributeValue<String> getWidth();


    /**
     * Returns the value of the hflex child.
     *
     * @return the value of the hflex child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("hflex")
    GenericAttributeValue<String> getHflex();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    GenericAttributeValue<String> getSpringBean();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the maxChar child.
     *
     * @return the value of the maxChar child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("maxChar")
    GenericAttributeValue<Integer> getMaxChar();


    /**
     * Returns the value of the link child.
     *
     * @return the value of the link child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("link")
    GenericAttributeValue<Boolean> getLink();


    /**
     * Returns the value of the link-value child.
     *
     * @return the value of the link-value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("link-value")
    GenericAttributeValue<String> getLinkValue();

}
