// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/gridView

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/gridView:renderer interface.
 */
public interface Renderer extends DomElement {

    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeModeAttr();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPositionAttr();


    /**
     * Returns the list of parameter children.
     *
     * @return the list of parameter children.
     */
    @NotNull
    @SubTagList("parameter")
    java.util.List<Parameter> getParameters();

    /**
     * Adds new child to the list of parameter children.
     *
     * @return created child
     */
    @SubTagList("parameter")
    Parameter addParameter();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    @Required
    GenericAttributeValue<String> getSpringBeanAttr();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


    /**
     * Returns the value of the position child.
     *
     * @return the value of the position child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("position")
    GenericAttributeValue<Integer> getPosition();


    /**
     * Returns the value of the spring-bean child.
     *
     * @return the value of the spring-bean child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("spring-bean")
    @Required
    GenericAttributeValue<String> getSpringBean();


}
