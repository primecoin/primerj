
package net.bither.bitherj.api;

import net.bither.bitherj.api.http.HttpGetResponse;
import net.bither.bitherj.api.http.PrimerUrl;

public class GetFiatSupplyApi extends HttpGetResponse<String> {

    private String mSupply;

    public GetFiatSupplyApi() {
        setUrl(PrimerUrl.PRIMER_FIAT_SUPPLY_URL);
    }

    @Override
    public void setResult(String response) throws Exception {
        this.mSupply = response.replace(" ", "");
    }

    public String getSupply() {
        return mSupply;
    }
}
