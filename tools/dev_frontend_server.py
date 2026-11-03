#!/usr/bin/env python3
"""Local static server for frontend/ that mirrors the production Caddy routing.

Same table as deploy/caddy/Caddyfile: `/` is the public landing page, the portal
lives under `/portal/` (hash routing, `/portal` redirects), `/impressum` and
`/datenschutz` are static pages, every /shop/<kennung> path gets shop.html, existing
files are served as-is and everything else answers 404 with 404.html.
"""

import argparse
import http.server
from urllib.parse import unquote
from functools import partial
from pathlib import Path

FRONTEND_DIR = Path(__file__).resolve().parent.parent / "frontend"

EXACT_PAGES = {
    "/": "/landing.html",
    "/impressum": "/impressum.html",
    "/datenschutz": "/datenschutz.html",
}


def route(path):
    """Return (file path, noindex) for a request path, or None for a 404."""
    path = path.split("?", 1)[0].split("#", 1)[0]
    if path in ("/shop", "/shop.html") or path.startswith("/shop/"):
        return "/shop.html", True
    if path.startswith("/portal/"):
        return "/index.html", True
    if path in EXACT_PAGES:
        return EXACT_PAGES[path], False
    if (FRONTEND_DIR / unquote(path).lstrip("/")).is_file():
        return path, False
    return None


class FrontendHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        self._serve(head=False)

    def do_HEAD(self):
        self._serve(head=True)

    def _serve(self, head):
        if self.path.split("?", 1)[0] == "/portal":
            self.send_response(308)
            self.send_header("Location", "/portal/")
            self.end_headers()
            return
        target = route(self.path)
        if target is None:
            self._not_found(head)
            return
        self.path, self._noindex = target
        if head:
            super().do_HEAD()
        else:
            super().do_GET()

    def _not_found(self, head):
        body = (FRONTEND_DIR / "404.html").read_bytes()
        self.send_response(404)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self._noindex = True
        self.end_headers()
        if not head:
            self.wfile.write(body)

    def end_headers(self):
        if getattr(self, "_noindex", False):
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
        print(
            f"Frontend: http://localhost:{args.port}  "
            f"(Portal: /portal/, Shops: /shop/<kennung>)"
        )
        server.serve_forever()


if __name__ == "__main__":
    main()
