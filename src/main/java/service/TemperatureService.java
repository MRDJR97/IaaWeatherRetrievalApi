package service;

import model.TemperatureFormat;
import org.apache.http.client.utils.URIBuilder;
import utility.creds.AwsSecretManager;
import utility.datetime.DatesUtil;
import utility.http.HttpGetRequest;
import utility.s3.S3QueryTool;
import utility.json.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.*;

public class TemperatureService {

    //Constants - Normally these configurable values would be in config file

    //We can control number of days that this program checks
    private static final int NUM_DAYS = 3;
    //We can change default temp
    private static final String DEFAULT_TEMP_FORMAT = "FAHRENHEIT";
    private static final String AIRPORT_DATA_PACKAGE_URL = "https://datahub.io/core/airport-codes/datapackage.json";

    private String airportCode;
    private String latitude;
    private String longitude;
    private String s3Bucket;
    private String s3ObjKey;
    private TemperatureFormat tempFormat;

    public TemperatureService(String[] args) {

        int len = args.length;
        if(len == 0  || len > 2) throw new IllegalArgumentException("Error: Incorrect number of arguments were provided");
        System.out.println("Arguments: " + Arrays.toString(args));
        this.airportCode = args[0];
        //Fahrenheit should be used by default if format not specified
        String inputtedTempFormat = (len == 2) ? args[1] : DEFAULT_TEMP_FORMAT;
        try {
            //param: = standard, metric and imperial -> default: kelvin, metric: Celsius, imperial: Fahrenheit
            this.tempFormat = TemperatureFormat.valueOf(inputtedTempFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            //Invalid arg passed in for temperature - we will use the default (fahrenheit) instead
            this.tempFormat = TemperatureFormat.valueOf(DEFAULT_TEMP_FORMAT.toUpperCase());
        }
    }

    public BigDecimal getHighestTemp() throws Exception {

        //send req to https://datahub.io/core/airport-codes/datapackage.json to find s3 bucket and most recent object key
        getLatestAirportDataSource();
        //use s3 select query language to find coords
        S3QueryTool s3 = new S3QueryTool();
        String coords = s3.getCoordsFromCsv(this.s3ObjKey, this.s3Bucket, this.airportCode);

        String[] coordsArr = coords.split(",");
        if(coordsArr.length != 2) throw new IOException("Coordinates not retrieved correctly");
        this.longitude = coordsArr[0];
        this.latitude = coordsArr[1];

        Map<String, String> parameters = new HashMap<>();
        parameters.put("units", tempFormat.getOutput());
        parameters.put("lat", this.latitude);
        parameters.put("lon", this.longitude);

        DatesUtil datesUnix = new DatesUtil();
        List<String> dates = datesUnix.retrievePastNDates(NUM_DAYS);

        //Add api key as parameter also - retrieve using AwsSecretManager
        AwsSecretManager mgr = new AwsSecretManager();
        parameters.put("appid", mgr.getSecret());
        //Insert params into URLs
        List <URI> uris = formatUrls(dates, parameters);
        //Max temp from last n days
        BigDecimal maxTemp = retrieveMaxTemp(uris);
        System.out.println("MAX TEMP: " + maxTemp + " DEGREES " + this.tempFormat+"\n");
        return maxTemp;
    }

    private List<URI> formatUrls(List<String> dates, Map<String, String> params) throws URISyntaxException, MalformedURLException {

        URIBuilder builder = new URIBuilder();
        List<URI> uris = new ArrayList<>();
        //these should probably be constants or in config file
        builder.setScheme("https");
        builder.setHost("api.openweathermap.org");
        builder.setPath("/data/2.5/onecall/timemachine");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addParameter(entry.getKey(), entry.getValue());
        }
        for(String date : dates) {
            URI uri = builder.setParameter("dt", date).build().toURL().toURI();
            uris.add(uri);
        }
        return uris;
    }

    //BigDecimal obj because that's what's returned by JsonParser for decimals
    private BigDecimal retrieveMaxTemp(List<URI> uris) throws Exception {

        BigDecimal maxVal = BigDecimal.valueOf(-1);
        //Send NUM_DAYS requests concurrently
        List<String> weatherDataResponses = new HttpGetRequest().getAsync(uris);

        JsonParser parser = new JsonParser();
        BigDecimal[] historicalMaxTemps = parser.parseTemps(weatherDataResponses);
        for(int i = 0; i < historicalMaxTemps.length; i++) {
            //calculate running max value
            maxVal = maxVal.max(historicalMaxTemps[i]);
        }
        return maxVal;
    }

    private void getLatestAirportDataSource() throws Exception {

        HttpResponse<String> response = new HttpGetRequest().getSync(AIRPORT_DATA_PACKAGE_URL, new HashMap<>());
        //Parse latest S3 bucket location
        JsonParser parser = new JsonParser();
        String csvUrl = parser.parseS3Info(response.body());
        String[] urlParts = csvUrl.split("/");
        this.s3ObjKey = urlParts[urlParts.length - 1];
        this.s3Bucket = urlParts[urlParts.length - 2];
        //example url: https://s3.amazonaws.com[host]/rawstore.datahub.io[bucket]/dfadb79d7ba34a49242332f2eaf4f1b0.csv[S3 Obj Key]
        return;
    }
}
