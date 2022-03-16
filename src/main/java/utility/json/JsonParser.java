package utility.json;

import com.amazonaws.services.mq.model.InternalServerErrorException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

public class JsonParser {


    public BigDecimal[] parseTemps(List<String> responses) throws InternalServerErrorException {

        BigDecimal[] res = new BigDecimal[responses.size()];
        for (int i = 0; i < responses.size(); i++) {
            JSONObject responseBodyJson = new JSONObject(responses.get(i));
            BigDecimal temp = responseBodyJson
                    .getJSONObject("current")
                    .getBigDecimal("temp");
            if(temp == null) throw new InternalServerErrorException("Unable to retrieve temp from openweather API");
            res[i] = temp;
        }
        return res;
    }

    public String parseAwsSecret(String secret) {

        JSONObject json = new JSONObject(secret);
        return json.getString("appid");
    }

    public String parseS3Info(String body) {

        JSONObject responseBodyJson = new JSONObject(body);
        JSONArray resources = responseBodyJson.getJSONArray("resources");
        for (int i = 0; i < resources.length(); i++) {
            JSONObject obj = resources.getJSONObject(i);
            if(obj.get("name").equals("airport-codes_csv")) {
                if(obj.get("dpp:streamedFrom") != null) {
                    String csvUrl = obj.get("dpp:streamedFrom").toString();
                    //eg url: https://s3.amazonaws.com/rawstore.datahub.io/dfadb79d7ba34a49242332f2eaf4f1b0.csv
                    return csvUrl;
                }
            }
        }
        throw new InternalServerErrorException("Unable to retrieve airport coordinates");
    }
}
