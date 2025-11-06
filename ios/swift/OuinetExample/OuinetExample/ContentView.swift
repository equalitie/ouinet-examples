//
//  ContentView.swift
//  OuinetExample
//
//  Created by grant on 9/18/23.
//

import SwiftUI
import WebKit
import Ouinet

struct ContentView: View {
    @State var getUrl: String = "https://wikipedia.org"
    @State var client: OuinetClient
    @ObservedObject var store: OuinetStatusStore
    //@State private var selectedCipher: CipherSuite = .RSA_WITH_AES_256_GCM_SHA384
    //@State private var selectedVersion: ProtocolVersion = .TLSv13
    @State var isDownloadPresented = false
    @State var isSettingsPresented = false
    @State var isUrlSessionPresented = false
    @State var isWebViewPresented = false
    
    init(client: OuinetClient) {
        self.client = client
        self.store = OuinetStatusStore(client: client)
    }
    
    @FocusState private var urlIsFocused: Bool
    var body: some View {
        
        NavigationView {
            List {
                /* TODO: picker example, reuse for ouinet settings maybe
                Picker("TLS version", selection: $selectedVersion) {
                    ForEach(ProtocolVersion.allCases) { proto in
                        Text("\(proto.rawValue.description)")
                    }
                }
                Picker("Cipher", selection: $selectedCipher) {
                    ForEach(CipherSuite.allCases) { cipher in
                        Text("\(cipher.id.rawValue.description)")
                    }
                }
                */
                Text("\(store.ouinetStatus.state)")
                Button("Download CA Cert") {
                    isDownloadPresented = true
                }
                Button("Open Ouinet Settings") {
                    isSettingsPresented = true
                }
                /*
                Button("Test UrlSession") {
                    isUrlSessionPresented = true
                }
                 */
                Button("Test WebView") {
                    isWebViewPresented = true
                }
            }
            .refreshable {
                await store.loadStats()
            }
            .navigationTitle("Ouinet Example")
            .sheet(isPresented: $isDownloadPresented){
                // TODO: using a Safari view is a workaround to download the certificate
                // safari has some magic sauce that lets put cert in correct location for install
                SFSafariView(url: URL(string: "http://localhost:9078/ca.pem")!)
            }
            .sheet(isPresented: $isSettingsPresented){
                SFSafariView(url: URL(string: "http://localhost:9078/")!)
            }
            /*
            .sheet(isPresented: $isUrlSessionPresented) {
                UrlSessionUIView()
                    .presentationDetents([.fraction(0.88)])
            }
             */
            .sheet(isPresented: $isWebViewPresented) {
                WebKitUIView()
                    .presentationDetents([.fraction(0.88)])
            }
        }
    }
}

/* TODO: Preview will not work until ouinet-ios framework supports both arm64 and x86_64 arch */
/*
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(client: Client.init(config: nil))
    }
}
*/
