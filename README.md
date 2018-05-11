# Intuit Assessment
## Introduction
I have not used the provided skeleton project (not sure if I actually had to, it was not specified), since I'm not familiar with the framework that was used.

Exercise Difficulty: Easy
 * How did you feel about the exercise itself?
 9 - It's pretty good. Much better than regular phonescreen with random coding questions. Howerver the exercise is pretty focused on a given thing (a REST API in that case) which I'm okay with.
 * How do you feel about coding an exercise as a step in the interview process?
 Depends on the exercise. I'm not a fan of questions in the style "given a binary search tree, return the subtree where the sum of the nodes is the lowest". Real life scenario are much more interesting but much harder to deal with in a one hour timeline.
 * What would you change in the exercise and/or process?
 Some of the instructions are not really clear. For example that sentence: "There can be more than 1 bid and there can be multiple bids". I'm not sure what the difference is between the 2 parts.

Time spent:
I had the main requirements done in about 1:30 hours. But then I started to get into it and adding more and more stuff (auth, metrics, logs, validators, tests etc...) and in the end I must have spent maybe 8 hours.

Technical choices:
Most of the reasons as to why something is made that way is explained in the code.

JavaDoc:
JavaDoc is mostly missing from the current code.

## Running
### Local
Compile using `mvn install` and run `run.sh`.

The API is exposed on port 11000 by default.

## Authentication
I implemented a basic authentication (as in the HTTP Basic Authentication).
You first need to create a user

`curl -i -X POST http://localhost:11000/v1/users -H 'Content-Type:application/json' -d '{"name":"ben","password":"pwd","confirmationPassword":"pwd","email":"ben@ben.com"}'`

It will return your userId and your name
```
HTTP/1.1 200 OK
Date: Fri, 11 May 2018 06:50:02 GMT
Content-Type: application/json
Content-Length: 58

{"id":"0f802286-4281-4fd5-95be-1b77681c7e0c","name":"ben"}
```

All the other APIs are authenticated so you need to send the correct authentication header.
You can use that website for convenience: https://www.blitter.se/utils/basic-authentication-header-generator/.
Then when using curl, you can add `-H '{WHATEVER_YOU_GOT}'`.

If you don't want to deal with all that stuff, you can just disable auth in the `config/config.yaml` file by putting `authDisabled: true`.

## APIs
I implemented 4 differents APIs.
```
POST    /v1/projects # Create a new project
GET     /v1/projects/{projectId} # Get a project
POST    /v1/projects/{projectId}/autobid # Create an auto-bid for the given project
POST    /v1/projects/{projectId}/bids # Post a bid for the given project
POST    /v1/users # Create a new user
```

### CreateProject
#### Description
Creates a project. All members are required.
#### Request
`curl -i -X POST http://localhost:11000/v1/projects -H 'Authorization: Basic YmVuOnB3ZA==' -H 'Content-Type:application/json' -d '{"name":"test","description":"test","closingBidTime":"2018-05-11T18:30:02Z","budget":1000}'
`
#### Response
```
HTTP/1.1 200 OK
Date: Fri, 11 May 2018 07:01:01 GMT
Content-Type: application/json
Content-Length: 200

{"id":"a26489a2-dad8-4438-8719-c67acd60cb3f","userId":"4dd66e01-cd1b-4f9f-869e-a5cdf993ed19","name":"test","description":"test","budget":1000,"closingBidTime":"2018-05-11T18:30:02Z","minimumBid":1000}
```

### GetProject
#### Description
Gets the project using its id. The id needs to be valid.
#### Request
`curl -i -H 'Authorization: Basic YmVuOnB3ZA==' -X GET http://localhost:11000/v1/projects/a26489a2-dad8-4438-8719-c67acd60cb3f
`
#### Response
```
HTTP/1.1 200 OK
Date: Fri, 11 May 2018 07:03:15 GMT
Content-Type: application/json
Vary: Accept-Encoding
Content-Length: 200

{"id":"a26489a2-dad8-4438-8719-c67acd60cb3f","userId":"4dd66e01-cd1b-4f9f-869e-a5cdf993ed19","name":"test","description":"test","budget":1000,"closingBidTime":"2018-05-11T18:30:02Z","minimumBid":1000}
```

### PostBid
#### Description
Posts a new bid for the supplied project. The bid needs to be valid.
#### Request
`curl -i -H 'Authorization: Basic YmVuOnB3ZA==' -X POST http://localhost:11000/v1/projects/a26489a2-dad8-4438-8719-c67acd60cb3f/bids -H 'Content-Type:application/json' -d '{"value":900}'`
#### Response
```
curl -i -H 'Authorization: Basic YmVuOnB3ZA==' -X POST http://localhost:11000/v1/projects/a26489a2-dad8-4438-8719-c67acd60cb3f/bids -H 'Content-Type:application/json' -d '{"value":900}'
HTTP/1.1 200 OK
Date: Fri, 11 May 2018 07:04:53 GMT
Content-Type: application/json
Content-Length: 105

{"id":"776f54e7-357e-4187-aaa8-ad090b81ceb1","userId":"4dd66e01-cd1b-4f9f-869e-a5cdf993ed19","value":900}
```

### CreateAutoBid
#### Description
Creates an auto-bid with a starting bid and minimum bid. When a bid is posted, all auto-bid for the same project will trigger new bids. The auto-bid step (the amount by which each new bid is calculated compared to the previous bid) is calculated based on the budget of the project (the percentage is configurable in the config.yaml file).
#### Request
`curl -i -H 'Authorization: Basic YmVuOnB3ZA==' -X POST http://localhost:11000/v1/projects/a26489a2-dad8-4438-8719-c67acd60cb3f/autobid -H 'Content-Type:application/json' -d '{"startingBid":800, "minimumBid":600}'`
#### Response
```
HTTP/1.1 200 OK
Date: Fri, 11 May 2018 07:05:58 GMT
Content-Type: application/json
Content-Length: 36

{"startingBid":800,"minimumBid":600}
```

## Metrics
The service emits metrics for each requests and tracks the overall latency of all APIs.
The metrics are exposed accessible through `http://localhost:11001/prometheusMetrics`.
Prometheus has been used for the metrics since it provides labels and provides a convenient Java API.

## Logs
Logs are output to the console. The `config.yaml` file can be changed to configure the logging (rotation/archiving/maxSize/file etc...).

## Tests
The project is tested through unit tests but not fully. I unit-tested most things but not everything because of time.
Current coverage is:
 * Class - 56% - 27/48
 * Method - 43% - 92/211
 * Line - 46% - 246/256

Mockito has been used for mocking.

Integration tests (which I haven't done) should cover all the basic operations of the service.

## Admin
An admin endpoint is available on port 11001.
The healthcheck is accessible from there `http://localhost:11001/healthcheck`