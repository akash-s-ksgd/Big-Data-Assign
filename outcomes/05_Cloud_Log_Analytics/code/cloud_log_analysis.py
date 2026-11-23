"""Local server-log proof of concept used when cloud credentials are unavailable."""
import argparse
import csv
import json
from collections import Counter
from pathlib import Path


def analyze(input_path: Path):
    with input_path.open(newline="", encoding="utf-8") as source:
        rows = list(csv.DictReader(source))
    status = Counter(row["status"] for row in rows)
    hourly = Counter(row["timestamp"][:13] for row in rows)
    server_errors = Counter(row["path"] for row in rows if int(row["status"]) >= 500)
    latencies = sorted(int(row["latency_ms"]) for row in rows)
    p95_index = max(0, int(0.95 * len(latencies) + 0.999999) - 1)
    return {
        "requests": len(rows),
        "status_counts": dict(sorted(status.items())),
        "server_errors": sum(count for code, count in status.items() if int(code) >= 500),
        "top_server_error_paths": server_errors.most_common(5),
        "request_counts_by_hour_utc": dict(sorted(hourly.items())),
        "peak_hour_utc": hourly.most_common(1)[0] if hourly else None,
        "p95_latency_ms_nearest_rank": latencies[p95_index] if latencies else None,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, default=Path("data/server_logs.csv"))
    parser.add_argument("--output", type=Path, default=Path("outputs/cloud_log_summary.json"))
    args = parser.parse_args()
    result = analyze(args.input)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, indent=2))
    print(f"saved={args.output}")


if __name__ == "__main__":
    main()
