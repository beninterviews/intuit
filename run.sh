#!/bin/bash

cd "$(dirname "$0")"

JAR="target/backend-tech-assessment-standard-*\.jar"
JAR_FILE=$(ls $JAR | grep -v "sources\.jar$" | grep -v "javadoc\.jar$" 2> /dev/null)

CMD="java -jar $JAR_FILE server target/config/config.yaml"

exec ${CMD}