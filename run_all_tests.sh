#!/bin/bash
set -e

# Jmix Insurance Test Execution Script
# Runs all clean and test tasks from the root composite build context.

echo "============================================="
echo "Cleaning and running tests for Jmix Insurance"
echo "============================================="

# Ensure we are in the script's directory (project root)
cd "$(dirname "$0")"

echo "Executing clean tasks..."
./gradlew \
  :common:common:clean \
  :common:common-starter:clean \
  :security:security:clean \
  :security:security-starter:clean \
  :partner-core:partner-core:clean \
  :partner-api:partner-api:clean \
  :partner-api:partner-api-starter:clean \
  :product-core:product-core:clean \
  :product-api:product-api:clean \
  :product-api:product-api-starter:clean \
  :policy-core:policy-core:clean \
  :policy-api:policy-api:clean \
  :policy-api:policy-api-starter:clean

echo "Executing test tasks..."
./gradlew \
  :common:common:test \
  :security:security:test \
  :partner-core:partner-core:test \
  :product-core:product-core:test \
  :policy-core:policy-core:test

echo "============================================="
echo "All modules cleaned and tested successfully!"
echo "============================================="
