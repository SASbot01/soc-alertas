#!/usr/bin/env python3
"""
BlackWolf Defense - HTTP Domain Monitor
Monitors HTTP endpoints and reports threats to the BlackWolf SOC platform.

Checks: HTTP status, response time, SSL certificate, headers security.

Usage:
    python blackwolf_http_monitor.py --api-url https://soc.blackwolfsec.io/api/v1 \
        --api-key YOUR_KEY --company-id YOUR_COMPANY_ID \
        --domains gol.blackwolfsec.io gol.view.blackwolfsec.io minimal.blackwolfsec.io soc.blackwolfsec.io
"""

import argparse
import json
import logging
import socket
import ssl
import time
import uuid
from datetime import datetime, timezone

import requests

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
log = logging.getLogger("blackwolf-http-monitor")


def get_sensor_id(domain):
    """Generate a consistent sensor ID per domain."""
    return f"http-monitor-{domain.replace('.', '-')}"


def check_ssl_certificate(domain):
    """Check SSL certificate validity and expiration."""
    try:
        ctx = ssl.create_default_context()
        with ctx.wrap_socket(socket.socket(), server_hostname=domain) as s:
            s.settimeout(10)
            s.connect((domain, 443))
            cert = s.getpeercert()

        not_after = datetime.strptime(cert['notAfter'], '%b %d %H:%M:%S %Y %Z').replace(tzinfo=timezone.utc)
        days_left = (not_after - datetime.now(timezone.utc)).days

        return {
            "valid": True,
            "days_left": days_left,
            "expires": not_after.isoformat(),
            "issuer": dict(x[0] for x in cert.get('issuer', [])).get('organizationName', 'Unknown'),
            "subject": dict(x[0] for x in cert.get('subject', [])).get('commonName', domain),
        }
    except ssl.SSLError as e:
        return {"valid": False, "error": f"SSL Error: {e}", "days_left": -1}
    except socket.timeout:
        return {"valid": False, "error": "SSL connection timeout", "days_left": -1}
    except Exception as e:
        return {"valid": False, "error": str(e), "days_left": -1}


def check_security_headers(headers):
    """Check for missing security headers."""
    missing = []
    recommended = {
        "Strict-Transport-Security": "HSTS not set",
        "X-Content-Type-Options": "X-Content-Type-Options missing",
        "X-Frame-Options": "Clickjacking protection missing",
        "Content-Security-Policy": "CSP not configured",
    }
    for header, desc in recommended.items():
        if header.lower() not in {k.lower() for k in headers.keys()}:
            missing.append({"header": header, "description": desc})
    return missing


def check_domain(domain):
    """Perform full health check on a domain."""
    url = f"https://{domain}"
    result = {
        "domain": domain,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "http": {},
        "ssl": {},
        "threats": [],
        "packets": [],
    }

    # HTTP check
    try:
        start = time.time()
        resp = requests.get(url, timeout=15, allow_redirects=True)
        response_time = round((time.time() - start) * 1000)

        result["http"] = {
            "status_code": resp.status_code,
            "response_time_ms": response_time,
            "url_final": resp.url,
            "server": resp.headers.get("Server", "Unknown"),
        }

        # Packet record for every check
        result["packets"].append({
            "src": "monitor",
            "dst": domain,
            "port": 443,
            "protocol": "HTTPS",
            "size": len(resp.content),
        })

        # Threat: site down (5xx)
        if resp.status_code >= 500:
            result["threats"].append({
                "threat_type": "service_down",
                "severity": 9,
                "src_ip": "0.0.0.0",
                "dst_ip": domain,
                "dst_port": 443,
                "description": f"[{domain}] HTTP {resp.status_code} - Service returning server error"
            })

        # Threat: client error (4xx except 404)
        elif resp.status_code >= 400 and resp.status_code != 404:
            result["threats"].append({
                "threat_type": "service_degraded",
                "severity": 5,
                "src_ip": "0.0.0.0",
                "dst_ip": domain,
                "dst_port": 443,
                "description": f"[{domain}] HTTP {resp.status_code} - Unexpected client error"
            })

        # Threat: slow response (>5s)
        if response_time > 5000:
            result["threats"].append({
                "threat_type": "performance_degradation",
                "severity": 6,
                "src_ip": "0.0.0.0",
                "dst_ip": domain,
                "dst_port": 443,
                "description": f"[{domain}] Response time {response_time}ms exceeds 5s threshold"
            })
        elif response_time > 3000:
            result["threats"].append({
                "threat_type": "performance_degradation",
                "severity": 4,
                "src_ip": "0.0.0.0",
                "dst_ip": domain,
                "dst_port": 443,
                "description": f"[{domain}] Response time {response_time}ms - slow response"
            })

        # Check security headers
        missing_headers = check_security_headers(resp.headers)
        if len(missing_headers) >= 3:
            header_names = ", ".join(h["header"] for h in missing_headers)
            result["threats"].append({
                "threat_type": "misconfiguration",
                "severity": 4,
                "src_ip": "0.0.0.0",
                "dst_ip": domain,
                "dst_port": 443,
                "description": f"[{domain}] Missing security headers: {header_names}"
            })

    except requests.exceptions.ConnectionError:
        result["http"] = {"status_code": 0, "response_time_ms": -1}
        result["threats"].append({
            "threat_type": "service_down",
            "severity": 10,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] Connection refused - service completely unreachable"
        })
    except requests.exceptions.Timeout:
        result["http"] = {"status_code": 0, "response_time_ms": -1}
        result["threats"].append({
            "threat_type": "service_down",
            "severity": 9,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] Connection timeout after 15s"
        })
    except requests.exceptions.SSLError as e:
        result["http"] = {"status_code": 0, "response_time_ms": -1}
        result["threats"].append({
            "threat_type": "ssl_invalid",
            "severity": 8,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] SSL error during HTTP request: {e}"
        })

    # SSL certificate check
    ssl_info = check_ssl_certificate(domain)
    result["ssl"] = ssl_info

    if not ssl_info["valid"]:
        result["threats"].append({
            "threat_type": "ssl_invalid",
            "severity": 9,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] SSL certificate invalid: {ssl_info.get('error', 'Unknown')}"
        })
    elif ssl_info["days_left"] <= 7:
        result["threats"].append({
            "threat_type": "ssl_expiring",
            "severity": 8,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] SSL certificate expires in {ssl_info['days_left']} days!"
        })
    elif ssl_info["days_left"] <= 30:
        result["threats"].append({
            "threat_type": "ssl_expiring",
            "severity": 5,
            "src_ip": "0.0.0.0",
            "dst_ip": domain,
            "dst_port": 443,
            "description": f"[{domain}] SSL certificate expires in {ssl_info['days_left']} days"
        })

    return result


