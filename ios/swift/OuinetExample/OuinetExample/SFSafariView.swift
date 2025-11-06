//
//  SFSafariView.swift
//  OuinetExample
//
//  Created by grant on 12/13/23.
//

import SwiftUI
import WebKit
import SafariServices

struct SFSafariView: UIViewControllerRepresentable {

    typealias UIViewControllerType = SFSafariViewController

    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let vc = SFSafariViewController(url: url)
        return vc
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {
    }
}
