/* * Copyright 2014 http://Bither.net * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * *    http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package net.bither.bitherj.api.http;import org.apache.http.HttpResponse;import org.apache.http.client.methods.HttpGet;import org.slf4j.Logger;import org.slf4j.LoggerFactory;public abstract class HttpGetResponse<T> extends BaseHttpResponse<T> {    public void handleHttpGet() throws Exception {        final Logger log = LoggerFactory                .getLogger(BaseHttpResponse.class);        setHttpClient();        try {            HttpGet httpGet = new HttpGet(getUrl());            httpGet.setHeader("Accept", "application/json");            HttpResponse httpResponse = getHttpClient().execute(httpGet);            String response = getReponse(httpResponse);            setResult(response);            log.info("Received http output: " + response);        } catch (Exception e) {            throw e;        } finally {            getHttpClient().getConnectionManager().shutdown();        }    }}