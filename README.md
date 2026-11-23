# Big Data Systems Self Learning Activities

Five demos for Course 24CI51: Apache Hive sales warehousing, Apache Kafka traffic alerts, Apache Flink patient-vitals windows, MongoDB catalog CRUD, and cloud server-log analytics. Synthetic sample data is included. No real patient, traffic, customer, or server data is used.

## Quick start

Prerequisites for the service demos: Docker Desktop with Linux containers, Python 3.10+, Java 17 (inside Docker builder), and internet access for the initial image/dependency downloads. Start Docker Desktop before running commands. Run commands from this project directory.

```powershell
python -m pip install -r requirements.txt
```

Each service runs separately to reduce laptop memory use. Follow [DEMO_RUNBOOK.md](DEMO_RUNBOOK.md) for the presentation order and exact commands. Hive, Kafka, Flink, and MongoDB examples use their named services. The cloud activity is a local log-analysis simulation plus optional Athena SQL; no AWS resources are created.

## Project files

- `Consolidated_Report.docx` — assignment report with technology study, implementation, sample results, interpretation, conclusions, references, and limits.
- `docker-compose.yml` — isolated Hive, Kafka, Flink, and MongoDB demo services.
- `code/` — HiveQL, Kafka producer/consumer, Flink Java job source and Maven build, mongosh catalog script, and cloud-log analysis/chart.
- `data/` — deterministic synthetic CSV and JSONL sample data.

## Important limits

Kafka producer and consumer use a real single-node Kafka broker when run per the guide. Flink uses a real local Flink cluster and Java DataStream job. Hive and MongoDB use local containers. Cloud analytics is a local proof of concept unless the optional Athena queries are run in an AWS account. Vitals thresholds are educational examples only, not clinical guidance. Container data is local and intended for the demonstration.
