//
//  ContentView.swift
//  OuinetVPN
//
//  Created by grant on 1/5/24.
//

import SwiftUI

struct ContentView: View {
    let service: VPNManager = .shared

    @State private var isLoading = false
    @State private var isShowingError = false
    @State private var errorMessage = ""
    @State private var isConfigPresented = false
    @State private var isWebViewPresented = false
    @State private var isUrlSessionPresented = false
    @State private var isAsyncClientPresented = false
    @State private var isUrlStreamPresented = false
    
    @State private var testUrl = Sites.example_com
    @StateObject var webViewModel = WebKitViewModel(url: Sites.example_com.rawValue)
    @StateObject var sessionModel = UrlSessionModel(url: Sites.example_com.rawValue)
    @StateObject var asyncModel = UrlSessionModel(url: Sites.example_com.rawValue)
    @StateObject var streamModel = UrlSessionModel(url: Sites.example_com.rawValue)

        var body: some View {
        NavigationView {
            VStack {
                header
                Spacer(minLength: 0)
                List {
                    Button("Open Ouinet Config") {
                        isConfigPresented = true
                    }
                    buttonInstall
                    Button("Test WkWebView") {
                        webViewModel.urlString = testUrl.rawValue
                        isWebViewPresented = true
                    }
                    Button("Test UrlSession") {
                        sessionModel.urlString = testUrl.rawValue
                        isUrlSessionPresented = true
                    }
                    Button("Test AsyncHTTPClient") {
                        asyncModel.urlString = testUrl.rawValue
                        isAsyncClientPresented = true
                    }
                    Button("Test UrlStream") {
                        streamModel.urlString = testUrl.rawValue
                        isUrlStreamPresented = true
                    }
                    Picker("Set default website:", selection: $testUrl) {
                        ForEach(Sites.allCases) { site in
                            Text("\(site.rawValue)")
                        }
                    }.pickerStyle(.inline)
                }
                .sheet(isPresented: $isConfigPresented){
                    // TODO: using a Safari view is a workaround to download the certificate
                    // safari has some magic sauce that lets put cert in correct location for install
                    SFSafariView(url: URL(string: "http://localhost:9078")!)
                }
                .sheet(isPresented: $isWebViewPresented) {
                    WebKitUIView(model: webViewModel)
                        .presentationDetents([.fraction(0.88)])
                }
                .sheet(isPresented: $isUrlSessionPresented) {
                    UrlSessionUIView(model: sessionModel)
                        .presentationDetents([.fraction(0.88)])
                }
                .sheet(isPresented: $isAsyncClientPresented) {
                    AsyncClientUIView(model: asyncModel)
                        .presentationDetents([.fraction(0.88)])
                }
                .sheet(isPresented: $isUrlStreamPresented) {
                    UrlStreamUIView(model: streamModel)
                        .presentationDetents([.fraction(0.88)])
                }
            }.padding()
        }
    }

    private var header: some View {
        VStack {
            Text("Ouinet Client")
                .font(.largeTitle)
                .fontWeight(.heavy)
    
        }
    }

    private var buttonInstall: some View {
        Button(
            "Install VPN Profile",
            action: self.installProfile
        )
    }

    private func installProfile() {
        isLoading = true

        service.installProfile { result in
            self.isLoading = false
            switch result {
            case .success:
                break // Do nothing, router will show what's next
            case let .failure(error):
                self.errorMessage = error.localizedDescription
                self.isShowingError = true
            }
        }
    }
}

enum Sites : String, CaseIterable, Identifiable {
    
    typealias RawValue = String
    
    case example_com = "https://example.com"
    
    case ouinet_work = "https://ouinet.work"

    case oldrart_city_60KB = "https://oldart.city/images/test-60KB.txt"

    case oldrart_city_250KB = "https://oldart.city/images/test-250KB.txt"

    case oldrart_city_500KB = "https://oldart.city/images/test-500KB.txt"

    case oldrart_city_1MB = "https://oldart.city/images/test-1MB.txt"

    case cdn_sanity_io = "https://cdn.sanity.io/images/86svaih1/production/06bf2cf62cd172ead83488b39a7faeef8a218273-3000x1998.jpg"

    var id: Self { self }
}

/*
#Preview {
    ContentView()
}
*/
