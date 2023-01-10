
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

package com.intellij.idea.plugin.hybris.business.process.jaxb;

import com.intellij.idea.plugin.hybris.business.process.jaxb.model.BpGenericAction;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contextParameter" type="{http://www.hybris.de/xsd/processdefinition}contextParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://www.hybris.de/xsd/processdefinition}nodes" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.hybris.de/xsd/processdefinition}name" />
 *       &lt;attribute name="start" use="required" type="{http://www.hybris.de/xsd/processdefinition}name" />
 *       &lt;attribute name="onError" type="{http://www.hybris.de/xsd/processdefinition}name" />
 *       &lt;attribute name="processClass" type="{http://www.hybris.de/xsd/processdefinition}name" default="de.hybris.platform.processengine.model.BusinessProcessModel" />
 *       &lt;attribute name="defaultNodeGroup" type="{http://www.hybris.de/xsd/processdefinition}name" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contextParameter",
    "nodes"
})
@XmlRootElement(name = "process", namespace = "http://www.hybris.de/xsd/processdefinition")
public class Process {

    @XmlElement(namespace = "http://www.hybris.de/xsd/processdefinition")
    protected List<ContextParameter> contextParameter;
    @XmlElements({
        @XmlElement(name = "scriptAction", namespace = "http://www.hybris.de/xsd/processdefinition", type = ScriptAction.class),
        @XmlElement(name = "notify", namespace = "http://www.hybris.de/xsd/processdefinition", type = Notify.class),
        @XmlElement(name = "join", namespace = "http://www.hybris.de/xsd/processdefinition", type = Join.class),
        @XmlElement(name = "wait", namespace = "http://www.hybris.de/xsd/processdefinition", type = Wait.class),
        @XmlElement(name = "split", namespace = "http://www.hybris.de/xsd/processdefinition", type = Split.class),
        @XmlElement(name = "end", namespace = "http://www.hybris.de/xsd/processdefinition", type = End.class),
        @XmlElement(name = "action", namespace = "http://www.hybris.de/xsd/processdefinition", type = Action.class)
    })
    protected List<BpGenericAction> nodes;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String start;
    @XmlAttribute
    protected String onError;
    @XmlAttribute
    protected String processClass;
    @XmlAttribute
    protected String defaultNodeGroup;

    /**
     * Gets the value of the contextParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contextParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContextParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContextParameter }
     * 
     * 
     */
    public List<ContextParameter> getContextParameter() {
        if (contextParameter == null) {
            contextParameter = new ArrayList<ContextParameter>();
        }
        return this.contextParameter;
    }

    /**
     * Gets the value of the nodes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nodes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNodes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ScriptAction }
     * {@link Notify }
     * {@link Join }
     * {@link Wait }
     * {@link Split }
     * {@link End }
     * {@link Action }
     * 
     * 
     */
    public List<BpGenericAction> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<BpGenericAction>();
        }
        return this.nodes;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the onError property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnError() {
        return onError;
    }

    /**
     * Sets the value of the onError property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnError(String value) {
        this.onError = value;
    }

    /**
     * Gets the value of the processClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessClass() {
        if (processClass == null) {
            return "de.hybris.platform.processengine.model.BusinessProcessModel";
        } else {
            return processClass;
        }
    }

    /**
     * Sets the value of the processClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessClass(String value) {
        this.processClass = value;
    }

    /**
     * Gets the value of the defaultNodeGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultNodeGroup() {
        return defaultNodeGroup;
    }

    /**
     * Sets the value of the defaultNodeGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultNodeGroup(String value) {
        this.defaultNodeGroup = value;
    }

}
