BIG DATA SYSTEMS SELF LEARNING ACTIVITY - LOCAL PROOF OF CONCEPT

Requirements: Python 3.10+ (standard library only) for Kafka/Flink educational simulations; MongoDB server plus mongosh for MongoDB script; Apache Hive and AWS account are optional for executing the HQL/Athena SQL.

From this directory: python code/kafka_traffic_prototype.py; python code/flink_vitals_prototype.py. The scripts read data/ JSONL files. Run MongoDB with mongosh < code/mongodb_catalog.js. Hive SQL expects sales data at /data/sales in the configured warehouse. Athena SQL requires a bucket/table location and an appropriate CSV SerDe; replace YOUR-BUCKET and deploy resources in your account.

All data is synthetic. Kafka and Flink scripts model producer/consumer and stream/window logic locally; they do NOT connect to actual Kafka/Flink clusters. MongoDB and Hive/Athena examples are deployment-ready teaching scripts but were not executed against managed services here. No cloud resource was provisioned and no actual cloud bill was incurred.
