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

package com.intellij.idea.plugin.hybris.tools.remote.http;

import com.google.gson.Gson;
import com.intellij.idea.plugin.hybris.tools.logging.LogLevel;
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType;
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil;
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrQueryExecutionContext;
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.impl.SolrHttpClient;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHttpResult.HybrisHttpResultBuilder.createResult;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.jsoup.Jsoup.parse;

@Service(Service.Level.PROJECT)
public final class HybrisHacHttpClient extends AbstractHybrisHacHttpClient {

    private static final Logger LOG = Logger.getInstance(HybrisHacHttpClient.class);
    @Serial
    private static final long serialVersionUID = -6570347636518523678L;

    public static HybrisHacHttpClient getInstance(@NotNull final Project project) {
        return project.getService(HybrisHacHttpClient.class);
    }

    @NotNull
    public HybrisHttpResult executeSolrSearch(final Project project, @Nullable final SolrQueryExecutionContext queryObject) {
        if (queryObject != null) {
            return SolrHttpClient.getInstance(project).executeSolrQuery(queryObject);
        }

        return HybrisHttpResult.HybrisHttpResultBuilder
            .createResult()
            .httpCode(HttpStatus.SC_BAD_GATEWAY)
            .errorMessage("Unable to connect to Solr server. Please, check connection configuration")
            .build();
    }

    @Nullable
    public Map<?, ?> parseResponse(final Elements fsResultStatus) {
        try {
            return new Gson().fromJson(fsResultStatus.text(), HashMap.class);
        } catch (final Exception e) {
            LOG.error("Cannot parse response", e);
            return null;
        }
    }

    @NotNull
    public HybrisHttpResult executeLogUpdate(
        final Project project,
        final String loggerName,
        final LogLevel logLevel,
        final int timeout
    ) {
        final var settings = RemoteConnectionUtil.INSTANCE.getActiveRemoteConnectionSettings(project, RemoteConnectionType.Hybris);
        final var params = Arrays.asList(
            new BasicNameValuePair("loggerName", loggerName),
            new BasicNameValuePair("levelName", logLevel.name())
        );
        HybrisHttpResult.HybrisHttpResultBuilder resultBuilder = createResult();
        final String actionUrl = settings.getGeneratedURL() + "/platform/log4j/changeLevel/";

        final HttpResponse response = post(actionUrl, params, true, timeout, settings, null);
        final StatusLine statusLine = response.getStatusLine();
        resultBuilder = resultBuilder.httpCode(statusLine.getStatusCode());
        if (statusLine.getStatusCode() != SC_OK || response.getEntity() == null) {
            return resultBuilder.errorMessage("[" + statusLine.getStatusCode() + "] " +
                statusLine.getReasonPhrase()).build();
        }
        final Document document;
        try {
            document = parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "");
        } catch (final IOException e) {
            return resultBuilder.errorMessage(e.getMessage() + ' ' + actionUrl).httpCode(SC_BAD_REQUEST).build();
        }
        final Elements fsResultStatus = document.getElementsByTag("body");
        if (fsResultStatus == null) {
            return resultBuilder.errorMessage("No data in response").build();
        }
        final Map json = parseResponse(fsResultStatus);

        if (json == null) {
            return createResult()
                .errorMessage("Cannot parse response from the server...")
                .build();
        }

        final var stacktraceText = json.get("stacktraceText");
        if (stacktraceText != null && isNotEmpty(stacktraceText.toString())) {
            return createResult()
                .errorMessage(stacktraceText.toString())
                .build();
        }

        if (json.get("outputText") != null) {
            resultBuilder.output(json.get("outputText").toString());
        }
        if (json.get("executionResult") != null) {
            resultBuilder.result(json.get("executionResult").toString());
        }
        return resultBuilder.build();
    }
}
