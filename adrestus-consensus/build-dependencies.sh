#!/bin/bash

# Attempt to build the entire project
echo "Attempting to package the project..."
cd adrestus-consensus
pwd
mvn clean install -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip
mvn clean compile test-compile
BUILD_STATUS=$?

# If the build succeeds, exit
if [ $BUILD_STATUS -eq 0 ]; then
  echo "Build successful!"
  exit 0
fi

echo "Build failed! Identifying dependencies..."
echo "Retrying main build..."
cd ..
pwd
mvn clean package -Dmaven.test.skip
