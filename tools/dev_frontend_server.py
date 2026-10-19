#!/usr/bin/env python3
"""Local static server for frontend/ that mirrors the production Caddy routing.

Like deploy/caddy/Caddyfile, every /shop/<kennung> path is answered with shop.html,
so public shop links look the same locally and in production. Everything else is
served as plain static files (the portal itself uses hash routing).
"""

import argparse
import http.server
from functools import partial
from pathlib import Path

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"


class FrontendHandler(http.server.SimpleHTTPRequestHandler):
    @staticmethod
    def _is_shop_path(path):
        path = path.split("?", 1)[0]
        return path == "/shop" or path.startswith("/shop/") or path == "/shop.html"

    def translate_path(self, path):
        if self._is_shop_path(path):
            path = "/shop.html"
        return super().translate_path(path)

    def end_headers(self):
        if self._is_shop_path(self.path):
            self.send_header("X-Robots-Tag", "noindex")
        # Always fetch fresh files while developing.
        self.send_header("Cache-Control", "no-store")
        super().end_headers()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=3000)
    args = parser.parse_args()
    handler = partial(FrontendHandler, directory=str(FRONTEND_DIR))
    with http.server.ThreadingHTTPServer(("localhost", args.port), handler) as server:
        print(f"Frontend: http://localhost:{args.port}  (Shops: /shop/<kennung>)")
        server.serve_forever()


if __name__ == "__main__":
    main()
