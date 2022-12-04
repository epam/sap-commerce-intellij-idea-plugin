// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.beans.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * null:property interface.
 */
public interface Property extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @Required
    //NOTE: We have to avoid @Convert since PsiField is in the read-only file and thus can't be renamed by platform
    //NOTE: Instead we are renaming the attribute value itself, see BeansRenamePsiElementProcessor
    //@Convert(soft = true, value = BeansPropertyNameConverter.class)
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the type child.
     *
     * @return the value of the type child.
     */
    @NotNull
    @Required
    GenericAttributeValue<String> getType();


    /**
     * Returns the value of the equals child.
     *
     * @return the value of the equals child.
     */
    @NotNull
    GenericAttributeValue<Boolean> getEquals();


    /**
     * Returns the value of the deprecated child.
     * <pre>
     * <h3>Attribute null:deprecated documentation</h3>
     * Marks property as deprecated. Allows defining a message.
     * </pre>
     *
     * @return the value of the deprecated child.
     */
    @NotNull
    GenericAttributeValue<Boolean> getDeprecated();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    GenericDomValue<String> getDescription();


    /**
     * Returns the list of annotations children.
     *
     * @return the list of annotations children.
     */
    @NotNull
    List<Annotations> getAnnotationses();

    @NotNull
    @SubTag("hints")
    Hints getHints();

    /**
     * Adds new child to the list of annotations children.
     *
     * @return created child
     */
    Annotations addAnnotations();


}
