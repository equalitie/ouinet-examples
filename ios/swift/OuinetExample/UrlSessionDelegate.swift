//
//  UrlSessionViewController.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import Foundation
import UIKit

import WebKit
import SwiftUI

@MainActor class UrlSessionDelegate: NSObject, URLSessionDelegate {
    
    func loadUrl(model : UrlSessionModel) {
        let url = URL(string: model.urlString)
        if url == nil {
            return
        }
        var request = URLRequest(url: url!, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 60.0)
        let dhtGroup : String = model.urlString.replacingOccurrences(of: "^https?://", with: "", options: .regularExpression)
        request.httpMethod = "GET"
        request.setValue(dhtGroup, forHTTPHeaderField: "X-Ouinet-Group")
        print("\n")
        print(request)
        let config = URLSessionConfiguration.ephemeral
        let endpoint = NWEndpoint.hostPort(host: "127.0.0.1", port: 9077)
        let proxyConfig = ProxyConfiguration.init(httpCONNECTProxy: endpoint)
        config.proxyConfigurations = [proxyConfig]
        let session = URLSession(configuration: config,
                                 delegate: self,
                                 delegateQueue: nil)
        Task {
            do {
                let (data, response) = try await session.data(for: request)
                print("\ndata: ")
                print(data)
                print("\nresponse: ")
                print(response)
                model.submitResponse = response.description
                model.showResponse = true
            } catch {
                print("\nerror: ")
                print(error)
            }
        }
    }
    
    /* TODO: Download function does not currently work */
    func downloadUrl() {
        let task = URLSession.shared.downloadTask(with: URL(string: "http://localhost:9078/ca.pem")!) {
            (tempURL, response, error) in
            // Handle response, the download file is
            // at tempURL
            let file = NSURL(fileURLWithPath: Bundle.main.bundlePath).appendingPathComponent("ca.pem")!
            //print("error: \(String(describing: error))")
            // Early exit on error
            print("tempUrl: \(String(describing: tempURL))")
            print("file: \(file.path)")
            guard let tempURL = tempURL else {
                //completion(error)
                print("error 1: \(String(describing: error))")
                return
            }
            do {
                /*
                do {
                    // Remove any existing document at file
                    if FileManager.default.fileExists(atPath: file.path) {
                        try FileManager.default.removeItem(at: file)
                    }
                    
                    // Copy the tempURL to file
                    try FileManager.default.copyItem(
                        at: tempURL,
                        to: file
                    )
                    print("tempUrl: \(String(describing: tempURL))")
                    print("response: \(String(describing: response))")
                    //completion(nil)
                }
                */
                let documentsURL = try
                    FileManager.default.url(for: .documentDirectory,
                                            in: .userDomainMask,
                                            appropriateFor: nil,
                                            create: false)
                let savedURL = documentsURL.appendingPathComponent(tempURL.lastPathComponent)
                try FileManager.default.moveItem(at: tempURL, to: savedURL)
            }
            catch {
                print("error 2: \(error)")
            }
            // Handle potential file system errors
        }
        // Start the download
        task.resume()
    }
}
