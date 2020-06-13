
package net.bither.bitherj.api;

import net.bither.bitherj.api.http.HttpGetResponse;
import net.bither.bitherj.api.http.PrimerUrl;

public class GetPrimecoinSupplyApi extends HttpGetResponse<String> {

    private String mSupply;

    public GetPrimecoinSupplyApi() {
        setUrl(PrimerUrl.PRIMECOIN_SUPPLY_URL);
    }

    @Override
    public void setResult(String response) throws Exception {
        String str = response.replace(" ", "");
        int dot = str.indexOf('.');
        if( dot == -1) {
            dot = str.length();
        }
        if(dot == 0) {
            this.mSupply = "0";
        } else {
            this.mSupply = str.substring(0, dot);
        }
    }

    public String getSupply() {
        return mSupply;
    }
}
