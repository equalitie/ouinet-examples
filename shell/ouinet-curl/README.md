# Ouinet's test application using shell and cURL

This script is a shell version of the [functions](https://gitlab.com/censorship-no/ceno-ext-settings/-/blob/master/background.js)
used by the JS Ceno browser extension.

The function `get_dht_group` invokes `remove_fragment_from_url`,
`remove_scheme_from_url`, `remove_trailing_slashes` and `remove_leading_www`
in order to clean the URL provided and make it fit the structure of Ouinet
groups.

After the URL is clean, it's passed to `http_request` function which is a
cURL wrapper using `--proxy` to forward the HTTP request to Ouinet's endpoint
running on `127.0.0.1:8077`.

Finally, the output is cleaned and grouped in a single line so the command can
be used with parallel or any other bulk execution tool.

Example:

    $ ./ouinet-curl https://debian.org
    2024-01-09T11:53:23,https://debian.org,debian.org,302,"injector"

The output of the command has the following fields: `${datetime}`, `${url}`,
`${dht_group}`, `${http_code}`, `${http_headers}`.
