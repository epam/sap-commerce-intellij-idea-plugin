// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:widgetsElemType interface.
 */
public interface Widgets extends DomElement {

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
     * Returns the list of widget children.
     *
     * @return the list of widget children.
     */
    @NotNull
    @SubTagList("widget")
    java.util.List<Widget> getWidgets();

    /**
     * Adds new child to the list of widget children.
     *
     * @return created child
     */
    @SubTagList("widget")
    Widget addWidget();


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


    /**
     * Returns the list of widget-extension children.
     *
     * @return the list of widget-extension children.
     */
    @NotNull
    @SubTagList("widget-extension")
    java.util.List<WidgetExtension> getWidgetExtensions();

    /**
     * Adds new child to the list of widget-extension children.
     *
     * @return created child
     */
    @SubTagList("widget-extension")
    WidgetExtension addWidgetExtension();


    /**
     * Returns the list of widget-connection children.
     *
     * @return the list of widget-connection children.
     */
    @NotNull
    @SubTagList("widget-connection")
    java.util.List<WidgetConnection> getWidgetConnections();

    /**
     * Adds new child to the list of widget-connection children.
     *
     * @return created child
     */
    @SubTagList("widget-connection")
    WidgetConnection addWidgetConnection();


    /**
     * Returns the list of widget-connection-remove children.
     *
     * @return the list of widget-connection-remove children.
     */
    @NotNull
    @SubTagList("widget-connection-remove")
    java.util.List<WidgetConnectionRemove> getWidgetConnectionRemoves();

    /**
     * Adds new child to the list of widget-connection-remove children.
     *
     * @return created child
     */
    @SubTagList("widget-connection-remove")
    WidgetConnectionRemove addWidgetConnectionRemove();


}
