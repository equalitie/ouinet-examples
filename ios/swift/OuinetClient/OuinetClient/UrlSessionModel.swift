//
//  UrlSessionModel.swift
//  OuinetExample
//
//  Created by grant on 10/10/23.
//

import Foundation
import Network

@MainActor class UrlSessionModel : ObservableObject {

    @Published var urlString: String
    @Published var submitResponse: String = ""
    @Published var showResponse: Bool = false
    
    init(url : String) {
        urlString = url
    }
}
