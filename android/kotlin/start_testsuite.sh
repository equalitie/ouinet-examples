#!/bin/bash

set -e
set -x

UPLOAD_APKS=0
while getopts u option; do
    case "$option" in
        u) UPLOAD_APKS=1;;
    esac
done

if [ $UPLOAD_APKS -eq 1 ]; then
  curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
  -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
  -F "file=@./app/build/outputs/apk/debug/app-debug.apk" \
  -F "custom_id=ouinet-debug-latest"

  curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
  -X POST "https://api-cloud.browserstack.com/app-automate/espresso/test-suite" \
  -F "file=@./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" \
  -F "custom_id=ouinet-debug-androidTest-latest"
fi

curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/build" \
-d '{"class": ["ie.equalit.ouinet_examples.android_kotlin.OuinetStartTest"], "clearPackageData": "true", "deviceLogs": "true", "devices": ["Samsung Galaxy Note 20-10.0"], "app": "ouinet-debug-latest", "testSuite": "ouinet-debug-androidTest-latest"}' \
-H "Content-Type: application/json"
