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

DEVICES='["Samsung Galaxy S20-10.0", "Xiaomi Redmi Note 11-11.0", "Motorola Moto G71 5G-11.0", "Samsung Galaxy S21-11.0", "Huawei Nova 11 SE-12.0", "Samsung Galaxy S21-12.0", "Google Pixel 6-12.0", "Samsung Galaxy S23-13.0", "Google Pixel 7-13.0", "OnePlus 11R-13.0"]'
# Test devices for higher Android versions: "Samsung Galaxy S24-14.0", "Google Pixel 9-15.0", "Samsung Galaxy S26-16.0", "Google Pixel 9-17.0"

curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/build" \
-d '{"class": ["ie.equalit.ouinet_examples.android_compose.OuinetStartTest"], "clearPackageData": "true", "deviceLogs": "true",  "devices": '"${DEVICES}"', "app": "ouinet-debug-latest", "testSuite": "ouinet-debug-androidTest-latest"}' \
-H "Content-Type: application/json"
