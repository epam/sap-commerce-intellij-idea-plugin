/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.tools.remote.http.impex;

import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.HttpStatus.SC_OK;

public class HybrisHttpResult {

    private boolean hasError;
    private String errorMessage;
    private String detailMessage;

    private String output;
    private String result;
    private int statusCode;
    private Collection<Pair<String, String>> headers;

    private HybrisHttpResult() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOutput() {
        return output;
    }

    public String getResult() {
        return result;
    }

    public Collection<Pair<String, String>> getHeaders() {
        return headers;
    }

    static public class HybrisHttpResultBuilder {

        private boolean hasError = false;
        private String errorMessage= EMPTY;
        private String detailMessage= EMPTY;

        private String output= EMPTY;
        private String result= EMPTY;
        private int statusCode = SC_OK;
        private Collection<Pair<String, String>> headers = new ArrayList<>();

        private HybrisHttpResultBuilder() {
        }

        public static HybrisHttpResultBuilder createResult() {
            return new HybrisHttpResultBuilder();
        }

        public HybrisHttpResultBuilder errorMessage(final String errorMessage) {
            if (isNotEmpty(errorMessage)) {
                this.errorMessage = errorMessage;
                this.hasError = true;
            }
            return this;
        }

        public HybrisHttpResultBuilder detailMessage(final String detailMessage) {
            if (isNotEmpty(detailMessage)) {
                this.detailMessage = detailMessage;
                this.hasError = true;
            }
            return this;
        }

        public HybrisHttpResultBuilder output(final String output) {
            this.output = output;
            return this;
        }

        public HybrisHttpResultBuilder result(final String result) {
            this.result = result;
            return this;
        }

        public HybrisHttpResultBuilder httpCode(final int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public HybrisHttpResultBuilder headers(final Collection<Pair<String, String>> headers) {
            this.headers.addAll(headers);
            return this;
        }

        public HybrisHttpResult build() {
            final HybrisHttpResult httpResult = new HybrisHttpResult();
            httpResult.hasError = this.hasError;
            httpResult.errorMessage = this.errorMessage;
            httpResult.detailMessage = this.detailMessage;
            httpResult.output = this.output;
            httpResult.result = this.result;
            httpResult.statusCode = this.statusCode;
            httpResult.headers = this.headers;

            return httpResult;
        }

    }
}
