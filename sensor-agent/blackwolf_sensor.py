#!/usr/bin/env python3
"""
BlackWolf Defense - Sensor Agent
Collects network events and sends them to the BlackWolf SOC platform.

Usage:
    python blackwolf_sensor.py --api-url http://your-server/api/v1 --api-key YOUR_KEY --company-id YOUR_COMPANY_ID

Requirements:
    pip install requests
"""

import argparse
import json
import logging
import random
import socket
import time
import uuid
from datetime import datetime

import requests

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
log = logging.getLogger("blackwolf-sensor")

# Simulated threat types for demo mode
THREAT_TYPES = [
    "brute_force", "port_scan", "ddos", "sql_injection", "xss",
    "malware_download", "c2_communication", "data_exfiltration",
    "dns_tunneling", "phishing", "privilege_escalation"
]

EXAMPLE_IPS = [
    "185.220.101.34", "45.155.205.233", "194.26.192.64",
    "91.240.118.172", "23.129.64.210", "178.128.23.9",
    "103.253.41.98", "5.188.206.14", "212.70.149.73"
]


def get_sensor_id():
    """Generate a consistent sensor ID based on hostname."""
    hostname = socket.gethostname()
    return f"sensor-{hostname}-{uuid.uuid5(uuid.NAMESPACE_DNS, hostname).hex[:8]}"


def generate_demo_threats(count=None):
    """Generate simulated threat events for demo/testing."""
    if count is None:
        count = random.randint(0, 5)

    threats = []
    for _ in range(count):
        severity = random.choices(range(1, 11), weights=[1, 2, 3, 4, 5, 5, 4, 3, 2, 1])[0]
        threats.append({
            "threat_type": random.choice(THREAT_TYPES),
            "severity": severity,
            "src_ip": random.choice(EXAMPLE_IPS),
            "dst_ip": f"10.0.{random.randint(1, 254)}.{random.randint(1, 254)}",
            "dst_port": random.choice([22, 80, 443, 3306, 3389, 8080, 8443, 5432]),
            "description": f"Simulated threat detected at {datetime.now().isoformat()}"
        })
    return threats


def generate_demo_packets(count=None):
    """Generate simulated packet data."""
    if count is None:
        count = random.randint(50, 500)

    packets = []
    for _ in range(count):
        packets.append({
            "src": f"10.0.{random.randint(1, 254)}.{random.randint(1, 254)}",
            "dst": f"10.0.{random.randint(1, 254)}.{random.randint(1, 254)}",
            "port": random.choice([22, 80, 443, 3306, 8080]),
            "protocol": random.choice(["TCP", "UDP", "ICMP"]),
            "size": random.randint(64, 1500)
        })
    return packets


def send_upload(api_url, api_key, company_id, sensor_id, threats, packets):
    """Send sensor data to BlackWolf SOC."""
    payload = {
        "api_key": api_key,
        "company_id": company_id,
        "sensor_id": sensor_id,
        "threats": threats,
        "packets": packets
    }

    try:
        response = requests.post(
            f"{api_url}/sensors/upload",
            json=payload,
            timeout=30,
            headers={"Content-Type": "application/json"}
        )
        response.raise_for_status()
        data = response.json()

        log.info(
            "Upload OK: %d threats, %d packets | Commands: %d",
            data.get("processed_threats", 0),
            data.get("processed_packets", 0),
            len(data.get("commands", []))
        )

        # Process commands from SOC
        commands = data.get("commands", [])
        for cmd in commands:
            if cmd.get("action") == "block_ip":
                log.warning("BLOCK IP: %s (reason: %s)", cmd.get("target"), cmd.get("reason"))
                # In production: execute iptables/firewall rules here

        return data

    except requests.exceptions.RequestException as e:
        log.error("Upload failed: %s", e)
        return None


def main():
    parser = argparse.ArgumentParser(description="BlackWolf Defense Sensor Agent")
    parser.add_argument("--api-url", required=True, help="BlackWolf API URL (e.g., http://localhost:8081/api/v1)")
    parser.add_argument("--api-key", required=True, help="Company API key")
    parser.add_argument("--company-id", required=True, help="Company ID")
    parser.add_argument("--sensor-id", default=None, help="Sensor ID (auto-generated if not set)")
    parser.add_argument("--interval", type=int, default=60, help="Upload interval in seconds (default: 60)")
    parser.add_argument("--demo", action="store_true", help="Run in demo mode with simulated data")
    parser.add_argument("--once", action="store_true", help="Send one upload and exit")

    args = parser.parse_args()

    sensor_id = args.sensor_id or get_sensor_id()

    log.info("BlackWolf Sensor Agent starting")
    log.info("Sensor ID: %s", sensor_id)
    log.info("API URL: %s", args.api_url)
    log.info("Company: %s", args.company_id)
    log.info("Mode: %s", "DEMO" if args.demo else "LIVE")
    log.info("Interval: %ds", args.interval)

    while True:
        if args.demo:
            threats = generate_demo_threats()
            packets = generate_demo_packets()
        else:
            # In production, you would collect real data here
            # For now, empty lists
            threats = []
            packets = []

        log.info("Sending: %d threats, %d packets", len(threats), len(packets))
        send_upload(args.api_url, args.api_key, args.company_id, sensor_id, threats, packets)

        if args.once:
            break

        time.sleep(args.interval)


if __name__ == "__main__":
    main()
