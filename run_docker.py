#!/usr/bin/env python3
"""Build and run a local WishMate instance with Docker Compose."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import secrets
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parent
PROJECT = "wishmate-" + hashlib.sha256(os.fsencode(ROOT)).hexdigest()[:12]
STATE = Path.home() / ".wishmate-docker" / PROJECT


def run(*command, env=None):
    subprocess.run(command, cwd=ROOT, env=env, check=True)


def prepare_state():
    STATE.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    if STATE.exists():
        return

    if shutil.which("openssl") is None:
        raise RuntimeError("Install OpenSSL and make it available on PATH.")

    with tempfile.TemporaryDirectory(dir=STATE.parent) as temporary:
        staging = Path(temporary) / "state"
        keys = staging / "keys"
        keys.mkdir(mode=0o700, parents=True)
        staging.chmod(0o700)
        private_key = keys / "jwt-private.pem"
        run(
            "openssl", "genpkey", "-algorithm", "RSA", "-pkeyopt",
            "rsa_keygen_bits:2048", "-out", str(private_key),
        )
        private_key.chmod(0o600)
        run(
            "openssl", "pkey", "-in", str(private_key), "-pubout",
            "-out", str(keys / "jwt-public.pem"),
        )

        credentials = {
            "WISHMATE_DB_PASSWORD": secrets.token_hex(32),
            "WISHMATE_DB_ROOT_PASSWORD": secrets.token_hex(32),
        }
        credentials_file = staging / "credentials.json"
        with open(credentials_file, "x", encoding="utf-8", opener=private_file) as output:
            json.dump(credentials, output)

        try:
            staging.rename(STATE)
        except OSError:
            if not STATE.is_dir():
                raise


def private_file(path, flags):
    return os.open(path, flags, 0o600)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", nargs="?", choices=("up", "down", "logs"), default="up")
    parser.add_argument("--port", type=int, default=8080, help="Local web port for up (default: 8080).")
    args = parser.parse_args()
    if not 1 <= args.port <= 65535:
        parser.error("--port must be between 1 and 65535.")

    try:
        if shutil.which("docker") is None:
            raise RuntimeError("Install Docker with the Docker Compose plugin.")
        run("docker", "compose", "version")
        if args.action == "up":
            if not (ROOT / "wishmate-schema.sql").is_file():
                raise RuntimeError("Copy the bundle into the repository root beside wishmate-schema.sql.")
            prepare_state()
        elif not STATE.is_dir():
            print("WishMate has not been started from this checkout.")
            return 0

        env = os.environ.copy()
        env.update(json.loads((STATE / "credentials.json").read_text(encoding="utf-8")))
        env.update(WISHMATE_KEYS_DIR=str(STATE / "keys"), WISHMATE_PORT=str(args.port))
        compose = ("docker", "compose", "-p", PROJECT, "-f", "compose.wishmate.yaml")

        if args.action == "up":
            run(*compose, "up", "--build", "--wait", "--wait-timeout", "240", env=env)
            print(f"WishMate is ready at http://localhost:{args.port}/auth/login")
        elif args.action == "down":
            run(*compose, "down", env=env)
        else:
            run(*compose, "logs", "--tail", "100", "--follow", env=env)
    except (OSError, RuntimeError, ValueError, subprocess.CalledProcessError) as error:
        print(f"WishMate: {error}", file=sys.stderr)
        if args.action == "up" and STATE.is_dir():
            print("Run python3 run_docker.py logs to inspect the containers.", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        return 130

    return 0


if __name__ == "__main__":
    sys.exit(main())
