# Cypher Fraud - Fraudulent Transaction Detector

A rule-based fraud detection system built in Java with a JavaFX interface. It reads transactions from a CSV file, runs each one through a set of independent fraud rules, and produces a risk score, a risk level (LOW / MEDIUM / HIGH), and a plain-English list of reasons for every transaction.

Built in 24 hours at **RowdyHack XII** by a team of three University of Texas at San Antonio (UTSA) students

DevPost Submission: https://devpost.com/software/fraud-transaction-detection


## Features

- **Explainable detection:** every flagged transaction lists exactly which rules triggered and why
- **Pluggable rules:** add a new rule by writing one class and one line of registration code
- **Risk scoring:** weak signals stay LOW, but several signals together stack up to HIGH
- **CSV input:** load your own transaction files or use the included sample data
- **Summary report:** totals per risk level and number of flagged transactions


## Fraud rules

| Rule                 | What it detects                                                 |
|----------------------|-----------------------------------------------------------------|
| High Amount          | Amount far above the user's average spend                       |
| Velocity             | Too many transactions in a short time window                    |
| Location Change      | Purchases in different countries too close together in time     |
| Odd Hour             | Activity at unusual hours (e.g. 12 a.m. - 5 a.m.) for that user |


**Risk levels:** 0-20 = LOW, 40 = MEDIUM, 60+ = HIGH


## Getting started

### Requirements

- Java 17 or newer
- JavaFX 26.0.2 (if not bundled with your JDK)
- Maven

### Run from source

```bash
git clone https://github.com/ThanhVinh2869/rowdyHacks
cd .\rowdyHacks\
mvn javafx:run
```

## CSV format

```csv
transactionId,userId,amount,merchant,country,date
1,u1,18.50,starbucks,US,2026-10-03T08:15:00Z
5,u1,650.00,electronics-hub,US,2026-10-04T13:00:00Z
10,u2,45.00,starbucks,BR,2026-10-04T09:00:00Z
```

## Built with

- Java 25.0.4.1
- JavaFX 26.0.2

## Limitations and future work

- Rules use fixed thresholds; they could be loaded from a config file
- The CSV is processed as a batch, but the design supports live transaction streams
- Possible new rules: card testing, duplicate transactions, statistical outliers, new-country detection
- Possible storage upgrade: a database instead of CSV files


## Team

| Name        | Role                                                                |
|-------------|---------------------------------------------------------------------|
| Vinh Nguyen | Backend: data and detection engine                                  |
| Zaid Haq    | Frontend: JavaFX dashboard and user interface                       |
| Carl Escala | Integration: connecting backend and frontend, testing data pipeline |