//
//  WebViewController.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import SwiftUI
import WebKit

class WebKitViewController: UIViewController, WKNavigationDelegate {
    
    var webView: WKWebView!

    override func loadView() {
        webView.navigationDelegate = self
        view = webView
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        print("Done loading")
    }
}
