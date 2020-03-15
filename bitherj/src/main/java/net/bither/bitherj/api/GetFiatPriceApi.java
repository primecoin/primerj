
package net.bither.bitherj.api;

import net.bither.bitherj.api.http.HttpGetResponse;
import net.bither.bitherj.api.http.PrimerUrl;

import org.json.JSONObject;

public class GetFiatPriceApi extends HttpGetResponse<String> {

    private static final String CNYPRICE = "cny";
    private static final String USDPRICE = "usd";
    private static final String COINID = "primecoin";

    private float mCny;
    private float mUsd;

    public GetFiatPriceApi() {
        setUrl(PrimerUrl.PRIMER_FIAT_PRICE_URL);
    }

    @Override
    public void setResult(String response) throws Exception {
        JSONObject json = new JSONObject(response);
        this.mCny = (float)json.getJSONObject(COINID).getDouble(CNYPRICE);
        this.mUsd = (float)json.getJSONObject(COINID).getDouble(USDPRICE);
        this.result = response;
    }

    public float getCurrencyCny() {
        return mCny;
    }

    public float getCurrencyUsd() {
        return mUsd;
    }
}
