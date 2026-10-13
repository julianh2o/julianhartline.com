#!/usr/bin/env bash
# Build the Jekyll site and upload it to julianhartline.com
set -euo pipefail
cd "$(dirname "$0")"

REMOTE="julianh2o@julianhartline.com"
REMOTE_DIR="~/julianhartline.com/"

# Submodules: drawmon is static and copied by Jekyll; toys need a webpack build
git submodule update --init

JEKYLL_ENV=production bundle exec jekyll build

# Toys use webpack 4, which needs the legacy OpenSSL provider on Node 17+
for toy in jeri kelli; do
  (cd "toys/$toy" && npm ci --no-audit --no-fund && NODE_OPTIONS=--openssl-legacy-provider npx webpack)
  mkdir -p "_site/toys/$toy"
  cp -r "toys/$toy/dist/." "_site/toys/$toy/"
done

# Copy the *contents* of _site (not the _site dir itself) into the remote folder
scp -r _site/. "$REMOTE:$REMOTE_DIR"
