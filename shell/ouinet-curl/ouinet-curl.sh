#!/bin/sh
# Fetch the given group using cURL proxified through a Ouinet client
#
# Usage: fetch-group URI1 [URI2]
#
# Where URI is an exact URI like "https://example.com/foo/"
#
set -e

OUINET_ENDPOINT="127.0.0.1:8077"
TIMEOUT=25
RETRY=1

SEPARATOR="~"

WRITE_OUT="%{http_code}"
WRITE_OUT=${WRITE_OUT}${SEPARATOR}"%{header_json}"

CURL_PARAMS="--silent "
CURL_PARAMS=$CURL_PARAMS"--insecure "
CURL_PARAMS=$CURL_PARAMS"--proxy ${OUINET_ENDPOINT} "
CURL_PARAMS=$CURL_PARAMS"--connect-timeout ${TIMEOUT} "
CURL_PARAMS=$CURL_PARAMS"--max-time ${TIMEOUT} "
CURL_PARAMS=$CURL_PARAMS"--retry ${RETRY} "
CURL_PARAMS=$CURL_PARAMS"--output /dev/null "
CURL_PARAMS=$CURL_PARAMS"--write-out ${WRITE_OUT} "

if [ $# -lt 1 ]; then
    echo "Error: Missing URI parameter\n Usage: fetch-group URI" > /dev/stderr
    exit 1
fi


remove_fragment_from_url() {
  sed -e 's/#.*$//'
}

remove_scheme_from_url() {
  sed -e 's/^[a-z][-+.0-9a-z]*:\/\///i'
}

remove_trailing_slashes() {
  tr -s /
}

remove_leading_www() {
  sed -e 's/^www\.//i'
}

remove_new_lines() {
  tr -d "\n\r"
}

get_dht_group() {
  echo $1 | \
    remove_fragment_from_url |\
    remove_scheme_from_url   |\
    remove_trailing_slashes  |\
    remove_leading_www
}

http_request() {
  curl \
    ${CURL_PARAMS} \
    -H 'X-Ouinet-Group: '${1} \
    $2
#x-ouinet-source
}

filter_http_code() {
  cut -d${SEPARATOR} -f 1
}

filter_http_headers() {
  cut -d${SEPARATOR} -f 2 | \
    jq -c '."x-ouinet-source"[]'
}

for url in "$@"; do
    datetime=$(date +"%Y-%m-%dT%H:%M:%S")

    dht_group="$(get_dht_group ${url})"
    http_response=$( http_request ${dht_group} ${url} | remove_new_lines )
    http_code=$( echo ${http_response} | filter_http_code )
    http_headers=$( echo ${http_response} | filter_http_headers)
    echo "${datetime},${url},${dht_group},${http_code},${http_headers}"
done
