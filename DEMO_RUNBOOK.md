# Demo runbook

Run from the project root in PowerShell. Allow the initial container pulls and Maven dependency download to finish before presenting. Start Docker Desktop first. Use the synthetic data only; do not describe the educational patient thresholds as medical advice.

## 0. Prepare

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
docker compose version
```

Keep only the activity being presented running to save memory. `docker compose --profile <name> down` stops that activity when finished.

## 1. Hive sales warehouse

```powershell
docker compose --profile hive up -d hive
docker compose exec -T hive beeline -u 'jdbc:hive2://localhost:10000/default' -f /project/code/hive_sales.hql
```

Show product units/revenue, branch totals, transaction hours, and the monthly query. For this 12-row sample, Bread leads units (15), Coffee leads revenue ($72.00), and South leads branch revenue ($77.50). One partial month is not enough to infer a trend.

## 2. Kafka traffic producer and consumer

```powershell
docker compose --profile kafka up -d kafka
docker compose exec -T kafka /opt/kafka/bin/kafka-topics.sh --create --topic traffic.sensors --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

In terminal A:

```powershell
python code/kafka_consumer.py --group demo-1 --max-messages 12
```

In terminal B:

```powershell
python code/kafka_producer.py --count 12 --interval 0.5
```

The consumer prints JSON with `congestion_alert`; the demo threshold is speed under 25 km/h OR vehicle count at least 80. A new group name replays the topic from its beginning. Stop the broker after the demo:

```powershell
docker compose --profile kafka down
```

## 3. Flink real-time stream and event-time window

```powershell
docker compose --profile flink run --rm flink-builder
docker compose --profile flink up -d flink-jobmanager flink-taskmanager
docker compose exec -T flink-jobmanager flink run /opt/flink/usrlib/vitals-stream-demo-1.0.jar
docker compose logs --tail 80 flink-taskmanager
```

Open `http://localhost:8081` to show the Flink job. TaskManager logs contain `ALERT` records and `SUMMARY` rows from a 10-second tumbling event-time window. The job emits a finite 10-reading synthetic stream over about five seconds so the demonstration finishes predictably. Thresholds are educational only.

## 4. MongoDB product catalog

```powershell
docker compose --profile mongo up -d mongo
mongosh 'mongodb://localhost:27017/ecommerce_demo' --file code/mongodb_catalog.js
```

Show the category/price query and its `executionStats`, stock increment (12 to 17), and discontinued-product deletion. The script resets the `products` collection in the dedicated demo database each run. Stop the container with `docker compose --profile mongo down`.

## 5. Cloud log analytics proof of concept

```powershell
python code/cloud_log_analysis.py
java code/CloudLogChart.java data/server_logs.csv outputs/cloud-log-chart.png
Get-Content outputs/cloud_log_summary.json
```

This is a local simulation for a no-credentials demo. Present the S3 + Glue Data Catalog + Athena architecture and the Athena SQL in `code/athena_logs.sql`. The sample shows 14 requests and five server errors; the sample is too small to estimate a true seasonal baseline. No AWS account or cloud bill is required.

## If a live service is unavailable

Use the precomputed examples in `Consolidated_Report.docx` and the static input datasets in `data/`. State clearly which service did not start. For Kafka/Flink, present the recorded dataset and explain the producer-consumer and event-time processing flow; do not claim the cluster ran if it did not.
