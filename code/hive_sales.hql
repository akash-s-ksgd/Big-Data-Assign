CREATE DATABASE IF NOT EXISTS retail_analytics;
USE retail_analytics;

CREATE EXTERNAL TABLE IF NOT EXISTS sales_transactions (
  transaction_ts STRING,
  branch STRING,
  product STRING,
  quantity INT,
  unit_price DECIMAL(10,2)
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION 'file:///data/hive_sales'
TBLPROPERTIES ('skip.header.line.count'='1');

SELECT product, SUM(quantity) AS units,
       CAST(SUM(quantity * unit_price) AS DECIMAL(12,2)) AS revenue
FROM sales_transactions
GROUP BY product
ORDER BY units DESC;

SELECT branch,
       CAST(SUM(quantity * unit_price) AS DECIMAL(12,2)) AS revenue
FROM sales_transactions
GROUP BY branch
ORDER BY revenue DESC;

SELECT HOUR(CAST(transaction_ts AS TIMESTAMP)) AS hour_of_day,
       COUNT(*) AS transactions
FROM sales_transactions
GROUP BY HOUR(CAST(transaction_ts AS TIMESTAMP))
ORDER BY transactions DESC;

SELECT SUBSTR(transaction_ts,1,7) AS month,
       CAST(SUM(quantity * unit_price) AS DECIMAL(12,2)) AS revenue
FROM sales_transactions
GROUP BY SUBSTR(transaction_ts,1,7)
ORDER BY month;