def send_to_soc(api_url, api_key, company_id, sensor_id, threats, packets):
    """Send monitoring data to SOC platform."""
    payload = {
        "api_key": api_key,
        "company_id": company_id,
        "sensor_id": sensor_id,
        "threats": threats,
        "packets": packets,
    }

    try:
        resp = requests.post(
            f"{api_url}/sensors/upload",
            json=payload,
            timeout=30,
            headers={"Content-Type": "application/json"}
        )
        resp.raise_for_status()
        data = resp.json()
        log.info(
            "  -> SOC upload OK: %d threats, %d packets",
            data.get("processed_threats", 0),
            data.get("processed_packets", 0),
        )
        return data
    except requests.exceptions.RequestException as e:
        log.error("  -> SOC upload failed: %s", e)
        return None


def main():
    parser = argparse.ArgumentParser(description="BlackWolf HTTP Domain Monitor")
    parser.add_argument("--api-url", required=True, help="BlackWolf API URL")
    parser.add_argument("--api-key", required=True, help="Company API key")
    parser.add_argument("--company-id", required=True, help="Company ID")
    parser.add_argument("--domains", nargs="+", required=True, help="Domains to monitor")
    parser.add_argument("--interval", type=int, default=120, help="Check interval in seconds (default: 120)")
    parser.add_argument("--once", action="store_true", help="Run once and exit")

    args = parser.parse_args()

    log.info("=" * 60)
    log.info("BlackWolf HTTP Domain Monitor")
    log.info("=" * 60)
    log.info("API: %s", args.api_url)
    log.info("Company: %s", args.company_id)
    log.info("Domains: %s", ", ".join(args.domains))
    log.info("Interval: %ds", args.interval)
    log.info("=" * 60)

    while True:
        for domain in args.domains:
            log.info("Checking %s ...", domain)
            result = check_domain(domain)

            # Log results
            http = result["http"]
            ssl_info = result["ssl"]
            status = http.get("status_code", 0)
            response_time = http.get("response_time_ms", -1)

            if status > 0:
                log.info("  HTTP: %d (%dms)", status, response_time)
            else:
                log.warning("  HTTP: UNREACHABLE")

            if ssl_info.get("valid"):
                log.info("  SSL: OK (%d days left, issuer: %s)", ssl_info["days_left"], ssl_info.get("issuer", "?"))
            else:
                log.warning("  SSL: INVALID - %s", ssl_info.get("error", "Unknown"))

            if result["threats"]:
                for t in result["threats"]:
                    log.warning("  THREAT [sev:%d] %s: %s", t["severity"], t["threat_type"], t["description"])
            else:
                log.info("  No threats detected")

            # Send to SOC with per-domain sensor ID
            sensor_id = get_sensor_id(domain)
            send_to_soc(
                args.api_url, args.api_key, args.company_id,
                sensor_id, result["threats"], result["packets"]
            )

        if args.once:
            log.info("Single run complete. Exiting.")
            break

        log.info("Next check in %ds...", args.interval)
        log.info("-" * 40)
        time.sleep(args.interval)


if __name__ == "__main__":
    main()
