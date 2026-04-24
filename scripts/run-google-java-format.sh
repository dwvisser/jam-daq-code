#!/bin/sh
set -e

CACHE_DIR="$HOME/.cache/google-java-format"
JAR="$CACHE_DIR/google-java-format-1.35.0-all-deps.jar"

mkdir -p "$CACHE_DIR"

if [ ! -f "$JAR" ]; then
  echo "Downloading google-java-format..."
  curl -fsSL -o "$JAR" "https://github.com/google/google-java-format/releases/download/v1.35.0/google-java-format-1.35.0-all-deps.jar"
fi

java -jar "$JAR" --replace "$@"
