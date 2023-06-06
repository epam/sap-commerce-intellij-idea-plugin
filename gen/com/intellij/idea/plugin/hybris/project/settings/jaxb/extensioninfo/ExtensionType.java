/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.project.settings.jaxb.extensioninfo;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * Configures the available modules of the extension.
 * <p/>
 * <p>Java class for extensionType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="extensionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requires-extension" type="{}requires-extensionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="coremodule" type="{}coremoduleType" minOccurs="0"/>
 *         &lt;element name="webmodule" type="{}webmoduleType" minOccurs="0"/>
 *         &lt;element name="hmcmodule" type="{}hmcmoduleType" minOccurs="0"/>
 *         &lt;element name="meta" type="{}metaType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="classprefix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="abstractclassprefix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="isoldstyleextension" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="requiredbyall" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="managername" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="managersuperclass" type="{}classType" />
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="usemaven" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jaloLogicFree" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extensionType", propOrder = {
    "requiresExtension",
    "coremodule",
    "webmodule",
    "hmcmodule",
    "meta"
})
public class ExtensionType {

    @XmlElement(name = "requires-extension")
    protected List<RequiresExtensionType> requiresExtension;
    protected CoreModuleType coremodule;
    protected WebModuleType webmodule;
    protected HmcModuleType hmcmodule;
    protected List<MetaType> meta;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String version;
    @XmlAttribute
    protected String classprefix;
    @XmlAttribute
    protected String abstractclassprefix;
    @XmlAttribute
    protected Boolean isoldstyleextension;
    @XmlAttribute
    protected Boolean requiredbyall;
    @XmlAttribute
    protected String managername;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String managersuperclass;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected String usemaven;
    @XmlAttribute
    protected Boolean jaloLogicFree;

    /**
     * Gets the value of the requiresExtension property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requiresExtension property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequiresExtension().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link RequiresExtensionType }
     */
    public List<RequiresExtensionType> getRequiresExtension() {
        if (requiresExtension == null) {
            requiresExtension = new ArrayList<RequiresExtensionType>();
        }
        return this.requiresExtension;
    }

    /**
     * Gets the value of the coremodule property.
     *
     * @return possible object is
     * {@link CoreModuleType }
     */
    public CoreModuleType getCoremodule() {
        return coremodule;
    }

    /**
     * Sets the value of the coremodule property.
     *
     * @param value allowed object is
     *              {@link CoreModuleType }
     */
    public void setCoremodule(CoreModuleType value) {
        this.coremodule = value;
    }

    /**
     * Gets the value of the webmodule property.
     *
     * @return possible object is
     * {@link WebModuleType }
     */
    public WebModuleType getWebmodule() {
        return webmodule;
    }

    /**
     * Sets the value of the webmodule property.
     *
     * @param value allowed object is
     *              {@link WebModuleType }
     */
    public void setWebmodule(WebModuleType value) {
        this.webmodule = value;
    }

    /**
     * Gets the value of the hmcmodule property.
     *
     * @return possible object is
     * {@link HmcModuleType }
     */
    public HmcModuleType getHmcmodule() {
        return hmcmodule;
    }

    /**
     * Sets the value of the hmcmodule property.
     *
     * @param value allowed object is
     *              {@link HmcModuleType }
     */
    public void setHmcmodule(HmcModuleType value) {
        this.hmcmodule = value;
    }

    /**
     * Gets the value of the meta property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meta property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeta().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MetaType }
     */
    public List<MetaType> getMeta() {
        if (meta == null) {
            meta = new ArrayList<MetaType>();
        }
        return this.meta;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the classprefix property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getClassprefix() {
        return classprefix;
    }

    /**
     * Sets the value of the classprefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClassprefix(String value) {
        this.classprefix = value;
    }

    /**
     * Gets the value of the abstractclassprefix property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAbstractclassprefix() {
        return abstractclassprefix;
    }

    /**
     * Sets the value of the abstractclassprefix property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAbstractclassprefix(String value) {
        this.abstractclassprefix = value;
    }

    /**
     * Gets the value of the isoldstyleextension property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isIsoldstyleextension() {
        return isoldstyleextension;
    }

    /**
     * Sets the value of the isoldstyleextension property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIsoldstyleextension(Boolean value) {
        this.isoldstyleextension = value;
    }

    /**
     * Gets the value of the requiredbyall property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public boolean isRequiredbyall() {
        if (requiredbyall == null) {
            return false;
        } else {
            return requiredbyall;
        }
    }

    /**
     * Sets the value of the requiredbyall property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setRequiredbyall(Boolean value) {
        this.requiredbyall = value;
    }

    /**
     * Gets the value of the managername property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManagername() {
        return managername;
    }

    /**
     * Sets the value of the managername property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManagername(String value) {
        this.managername = value;
    }

    /**
     * Gets the value of the managersuperclass property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManagersuperclass() {
        return managersuperclass;
    }

    /**
     * Sets the value of the managersuperclass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManagersuperclass(String value) {
        this.managersuperclass = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the usemaven property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUsemaven() {
        return usemaven;
    }

    /**
     * Sets the value of the usemaven property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUsemaven(String value) {
        this.usemaven = value;
    }

    /**
     * Gets the value of the jaloLogicFree property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public boolean isJaloLogicFree() {
        if (jaloLogicFree == null) {
            return false;
        } else {
            return jaloLogicFree;
        }
    }

    /**
     * Sets the value of the jaloLogicFree property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setJaloLogicFree(Boolean value) {
        this.jaloLogicFree = value;
    }

}
