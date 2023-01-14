// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config:config interface.
 */
public interface Config extends DomElement {

    /**
     * Returns the value of the required-parameters child.
     *
     * @return the value of the required-parameters child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("required-parameters")
    GenericAttributeValue<String> getRequiredParameters();


    /**
     * Returns the list of requires children.
     *
     * @return the list of requires children.
     */
    @NotNull
    @SubTagList("requires")
    java.util.List<Requirement> getRequireses();

    /**
     * Adds new child to the list of requires children.
     *
     * @return created child
     */
    @SubTagList("requires")
    Requirement addRequires();


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


    /**
     * Returns the list of import children.
     *
     * @return the list of import children.
     */
    @NotNull
    @SubTagList("import")
    java.util.List<Import> getImports();

    /**
     * Adds new child to the list of import children.
     *
     * @return created child
     */
    @SubTagList("import")
    Import addImport();


}
