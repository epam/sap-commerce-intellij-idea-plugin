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

package com.intellij.idea.plugin.hybris.type.system.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

public class ValidateContextImpl implements ValidateContext {

    private final InspectionManager inspectionManager;
    @NotNull
    private final XmlFile xmlFile;
    private final Document document;
    private final boolean isOnTheFly;

    public ValidateContextImpl(
        @NotNull final InspectionManager manager,
        @NotNull final XmlFile psiFile,
        @NotNull final Document document,
        final boolean isOnTheFly
    ) {
        this.inspectionManager = manager;
        this.xmlFile = psiFile;
        this.document = document;
        this.isOnTheFly = isOnTheFly;
    }

    @Nullable
    public static ValidateContext createFileContext(
        @NotNull final InspectionManager manager,
        final boolean isOnTheFly,
        @SuppressWarnings("TypeMayBeWeakened") @NotNull final XmlFile psiFile
    ) {
        final Document mappedDocument;
        try (StringReader reader = new StringReader(psiFile.getText())) {
            mappedDocument = buildMappedDocument(new InputSource(reader));
        } catch (SAXException | IOException e) {
            //cause will be probably reported by xml integration
            return null;
        }
        return new ValidateContextImpl(manager, psiFile, mappedDocument, isOnTheFly);
    }

    private static Document buildMappedDocument(final InputSource source) throws SAXException, IOException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        final Document result;
        try {
            final SAXParser sp = factory.newSAXParser();
            result = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            sp.parse(source, new MappedDocumentBuilder(result));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Invalid Parser Configuration", e);
        }
        return result;
    }

    @Override
    public boolean isOnTheFly() {
        return this.isOnTheFly;
    }

    @NotNull
    @Override
    public Document getDocument() {
        return this.document;
    }

    @NotNull
    @Override
    public InspectionManager getManager() {
        return this.inspectionManager;
    }

    @NotNull
    @Override
    public PsiElement mapNodeToPsi(@NotNull final Node xmlNode) {
        final PsiElement result = findByLineAndColumn(this.xmlFile, MappedDocumentBuilder.START_LOC.get(xmlNode));
        return result == null ? this.xmlFile : result;
    }

    @Nullable
    private static PsiElement findByLineAndColumn(
        @NotNull final PsiElement file,
        @Nullable final Point columnAndLine
    ) {
        if (columnAndLine == null) {
            return file;
        }
        final int line = columnAndLine.y - 1;
        final int column = columnAndLine.x - 1;

        PsiElement leaf = findByLineAndColumn(file, line, column);

        if (leaf instanceof PsiWhiteSpace) {
            leaf = PsiTreeUtil.prevVisibleLeaf(leaf);
        }

        final PsiElement tag = leaf instanceof XmlTag ? leaf : PsiTreeUtil.getParentOfType(leaf, XmlTag.class);
        return tag == null ? leaf : tag;
    }

    private static PsiElement findByLineAndColumn(@NotNull final PsiElement file, final int line, final int column) {
        final String fullText = file.getText();
        int offset = StringUtil.lineColToOffset(fullText, line, column);

        if (offset < 0) {
            offset = StringUtil.lineColToOffset(fullText, line, 0);
        }

        return file.findElementAt(offset);
    }

    public static class NodeKey<T> {

        private final String myName;

        public NodeKey(@NotNull final Class<?> contextClazz, @NotNull final String name) {
            this(contextClazz.getName() + ':' + name);
        }

        public NodeKey(@NotNull final String name) {
            this.myName = name;
        }

        public T get(@NotNull final Node node) {
            //noinspection unchecked
            return (T) node.getUserData(this.myName);
        }

        public void put(@NotNull final Node node, final T value) {
            node.setUserData(this.myName, value, null);
        }

    }

    public static class MappedDocumentBuilder extends DefaultHandler {

        public static final NodeKey<Point> START_LOC = new NodeKey<>(MappedDocumentBuilder.class, "Start");
        public static final NodeKey<Point> END_LOC = new NodeKey<>(MappedDocumentBuilder.class, "End");

        private Document myDoc;
        private Locator myLocator;
        private LinkedList<Node> myElements = new LinkedList<>();

        public MappedDocumentBuilder(final Document doc) {
            this.myDoc = doc;
            this.myElements.addFirst(this.myDoc);
        }

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.myLocator = locator;
        }

        @Override
        public void startElement(
            final String uri, final String localName, final String qName, final Attributes attributes
        ) {

            final Element currentElement;
            if (localName != null && !localName.isEmpty()) {
                currentElement = this.myDoc.createElementNS(uri, localName);
            } else {
                currentElement = this.myDoc.createElement(qName);
            }

            this.markLocation(currentElement, START_LOC);
            this.appendElement(currentElement);

            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    final Attr nextAttr = this.createAndAppendAttribute(currentElement, attributes, i);
                    nextAttr.setValue(attributes.getValue(i));
                    this.markLocation(nextAttr, START_LOC);
                }
            }
        }

        protected void markLocation(@NotNull final Node node, @NotNull final NodeKey<Point> key) {
            if (this.myLocator != null) {
                final int line = this.myLocator.getLineNumber();
                final int column = this.myLocator.getColumnNumber();
                final Point point = new Point(column, line);
                key.put(node, point);
            }
        }

        @SuppressWarnings("TypeMayBeWeakened")
        private void appendElement(@NotNull final Element childElement) {
            final Node last = this.myElements.getLast();
            last.appendChild(childElement);
            this.myElements.addLast(childElement);
        }

        private Attr createAndAppendAttribute(
            @NotNull final Element owner,
            @NotNull final Attributes attrs,
            final int idx
        ) {
            final String localName = attrs.getLocalName(idx);
            final Attr result;
            if (StringUtil.isEmpty(localName)) {
                result = this.myDoc.createAttribute(attrs.getQName(idx));
                owner.setAttributeNode(result);
            } else {
                result = this.myDoc.createAttributeNS(attrs.getURI(idx), localName);
                owner.setAttributeNodeNS(result);
            }
            return result;
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            final Node last = this.myElements.removeLast();
            this.markLocation(last, END_LOC);

            if (this.myElements.isEmpty()) {
                last.normalize();
            }
        }

        @Override
        public void characters(
            @SuppressWarnings("StandardVariableNames") final char[] ch,
            final int start,
            final int length
        ) {
            if (this.myElements.isEmpty()) {
                //should never happen ?
                return;
            }
            final Text text = this.myDoc.createTextNode(new String(ch, start, length));
            this.markLocation(text, START_LOC);
            this.myElements.peekLast().appendChild(text);
        }

    }

}