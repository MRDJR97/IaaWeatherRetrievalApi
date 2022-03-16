package utility.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.amazonaws.util.IOUtils.copy;


public class S3QueryTool {

    //should use names instead of 's._12' in case order of csv headers/values changes
    private static String query = "SELECT s._12 FROM s3object s WHERE s._1 = '%s'";

    public S3QueryTool () {
    }

    //todo: pull out POST into class of its own
    public static String getCoordsFromCsv(String objKey, String bucket, String airportCode) throws Exception {

        final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        String coords;
        query = String.format(query, airportCode);

        SelectObjectContentRequest request = generateBaseCSVRequest(bucket, objKey, query);
        final AtomicBoolean isResultComplete = new AtomicBoolean(false);

        try (OutputStream baos = new ByteArrayOutputStream();
             SelectObjectContentResult result = s3Client.selectObjectContent(request)) {
            InputStream resultInputStream = result.getPayload().getRecordsInputStream(
                    new SelectObjectContentEventVisitor() {
                        @Override
                        public void visit(SelectObjectContentEvent.StatsEvent event)
                        {
                            System.out.println(
                                    "Received Stats, Bytes Scanned: " + event.getDetails().getBytesScanned()
                                            +  " Bytes Processed: " + event.getDetails().getBytesProcessed());
                        }

                        //An End Event informs that the request has finished successfully.
                        @Override
                        public void visit(SelectObjectContentEvent.EndEvent event)
                        {
                            isResultComplete.set(true);
                            System.out.println("Received End Event. Result is complete.");
                        }
                    }
            );
            copy(resultInputStream, baos);
            coords = baos.toString();
        }

        /* The End Event indicates all matching records have been transmitted.
         * If the End Event is not received, the results may be incomplete.*/
        if (!isResultComplete.get()) {
            throw new Exception("S3 Select request was incomplete");
        }
        //Invalid IATA code provided
        if(coords == null || coords.isEmpty()) {
            throw new IllegalArgumentException("Unable to find coordinates of IATA code provided");
        }
        return trimCsv(coords);
    }

    private static SelectObjectContentRequest generateBaseCSVRequest(String bucket, String key, String query) {
        SelectObjectContentRequest request = new SelectObjectContentRequest();
        request.setBucketName(bucket);
        request.setKey(key);
        request.setExpression(query);
        request.setExpressionType(ExpressionType.SQL);

        InputSerialization inputSerialization = new InputSerialization();
        inputSerialization.setCsv(new CSVInput());
        inputSerialization.setCompressionType(CompressionType.NONE);
        request.setInputSerialization(inputSerialization);

        OutputSerialization outputSerialization = new OutputSerialization();
        outputSerialization.setCsv(new CSVOutput());
        request.setOutputSerialization(outputSerialization);

        return request;
    }

    private static String trimCsv(String s) {
        //These regexes should be combined
        s = s.replaceAll("\"", "");
        s = s.replaceAll("\n", "");
        s = s.replaceAll("\\s+","");
        return s;
    }
}
