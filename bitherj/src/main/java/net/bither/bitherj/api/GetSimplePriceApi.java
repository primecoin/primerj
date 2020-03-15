/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.bitherj.api;

import net.bither.bitherj.api.http.HttpGetResponse;
import net.bither.bitherj.api.http.PrimerUrl;
import net.bither.bitherj.core.Peer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSimplePriceApi extends HttpGetResponse<String> {

    private static final Logger log = LoggerFactory.getLogger(GetSimplePriceApi.class);
    private static final String CNYPRICE = "cny";
    private static final String USDPRICE = "usd";
    private static final String COINID = "primecoin";

    private float mCny;
    private float mUsd;

    public GetSimplePriceApi() {
        setUrl(PrimerUrl.BITHER_SIMPLE_PRICE);
    }

    @Override
    public void setResult(String response) throws Exception {
        JSONObject json = new JSONObject(response);
        this.mCny = (float)json.getJSONObject(COINID).getDouble(CNYPRICE);
        this.mUsd = (float)json.getJSONObject(COINID).getDouble(USDPRICE);
        log.info("curry " + mCny + "; " + mUsd);
        this.result = response;
    }

    public float getCurrencyCny() {
        return mCny;
    }

    public float getCurrencyUsd() {
        return mUsd;
    }
}
