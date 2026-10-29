#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q clean test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/merge-test-classpath.txt
classpath="target/test-classes:target/classes:$(cat target/merge-test-classpath.txt)"
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) classpath="target/test-classes;target/classes;$(cat target/merge-test-classpath.txt)" ;; esac
for test_class in service.PaymentFlowRegressionTest service.RoomAssignmentTransactionTest util.WalkInNoShowPolicyTest service.DatabaseBackupRegressionTest service.JpaMappingSmokeTest; do
    java -cp "$classpath" "$test_class"
done
node src/test/js/payment-ui.test.js
