// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetDefinition interface.
 */
public interface WidgetDefinition extends DomElement, ComponentDefinition {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @SubTag("name")
    GenericDomValue<String> getName();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the author child.
     *
     * @return the value of the author child.
     */
    @NotNull
    @SubTag("author")
    GenericDomValue<String> getAuthor();


    /**
     * Returns the value of the version child.
     *
     * @return the value of the version child.
     */
    @NotNull
    @SubTag("version")
    GenericDomValue<String> getVersion();


    /**
     * Returns the value of the keywords child.
     *
     * @return the value of the keywords child.
     */
    @NotNull
    @SubTag("keywords")
    Keywords getKeywords();


    /**
     * Returns the value of the settings child.
     *
     * @return the value of the settings child.
     */
    @NotNull
    @SubTag("settings")
    Settings getSettings();


    /**
     * Returns the value of the defaultTitle child.
     *
     * @return the value of the defaultTitle child.
     */
    @NotNull
    @SubTag("defaultTitle")
    GenericDomValue<String> getDefaultTitle();


    /**
     * Returns the value of the view child.
     *
     * @return the value of the view child.
     */
    @NotNull
    @SubTag("view")
    View getView();


    /**
     * Returns the value of the controller child.
     *
     * @return the value of the controller child.
     */
    @NotNull
    @SubTag("controller")
    Controller getController();


    /**
     * Returns the value of the sockets child.
     *
     * @return the value of the sockets child.
     */
    @NotNull
    @SubTag("sockets")
    Sockets getSockets();


}
