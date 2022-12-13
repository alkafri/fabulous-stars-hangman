# Hangman backend

Uses Google Cloud Datastore and App Engine Standard Java17 Bundled Services

* [java-datastore](https://cloud.google.com/appengine/docs/java/datastore/)
* [ae-docs](https://cloud.google.com/appengine/docs/standard/java-gen2/services/access)

## Running locally

This example uses the
[Cloud SDK Maven plugin](https://cloud.google.com/appengine/docs/java/tools/using-maven).
To run this sample locally:

```
mvn package appengine:run
```
To see the results of the sample application, open
[localhost:8080](http://localhost:8080) in a web browser.


## Deploying

```
mvn clean package appengine:deploy -Dapp.deploy.gcloudMode=beta
mvn appengine:deployIndex

```

