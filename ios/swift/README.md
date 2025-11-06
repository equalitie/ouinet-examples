# Ouinet's test iOS application in Swift

## Prepare your app for using Ouinet

Build `ouinet.xcframework` using https://gitlab.com/equalitie/ouinet/-/blob/main/scripts/build-darwin.sh

After building, copy `ouinet.xcframework` into the root of the Xcode workspace. 

Add the `ouinet.xcframework` to your Xcode project's "Frameworks, Libraries, and Embedded Content" via the project settings page.

In the main routine of your app, import the Ouinet package, initialize a `OuinetClient` and a `OuinetConfig` object. Then start the ouinet client:
```swift
import Ouinet

@main
struct OuinetExampleApp: App {
    var client : OuinetClient
    init() {
        let config = OuinetConfig.init()
        config?.setCacheType("bep5-http")
        client = OuinetClient.init(config: config)
        client.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```
This is the minimum code required to get ouinet started. However, it won't be very useful without setting more configuration options, see the `OuinetExampleApp.swift` file for a more complete example of starting the ouinet client.

## Pass config values to Ouinet

You can add the Ouinet keys and passwords to the project in the `Localizable.strings` file.
Set the values as follows before building the app:
```swift
"CACHE_PUB_KEY" = "YOUR OUINET CACHE PUB KEY";
"INJECTOR_CREDENTIALS" = "ouinet:YOURINJECTORPASSWORD";
// It's important to keep the new line characters in the beggining and the end
// of certificate delimiters
"INJECTOR_TLS_CERT" = "-----BEGIN CERTIFICATE-----\nABCDEFG...\n-----END CERTIFICATE-----";
```

and can be referenced after that from Kotlin via `BuildConfig`:

```swift
let config = Config.init()
    // ...
    .setCacheType(NSLocalizedString("CACHE_TYPE", comment: ""))
    .setCacheHttpPubKey(NSLocalizedString("CACHE_PUB_KEY", comment: ""))
    .setInjectorCredentials(NSLocalizedString("INJECTOR_CREDENTIALS", comment: ""))
    .setInjectorTlsCert(NSLocalizedString("INJECTOR_TLS_CERT", comment: ""))
```

## Send an HTTP request through Ouinet

TODO

## Validate Ouinet's TLS cert

TODO

## Test Ouinet access mechanisms

TODO
