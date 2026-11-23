"""Consume traffic.sensors events and emit threshold-based alerts."""
import argparse
import json
import time

from confluent_kafka import Consumer, KafkaError


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--bootstrap", default="localhost:9092")
    parser.add_argument("--topic", default="traffic.sensors")
    parser.add_argument("--group", default="traffic-demo")
    parser.add_argument("--max-messages", type=int, default=12)
    parser.add_argument("--idle-seconds", type=float, default=30)
    args = parser.parse_args()
    consumer = Consumer({
        "bootstrap.servers": args.bootstrap,
        "group.id": args.group,
        "auto.offset.reset": "earliest",
        "enable.auto.commit": True,
    })
    consumer.subscribe([args.topic])
    received = 0
    last_message = time.monotonic()
    print(f"listening topic={args.topic} group={args.group}", flush=True)
    try:
        while received < args.max_messages and time.monotonic() - last_message < args.idle_seconds:
            message = consumer.poll(1.0)
            if message is None:
                continue
            if message.error():
                if message.error().code() == KafkaError._PARTITION_EOF:
                    continue
                raise RuntimeError(message.error())
            event = json.loads(message.value())
            congested = event["speed_kmh"] < 25 or event["vehicle_count"] >= 80
            result = {"event": event, "congestion_alert": congested,
                      "reason": "speed_below_25_or_count_at_least_80" if congested else None}
            print(json.dumps(result, separators=(",", ":")), flush=True)
            received += 1
            last_message = time.monotonic()
    finally:
        consumer.close()
    print(f"consumed={received}", flush=True)


if __name__ == "__main__":
    main()
