# Apache Flink

## Scenario problem statement

A hospital monitors patients using connected medical devices that continuously transmit heart rate, oxygen saturation, and body temperature readings. The hospital wants to process readings in real time and flag potentially abnormal values for clinical review.

## Expected outcome

Develop a real-time stream-processing prototype with alert records and summary reports. Use synthetic patient data and educational thresholds; the system must not be treated as a clinical diagnostic tool.

## Files

- `Outcome_Report.docx` — this activity’s problem, implementation, results, analysis, and conclusion.
- `code/` and `data/` — only the code and synthetic input data for this technology.
- `docker-compose.yml` — isolated local service configuration when applicable.

## Demo

Build the Maven job, start the Flink cluster, submit the Java DataStream job, and inspect TaskManager logs for ALERT and SUMMARY records. Synthetic thresholds are educational only.

See `Outcome_Report.docx` for the full activity details and sample results. Inputs are synthetic.
