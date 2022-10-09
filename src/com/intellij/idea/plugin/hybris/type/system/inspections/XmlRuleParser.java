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

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class XmlRuleParser {

    private static final Logger LOG = Logger.getInstance(XmlRuleParser.class);

    public List<XmlRule> parseRules(final InputStream input) throws IOException {
        final SAXParser parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            final RulesHandler handler = new RulesHandler();
            parser.parse(input, handler);

            final List<XmlRule> rules = handler.getRules();
            final List<XmlRule> result = new ArrayList<>(rules.size());
            result.addAll(rules);

            return result;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Can't create SAX parser", e);
        }
    }

    private static class RulesHandler extends DefaultHandler {

        private final List<XmlRule> myRules = new ArrayList<>();

        public List<XmlRule> getRules() {
            return Collections.unmodifiableList(myRules);
        }

        @Override
        public void startElement(
            final String uri,
            final String localName,
            final String qName,
            final Attributes attributes
        ) {

            if ("rule".equals(qName)) {
                final String type = attributes.getValue("type");
                if ("XPATH".equals(type)) {
                    createRule(attributes).ifPresent(rule -> {
                        validate(rule);
                        myRules.add(rule);
                    });
                }
            }
        }


        public boolean validate(final XmlRule rule) {
            boolean isValid = this.validateNotNull("Missing name XPath", rule.getNameXPath());
            isValid &= this.validateNotNull("Missing selection XPath", rule.getSelectionXPath());
            isValid &= this.validateNotNull("Missing test XPath", rule.getTestXPath());
            return isValid;
        }

        private boolean validateNotNull(@NotNull final String problem, @Nullable final String subj) {
            boolean isValid = true;
            if (subj == null || subj.isEmpty()) {
                LOG.warn(problem + ": " + this);
                isValid = false;
            }
            return isValid;
        }

        private Optional<XmlRuleImpl> createRule(final Attributes attrs) {
            final String id = attrs.getValue("id");
            if (id == null) {
                LOG.warn("XPath validation rule without ID found, ignored: " + attrs);
                return Optional.empty();
            }

            final XmlRuleImpl result = new XmlRuleImpl(id);

            result.setSelectionXPath(attrs.getValue("selectionQuery"));
            result.setFailOnTestQuery(attrs.getValue("failOnTestQuery"));
            result.setTestXPath(attrs.getValue("testQuery"));
            result.setNameXPath(attrs.getValue("nameQuery"));

            return Optional.of(result);
        }
    }

    private static class XmlRuleImpl implements XmlRule {

        private final String myId;
        private String myNameXPath;
        private String mySelectionXPath;
        private String myTestXPath;
        private boolean failOnTestQuery;

        public XmlRuleImpl(@NotNull final String id) {
            this.myId = id;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " for :" + this.getID();
        }

        @NotNull
        @Override
        public String getID() {
            return this.myId;
        }

        @Override
        public String getNameXPath() {
            return this.myNameXPath;
        }

        void setNameXPath(final String nameXPath) {
            this.myNameXPath = nameXPath;
        }

        @Nullable
        @Override
        public String getTestXPath() {
            return this.myTestXPath;
        }

        void setTestXPath(final String testXPath) {
            this.myTestXPath = testXPath;
        }

        @Nullable
        @Override
        public String getSelectionXPath() {
            return this.mySelectionXPath;
        }

        public void setSelectionXPath(final String selectionXPath) {
            this.mySelectionXPath = selectionXPath;
        }

        @Override
        public boolean isFailOnTestQuery() {
            return failOnTestQuery;
        }

        public void setFailOnTestQuery(final String failOnTestQuery) {
            this.failOnTestQuery = Boolean.parseBoolean(failOnTestQuery);
        }

    }

}
