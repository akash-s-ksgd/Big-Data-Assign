"""Deterministic local stream/window simulation; thresholds are educational only."""
import json, pathlib
WINDOW_SECONDS=60
def process(path="data/patient_vitals.jsonl"):
    events=[json.loads(x) for x in pathlib.Path(path).read_text().splitlines()]
    alerts=[]
    for e in events:
        reasons=[]
        if e["heart_rate_bpm"] > 100: reasons.append("heart_rate_demo_high")
        if e["spo2_pct"] < 92: reasons.append("spo2_demo_low")
        if e["temperature_c"] > 38.0: reasons.append("temperature_demo_high")
        if reasons: alerts.append({"patient_id":e["patient_id"],"ts":e["ts"],"reasons":reasons})
    print(json.dumps({"window":"60 seconds","readings":len(events),"abnormal_readings":len(alerts),"alerts":alerts},indent=2))
if __name__ == "__main__": process()
