//
//  UrlSessionViewDelegate.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import Foundation
import UIKit
import Network
import SwiftUI

@MainActor class UrlSessionDelegate: NSObject, URLSessionDelegate {
    
    var receivedData: Data?
  
    override init() {
        super.init()
    }
    
    func loadUrl(model : UrlSessionModel) {
        let url = URL(string: model.urlString)
        if url == nil {
            return
        }
        let request = URLRequest(url: url!, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 60.0)
        //let dhtGroup : String = model.urlString.replacingOccurrences(of: "^https?://", with: "", options: .regularExpression)
        //request.httpMethod = "GET"
        //request.setValue(dhtGroup, forHTTPHeaderField: "X-Ouinet-Group")
        //print("\n")
        print(request)
        let config = URLSessionConfiguration.default
        
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
    
    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive response: URLResponse,
                    completionHandler: @escaping (URLSession.ResponseDisposition) -> Void) {
        guard let response = response as? HTTPURLResponse,
            (200...299).contains(response.statusCode),
            let mimeType = response.mimeType,
            mimeType == "text/html" else {
            completionHandler(.cancel)
            return
        }
        completionHandler(.allow)
    }


    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        self.receivedData?.append(data)
        //print(data)
    }


    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        DispatchQueue.main.async {
            //self.loadButton.isEnabled = true
            if error != nil {
                //handleClientError(error)
            } else if let receivedData = self.receivedData,
                let string = String(data: receivedData, encoding: .utf8) {
                print(string)
            }
        }
    }
    
    func urlConnect() {
        let urlPath: String = "https://oldart.city/images/test-250KB.txt"
        let url: NSURL = NSURL(string: urlPath)!
        let request1: NSMutableURLRequest = NSMutableURLRequest(url: url as URL)
        
        request1.httpMethod = "GET"
        let queue:OperationQueue = OperationQueue()
        
        print("Request: \(String(describing: request1.httpBody))")
        
        NSURLConnection.sendAsynchronousRequest(request1 as URLRequest, queue: queue, completionHandler:{ (response: URLResponse?, data: Data?, error: Error?) -> Void in
                print("Response: \(String(describing: response))")
                print("Data: \(String(describing: data))")
            print("Error: \(error?.localizedDescription)")
            
        })
    }
    
    func loadFile(model : UrlSessionModel) {
        let url = URL(string: model.urlString)
        if url == nil {
            return
        }
        //var request = URLRequest(url: url!, cachePolicy: .reloadIgnoringLocalCacheData, timeoutInterval: 60.0)
        //print(request)
        let config = URLSessionConfiguration.default
        let endpoint = NWEndpoint.hostPort(host: "127.0.0.1", port: 9077)
        let proxyConfig = ProxyConfiguration.init(httpCONNECTProxy: endpoint)
        config.proxyConfigurations = [proxyConfig]
        let session = URLSession(configuration: config,
                                 delegate: self,
                                 delegateQueue: nil)
        let task = session.downloadTask(with: url!) { localURL, response, error in
            if let localURL = localURL {
                // Move the downloaded file to a desired location
                let destinationURL = FileManager.default.temporaryDirectory.appendingPathComponent("test-250KB.txt")
                try? FileManager.default.moveItem(at: localURL, to: destinationURL)
            }
        }
        task.resume()
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
