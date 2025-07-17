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

package com.intellij.idea.plugin.hybris.tools.remote.execution;

import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType;
import com.intellij.idea.plugin.hybris.tools.remote.execution.groovy.ReplicaContext;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.HttpStatus.SC_OK;

public class ExecutionResult {

    private boolean hasError;
    private String errorMessage;
    private String detailMessage;

    private String output;
    private String result;
    private int statusCode;
    private RemoteConnectionType remoteConnectionType;
    private ReplicaContext replicaContext;

    private ExecutionResult() {
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

    public RemoteConnectionType getRemoteConnectionType() {
        return remoteConnectionType;
    }

    @Nullable
    public ReplicaContext getReplicaContext() {
        return replicaContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    static public class Builder {

        private boolean hasError = false;
        private String errorMessage = EMPTY;
        private String detailMessage = EMPTY;

        private String output = EMPTY;
        private String result = EMPTY;
        private int statusCode = SC_OK;

        private RemoteConnectionType remoteConnectionType;
        private ReplicaContext replicaContext;

        private Builder() {
        }

        public Builder errorMessage(final String errorMessage) {
            if (isNotEmpty(errorMessage)) {
                this.errorMessage = errorMessage;
                this.hasError = true;
            }
            return this;
        }

        public Builder detailMessage(final String detailMessage) {
            if (isNotEmpty(detailMessage)) {
                this.detailMessage = detailMessage;
                this.hasError = true;
            }
            return this;
        }

        public Builder output(final String output) {
            this.output = output;
            return this;
        }

        public Builder result(final String result) {
            this.result = result;
            return this;
        }

        public Builder httpCode(final int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder ok() {
            this.statusCode = HttpStatus.SC_OK;
            return this;
        }

        public Builder badRequest() {
            this.statusCode = HttpStatus.SC_BAD_REQUEST;
            return this;
        }

        public Builder replicaContext(final ReplicaContext replicaContext) {
            this.replicaContext = replicaContext;
            return this;
        }

        public Builder remoteConnectionType(final RemoteConnectionType remoteConnectionType) {
            this.remoteConnectionType = remoteConnectionType;
            return this;
        }

        public ExecutionResult build() {
            final ExecutionResult httpResult = new ExecutionResult();
            httpResult.hasError = this.hasError;
            httpResult.errorMessage = this.errorMessage;
            httpResult.detailMessage = this.detailMessage;
            httpResult.output = this.output;
            httpResult.result = this.result;
            httpResult.statusCode = this.statusCode;
            httpResult.replicaContext = this.replicaContext;
            httpResult.remoteConnectionType = this.remoteConnectionType;

            return httpResult;
        }

    }
}
