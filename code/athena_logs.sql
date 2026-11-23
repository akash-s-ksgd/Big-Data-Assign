-- Athena external table over partitioned S3 log data; align columns with CSV/JSON SerDe used.
CREATE EXTERNAL TABLE IF NOT EXISTS ops_logs (timestamp string, status int, path string, method string, latency_ms int, bytes int)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES ('separatorChar'=',','quoteChar'='"','escapeChar'='\\')
LOCATION 's3://YOUR-BUCKET/logs/'
TBLPROPERTIES ('skip.header.line.count'='1');
-- Error frequency
SELECT status, count(*) AS requests FROM ops_logs WHERE status >= 400 GROUP BY status ORDER BY requests DESC;
-- Failed-request spikes by hour
SELECT substr(timestamp,1,13) AS hour_utc, count(*) AS failures FROM ops_logs WHERE status >= 500 GROUP BY substr(timestamp,1,13) ORDER BY hour_utc;
-- Frequent server errors
SELECT path, count(*) AS errors FROM ops_logs WHERE status >= 500 GROUP BY path ORDER BY errors DESC LIMIT 10;
-- Peak volume and latency
SELECT substr(timestamp,1,13) AS hour_utc, count(*) AS requests, approx_percentile(latency_ms,0.95) AS p95_latency_ms FROM ops_logs GROUP BY substr(timestamp,1,13) ORDER BY requests DESC;
