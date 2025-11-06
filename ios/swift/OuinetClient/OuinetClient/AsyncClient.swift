//
//  AsyncClient.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import Foundation
import UIKit
import SwiftUI
import AsyncHTTPClient

@MainActor class AsyncClient: NSObject {

    let httpClient: HTTPClient
    var receivedData: Data?
  
    override init() {
        let proxy = HTTPClient.Configuration.Proxy.server(host: "127.0.0.1", port: 9077)
        let config = HTTPClient.Configuration(proxy: proxy)
        httpClient = HTTPClient(eventLoopGroupProvider: .singleton, configuration: config)
        super.init()
    }

    func loadUrl(model: UrlSessionModel) {
        /// MARK: - Using SwiftNIO EventLoopFuture
        httpClient.get(url: model.urlString).whenComplete { result in
            switch result {
            case .failure(let error):
                // process error
                print(error)
            case .success(let response):
                if response.status == .ok {
                    // handle response
                    print("OKAY")
                    print(response.headers)
                    print(String(describing: response.body))
                    let bytes : String = String(response.body?.readableBytes ?? 0)
                    DispatchQueue.main.async{
                        // TODO: format header, and put read bytes into other examples...
                        model.submitResponse = "readableBytes: \(bytes)\n \(String(describing: response.headers))"
                        model.showResponse = true
                    }
                } else {
                    // handle remote error
                    print("ERROR?")
                }
            }
        }
    }
}
