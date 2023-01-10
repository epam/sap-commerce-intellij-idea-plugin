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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Configures a core module for the extension. A core module consists of an items.xml file (and therefore allows to add new types to the system), a manager class, classes for the JaLo Layer and the ServiceLayer and JUnit test classes. The following directories are required: /src, /resources, /testsrc.
 * <p/>
 * <p>Java class for coremoduleType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="coremoduleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="additionalclasspath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="packageroot" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="manager" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sourceavailable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="generated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="java5" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="generatePartOf" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "coremoduleType")
public class CoreModuleType {

    @XmlAttribute
    protected String additionalclasspath;
    @XmlAttribute
    protected String packageroot;
    @XmlAttribute
    protected String manager;
    @XmlAttribute
    protected Boolean sourceavailable;
    @XmlAttribute
    protected Boolean generated;
    @XmlAttribute
    protected Boolean java5;
    @XmlAttribute
    protected Boolean generatePartOf;

    /**
     * Gets the value of the additionalclasspath property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdditionalclasspath() {
        return additionalclasspath;
    }

    /**
     * Sets the value of the additionalclasspath property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdditionalclasspath(String value) {
        this.additionalclasspath = value;
    }

    /**
     * Gets the value of the packageroot property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPackageroot() {
        return packageroot;
    }

    /**
     * Sets the value of the packageroot property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPackageroot(String value) {
        this.packageroot = value;
    }

    /**
     * Gets the value of the manager property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManager() {
        return manager;
    }

    /**
     * Sets the value of the manager property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManager(String value) {
        this.manager = value;
    }

    /**
     * Gets the value of the sourceavailable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSourceavailable() {
        return sourceavailable;
    }

    /**
     * Sets the value of the sourceavailable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSourceavailable(Boolean value) {
        this.sourceavailable = value;
    }

    /**
     * Gets the value of the generated property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isGenerated() {
        return generated;
    }

    /**
     * Sets the value of the generated property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setGenerated(Boolean value) {
        this.generated = value;
    }

    /**
     * Gets the value of the java5 property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isJava5() {
        return java5;
    }

    /**
     * Sets the value of the java5 property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setJava5(Boolean value) {
        this.java5 = value;
    }

    /**
     * Gets the value of the generatePartOf property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isGeneratePartOf() {
        return generatePartOf;
    }

    /**
     * Sets the value of the generatePartOf property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setGeneratePartOf(Boolean value) {
        this.generatePartOf = value;
    }

}
