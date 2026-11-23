"""Publish synthetic junction events to a real Kafka topic."""
import argparse
import json
import random
import time
from datetime import datetime, timezone

from confluent_kafka import Producer


SENSORS = [
    ("J-A", "Central Ave"),
    ("J-B", "River Rd"),
    ("J-C", "Market St"),
]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--bootstrap", default="localhost:9092")
    parser.add_argument("--topic", default="traffic.sensors")
    parser.add_argument("--count", type=int, default=12)
    parser.add_argument("--interval", type=float, default=0.5)
    parser.add_argument("--seed", type=int, default=24)
    args = parser.parse_args()
    producer = Producer({"bootstrap.servers": args.bootstrap})
    rng = random.Random(args.seed)
    for i in range(1, args.count + 1):
        junction, road = SENSORS[(i - 1) % len(SENSORS)]
        # Repeated congestion cases make the live demo predictable.
        if i % 3 == 1:
            speed, vehicles = rng.randint(12, 23), rng.randint(82, 110)
        else:
            speed, vehicles = rng.randint(32, 52), rng.randint(18, 64)
        event = {
            "event_id": f"TRAFFIC-{i:03d}",
            "junction": junction,
            "road": road,
            "speed_kmh": speed,
            "vehicle_count": vehicles,
            "ts": datetime.now(timezone.utc).isoformat(),
        }
        producer.produce(args.topic, key=junction, value=json.dumps(event))
        producer.poll(0)
        print(f"produced {event['event_id']} {road} {speed} km/h {vehicles} vehicles", flush=True)
        time.sleep(args.interval)
    pending = producer.flush(10)
    if pending:
        raise SystemExit(f"Kafka did not deliver {pending} message(s)")


if __name__ == "__main__":
    main()
