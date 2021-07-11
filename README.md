# N26 Coding Challenge

## Requirements
These are the additional requirements for the solution:
- You are free to choose any JVM language to complete the challenge in, but
your application has to run in Maven.
- The API has to be thread-safe with concurrent requests.
- The POST /transactions and GET /statistics endpoints MUST execute in
constant time and memory ie O(1). Scheduled cleanup is not sufficient
- The solution has to work without a database (this also applies to in-memory
databases).
- Unit tests are mandatory.
- mvn clean install and mvn clean integration-test must complete successfully.
- Please ensure that no changes are made to the src/it folder.
- In addition to passing the tests, the solution must be at a quality level that you
would be comfortable enough to put in production.

## Problem challenge
We would like to have a REST API for our statistics. The main use case for the
API is to calculate realtime statistics for the last 60 seconds of transactions.
The API needs the following endpoints:
- `POST /transactions` – called every time a transaction is made.
- `GET /statistics` – returns the statistic based of the transactions of the last 60
seconds.
- `DELETE /transactions` – deletes all transactions.

You can complete the challenge offline using an IDE of your choice. To download
the application skeleton, please enable Use Git in the editor and follow the
instructions on screen. Please make sure you test your solution where possible
before submitting.

## Specs

1. `POST /transactions`

This endpoint is called to create a new transaction. It MUST execute in constant time
and memory (O(1)).

Body:
```
{
"amount": "12.3343",
"timestamp": "2018-07-17T09:59:51.312Z"
}
```

Where:
- amount – transaction amount; a string of arbitrary length that is parsable as a
BigDecimal
- timestamp – transaction time in the ISO 8601 format
YYYY-MM-DDThh:mm:ss.sssZ in the UTC timezone (this is not the current
timestamp)

Returns: Empty body with one of the following:
- 201 – in case of success
- 204 – if the transaction is older than 60 seconds
- 400 – if the JSON is invalid
- 422 – if any of the fields are not parsable or the transaction date is in the
future

2. `GET /statistics`

This endpoint returns the statistics based on the transactions that happened in the
last 60 seconds. It MUST execute in constant time and memory (O(1)).

Returns:
```
{
"sum": "1000.00",
"avg": "100.53",
"max": "200000.49",
"min": "50.23",
"count": 10
}
```

Where:
- sum – a BigDecimal specifying the total sum of transaction value in the last 60
seconds
- avg – a BigDecimal specifying the average amount of transaction value in the
last 60 seconds
- max – a BigDecimal specifying single highest transaction value in the last 60
seconds
- min – a BigDecimal specifying single lowest transaction value in the last 60
seconds
- count – a long specifying the total number of transactions that happened in
the last 60 seconds

All BigDecimal values always contain exactly two decimal places and use
`HALF_ROUND_UP` rounding. eg: 10.345 is returned as 10.35, 10.8 is returned as
10.80

3. `DELETE /transactions`

This endpoint causes all existing transactions to be deleted
The endpoint should accept an empty request body and return a `204` status code.

## Design Explained

### Overall
#### Entering Valid Transaction
The application initiates final statistics store array to contain all transactions statistics. 
Each index in the array represents an individual interval (default to 1000ms = 1 sec. )

Once an incoming transaction passes validation it follows the below scenario:
1. A proper store index is calculated
2. According to the above index, StatisticsStore is fetched from the store array and there are 3 options:

(a) StatisticsStore is empty ?  - Aggregating the current transaction details.

(b) StatisticsStore is out of date ?  Resetting aggregator and aggregating the
current transaction details.

(c) StatisticsStore is valid ? Adding and aggregating the current transaction details

#### Getting Statistics
According to the current time, the manager provides all valid StatisticsStore(s)
Statistics are calculated by iterating on all valid aggregators.

### Concurrency Handling Explained
Each StatisticsStore has its own ReadWriteLock (https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReadWriteLock.html)
When a StatisticsStore is taken for an update, write lock is granted. When StatisticsStore is taking for observing and statistics a Read lock is requested.
ReadWriteLock technique was chosen due to its mechanism to allow non-blocking multiple reads threads. The only time a block will occur is
when write lock is granted.

## Time Complexity

### Inserting (POST /transactions)
Will take O(1) as it only put the valid transaction in to the store array (number of operations is constant)

### Producing Statistics (GET /statistics)
Will take O(1):

a. Fetching valid StatisticsStore(s) - O(1) by default will iterate on 60 indices

b. Calculate statistics from valid store(s) - O(1) by default - maximum 60 aggregator(s)

## Space Complexity
Requires O(1) . Container array's size is constant and pre-defined and not depended on number of incoming transactions.