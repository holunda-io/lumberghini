# lumberghini

A camunda perpetuum mobile based on the movie "office space".

[![Slideshare](./talk.png)](https://www.slideshare.net/jangalinski/camunda-summit2021-the-great-lumberghini)

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

Configuration: <https://portal.azure.com>



https://user-images.githubusercontent.com/814032/116476200-2deeca80-a87b-11eb-9447-5248b07f7c4a.mp4

