//
//  WebViewModel.swift
//  OuinetExample
//
//  Created by grant on 10/10/23.
//

import WebKit

class WebKitViewModel: ObservableObject {
    let webView: WKWebView
    @Published var urlString: String = "https://wikipedia.org"

    init() {
        print("Init webview model")
        webView = WKWebView(frame: .zero)
    }
    
    func loadUrl() {
        print("loadUrl \(urlString)")
        guard let url = URL(string: urlString) else {
                    return
                }
        var request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 60.0)
        let dhtGroup : String = urlString.replacingOccurrences(of: "^https?://", with: "", options: .regularExpression)
        request.httpMethod = "GET"
        request.setValue(dhtGroup, forHTTPHeaderField: "X-Ouinet-Group")
        print("\n")
        print(request)
        let endpoint = NWEndpoint.hostPort(host: "127.0.0.1", port: 9077)
        let proxyConfig = ProxyConfiguration.init(httpCONNECTProxy: endpoint)
                let websiteDataStore = WKWebsiteDataStore.default()
        websiteDataStore.proxyConfigurations = [proxyConfig]
        webView.configuration.websiteDataStore = websiteDataStore
        webView.load(request)
    }
}
