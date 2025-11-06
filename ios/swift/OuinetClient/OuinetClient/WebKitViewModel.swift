//
//  WebKitViewModel.swift
//  OuinetExample
//
//  Created by grant on 10/10/23.
//

import WebKit

class WebKitViewModel: ObservableObject {
    let webView: WKWebView
    @Published var urlString: String
    init(url : String) {
        print("Init webview model")
        webView = WKWebView(frame: .zero)
        urlString = url
    }
    
    func loadUrl() {
        print("loadUrl \(urlString)")
        guard let url = URL(string: urlString) else {
                    return
                }
        let request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 60.0)
        print(request)
        let endpoint = NWEndpoint.hostPort(host: "127.0.0.1", port: 9077)
        let proxyConfig = ProxyConfiguration.init(httpCONNECTProxy: endpoint)
        let websiteDataStore = WKWebsiteDataStore.default()
        websiteDataStore.proxyConfigurations = [proxyConfig]
        webView.configuration.websiteDataStore = websiteDataStore
        webView.configuration.suppressesIncrementalRendering = true
        webView.isInspectable = true
        webView.load(request)
    }
}
