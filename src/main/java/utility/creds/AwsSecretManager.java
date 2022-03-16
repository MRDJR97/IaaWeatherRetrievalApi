package utility.creds;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import utility.json.JsonParser;

import java.util.Base64;

public class AwsSecretManager {

    public static String getSecret() {

        //These should be in config file
        String secretName = "api.openweathermap.key";
        String region = "eu-west-2";

        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        String secret = null, decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        }
        //In the interest of time I didn't do proper exception handling for this key retrieval
        catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            throw e;
        }

        // Decrypts secret using the associated KMS key.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        }
        else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }

        //Parse ASM json response
        JsonParser p = new JsonParser();
        secret = p.parseAwsSecret(secret);
        return secret;
    }
}
