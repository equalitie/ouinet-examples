//
//  WebKitView.swift
//  OuinetExample
//
//  Created by grant on 10/13/23.
//

import SwiftUI
import WebKit

struct WebKitView: UIViewControllerRepresentable {
    
    typealias UIViewControllerType = WebKitViewController
    
    let webView: WKWebView

    func makeUIViewController(context: Context) -> WebKitViewController {
        let vc = WebKitViewController()
        vc.webView = webView
        return vc
    }
    
    func updateUIViewController(_ uiViewController: WebKitViewController, context: Context) {
    }
}
