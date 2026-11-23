"""Local Kafka-style producer/consumer simulation; no broker is required."""
import json, pathlib
TOPIC = "traffic.sensors"
SPEED_THRESHOLD_KMH = 25
VEHICLE_THRESHOLD = 80
def run(path="data/traffic_events.jsonl"):
    events=[json.loads(x) for x in pathlib.Path(path).read_text().splitlines()]
    print(f"topic={TOPIC} produced={len(events)}")
    for e in events:  # consumer receives records in file order
        if e["speed_kmh"] < SPEED_THRESHOLD_KMH or e["vehicle_count"] >= VEHICLE_THRESHOLD:
            print(json.dumps({"type":"CONGESTION_ALERT","event_id":e["event_id"],"road":e["road"],"speed_kmh":e["speed_kmh"],"vehicle_count":e["vehicle_count"],"ts":e["ts"]}))
if __name__ == "__main__": run()
