# Publisher

## Modules
- api - exports the publisher interface for clients and providers to import
- features - used for managing the feature repository for publisher
- karaf.extension - used for creating an easily installable zip 
- provider - provides an implementation of the publisher api, this implementation assumes the controller has a single identity for publishing to DMAAP message router
- sample.client - a dummy client that posts a simple message to a configured topic during its initialization
