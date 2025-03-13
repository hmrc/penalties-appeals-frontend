# Penalties Appeals Frontend

The penalties and appeals service is a frontend microservice only applicable for MTD VAT users.

Users can appeal their late submission penalties and late payment penalties using this service,

Features:
- Supports both JavaScript and non-JavaScript journeys
- Interacts with file upload services to upload supporting documents

## Running the service locally

Pre-requisite: Install Service Manager, MongoDB(4.2) and Node v18.20.5.

 Start all the dependent services excepting the penalties_appeals_frontend in the following two steps 

 `sm2 --start PENALTIES_ALL`

 `sm2 --stop PENALTIES_APPEALS_FRONTEND`

Now to run the penalties_appeals_frontend service 

 `./run.sh`

The service when started listens on port 9181

## Testing

The service can be tested in SBT

  `sbt test it:test` 

## Testing with coverage

To run testing with coverage and scalastyle in SBT

    sbt clean scalastyle coverage test it:test coverageReport

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

