// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.idea.plugin.hybris.system.cockpitng.file.ComponentConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config:context interface.
 */
public interface Context extends DomElement {

    /**
     * Returns the value of the merge-by child.
     *
     * @return the value of the merge-by child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-by")
    GenericAttributeValue<String> getMergeBy();

    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("type")
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the principal child.
     *
     * @return the value of the principal child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("principal")
    GenericAttributeValue<String> getPrincipal();


    /**
     * Returns the value of the component child.
     *
     * @return the value of the component child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("component")
    @Convert(ComponentConverter.class)
    GenericAttributeValue<String> getComponent();


    /**
     * Returns the value of the authority child.
     *
     * @return the value of the authority child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("authority")
    GenericAttributeValue<String> getAuthority();


    /**
     * Returns the list of context children.
     *
     * @return the list of context children.
     */
    @NotNull
    @SubTagList("context")
    java.util.List<Context> getContexts();

    /**
     * Adds new child to the list of context children.
     *
     * @return created child
     */
    @SubTagList("context")
    Context addContext();


}
