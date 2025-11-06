//
//  UrlSessionViewController.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import Foundation
import UIKit
import SwiftUI
import Network

@MainActor class UrlStreamDelegate: NSObject, URLSessionDelegate {

    private var session: URLSession! = nil

        override init() {
            super.init()
            let config = URLSessionConfiguration.default
            let endpoint = NWEndpoint.hostPort(host: "127.0.0.1", port: 9077)
            let proxyConfig = ProxyConfiguration.init(httpCONNECTProxy: endpoint)
            config.proxyConfigurations = [proxyConfig]
            session = URLSession(configuration: config,
                                     delegate: self,
                                     delegateQueue: nil)
        }

    private var streamingTask: URLSessionDataTask? = nil

    var isStreaming: Bool { return self.streamingTask != nil }

    func startStreaming(model: UrlSessionModel) {
        precondition( !self.isStreaming )

        let url = URL(string: "https://oldart.city/images/test-250KB.txt")!
        let request = URLRequest(url: url, cachePolicy: .reloadIgnoringCacheData)
        let task = self.session.dataTask(with: request)
        self.streamingTask = task
        self.streamingTask?.resume()
    }

    func stopStreaming() {
        guard let task = self.streamingTask else {
            return
        }
        self.streamingTask = nil
        task.cancel()
        self.closeStream()
    }

    var outputStream: OutputStream? = nil

    private func closeStream() {
        if let stream = self.outputStream {
            stream.close()
            self.outputStream = nil
        }
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, needNewBodyStream completionHandler: @escaping (InputStream?) -> Void) {
        self.closeStream()

        var inStream: InputStream? = nil
        var outStream: OutputStream? = nil
        Stream.getBoundStreams(withBufferSize: 65536, inputStream: &inStream, outputStream: &outStream)
        self.outputStream = outStream

        completionHandler(inStream)
    }

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        NSLog("task data: %@", data as NSData)
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        if let error = error as NSError? {
            NSLog("task error: %@ / %d", error.domain, error.code)
        } else {
            NSLog("task complete")
        }
    }
}
