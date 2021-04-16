# lumberghini

A camunda perpetuum mobile based on the movie "office space".

## Information

* [Issues](https://github.com/jangalinski/lumberghini/issues)

## Build

### Deploy on azure

To avoid having azure details in the public pom, I used [property-injection](https://maven.apache.org/examples/injecting-properties-via-settings.html)
from my local `.m2/settings.xml`. So the call of the azure maven plugin has to include profiles.

* `az login`
* `mvn -Plumberghini-azure -Plumberghini-azure-properties  azure-webapp:deploy`
* wait
* check <https://lumberghini-app.azurewebsites.net>



