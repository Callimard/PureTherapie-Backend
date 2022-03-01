#!/bin/bash

docker run --restart=always -d -t -p8585:8585 --network="host" \
  -v puretherapie-logs:/logs \
  -v puretherapie-properties:/configuration \
  puretherapie-backend:latest