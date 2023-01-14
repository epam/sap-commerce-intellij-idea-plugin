// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:AbstractFlowType interface.
 */
public interface AbstractFlow extends DomElement {

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
     * Returns the value of the model child.
     *
     * @return the value of the model child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("model")
    GenericAttributeValue<String> getModel();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the list of handler children.
     *
     * @return the list of handler children.
     */
    @NotNull
    @SubTagList("handler")
    java.util.List<ComposedHandler> getHandlers();

    /**
     * Adds new child to the list of handler children.
     *
     * @return created child
     */
    @SubTagList("handler")
    ComposedHandler addHandler();


    /**
     * Returns the value of the prepare child.
     *
     * @return the value of the prepare child.
     */
    @NotNull
    @SubTag("prepare")
    Prepare getPrepare();


    /**
     * Returns the list of step children.
     *
     * @return the list of step children.
     */
    @NotNull
    @SubTagList("step")
    java.util.List<Step> getSteps();

    /**
     * Adds new child to the list of step children.
     *
     * @return created child
     */
    @SubTagList("step")
    Step addStep();


    /**
     * Returns the list of subflow children.
     *
     * @return the list of subflow children.
     */
    @NotNull
    @SubTagList("subflow")
    java.util.List<Subflow> getSubflows();

    /**
     * Adds new child to the list of subflow children.
     *
     * @return created child
     */
    @SubTagList("subflow")
    Subflow addSubflow();


}
