<div id="top"><h1 align="center">VmWare Assignment Daniel Regan</h1></div>

## How to Run
<h3>Install</h3>
* [Java 11](https://www.oracle.com/java/technologies/downloads/) so you will need JDK/JRE 11 installed and configured correctly.
* [Maven](https://maven.apache.org/download.cgi) >3.8.0 is necessary also.
* [AWS SDK](https://aws.amazon.com/sdk-for-java/) which is configured with a programmatic access user with S3 & AWS Secrets Manager access is necessary also.
1. Run `mvn clean install` - this will create a runnable jar file in the target folder
2. Run the jar using command line or IDE and pass 1/2 args to it

<h3>Command Line Arguments</h3>
1. The program is designed so that the first argument passed is always interpreted as the airport code.
2. A second optional argument is interpreted as the temperature format. This can be Kelvin, Celsius or Fahrenheit. If this argument is blank or not one of the accepted values, Fahrenheit is used as the default.
<p align="right">(<a href="#top">back to top</a>)</p>


## Approach Taken
1. The program makes a GET request to https://datahub.io/core/airport-codes/datapackage.json to retrieve the URL of the latest CSV file stored in AWS S3. This is because it is updated daily according to the documentation.
2. Once the URL of the CSV file has been retrieved, it uses [S3 Select](https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-glacier-select-sql-reference-select.html) to find the coordinates of the provided Airport code. This was the most efficient way I could think of retrieving the coordinates, instead of requesting large amounts of data over HTTPS.
3. Now, in order to get the secure API key for calling the Open Weather API, a request is sent to AWS Secrets Manager, where I have stored the key.
4. A number of concurrent asynchronous GET requests are sent to the Open Weather API to save time. Each request is populated using the `latitude`, `longitude`, `unit of temperature` & `API Key` parameters. One request is necessary for each day of historical
data as per the OW documentation. Instead of hardcoding 3 requests, the number of days is configurable.
5. The results from these requests are compared to find the max temp which is then printed out.
<p align="right">(<a href="#top">back to top</a>)</p>

## Bugs
* My positive Junit tests aren't working correctly, but I have spent quite a bit of time on this so I just left them in to show my approach.

<p align="right">(<a href="#top">back to top</a>)</p>