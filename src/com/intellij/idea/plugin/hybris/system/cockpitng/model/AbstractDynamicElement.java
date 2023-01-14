// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:abstractDynamicElement interface.
 */
public interface AbstractDynamicElement extends DomElement, Positioned {

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
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the visibleIf child.
     *
     * @return the value of the visibleIf child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("visibleIf")
    GenericAttributeValue<String> getVisibleIf();


    /**
     * Returns the value of the disabledIf child.
     *
     * @return the value of the disabledIf child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("disabledIf")
    GenericAttributeValue<String> getDisabledIf();


    /**
     * Returns the value of the modelProperty child.
     * <pre>
     * <h3>Attribute null:modelProperty documentation</h3>
     * Overrides modelProperty attribute from dynamicForms element.
     * </pre>
     *
     * @return the value of the modelProperty child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("modelProperty")
    GenericAttributeValue<String> getModelProperty();


    /**
     * Returns the value of the triggeredOn child.
     * <pre>
     * <h3>Attribute null:triggeredOn documentation</h3>
     * Dynamic forms actions will be triggered on change of elements specified here as a comma
     * 							separated
     * 							values.
     * 							By default it is set to "*" therefore it is triggered on every model change.
     * </pre>
     *
     * @return the value of the triggeredOn child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("triggeredOn")
    GenericAttributeValue<String> getTriggeredOn();


    /**
     * Returns the value of the qualifier child.
     * <pre>
     * <h3>Attribute null:qualifier documentation</h3>
     * Qualifier name of element on which actions are performed.
     * </pre>
     *
     * @return the value of the qualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("qualifier")
    @Required
    GenericAttributeValue<String> getQualifier();


    /**
     * Returns the value of the scriptingConfig child.
     *
     * @return the value of the scriptingConfig child.
     */
    @NotNull
    @SubTag("scriptingConfig")
    ScriptingConfig getScriptingConfig();


}
