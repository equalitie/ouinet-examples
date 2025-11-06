//
//  OuinetExampleApp.swift
//  OuinetExample
//
//  Created by grant on 9/18/23.
//

import SwiftUI
import Ouinet

extension FileManager {
    func copyFileToDirectory(fileName name: String,
                             toPath path: String) throws
    {
        let srcPath = NSURL(fileURLWithPath: Bundle.main.bundlePath).appendingPathComponent(name)!.path
        let destPath = NSURL(fileURLWithPath: path).appendingPathComponent(name)!.path
        let bakPath = NSURL(fileURLWithPath: path).appendingPathComponent(name + ".bak")!.path
        if !self.fileExists(atPath: srcPath) {
            throw(NSError(domain: "", code: 0, userInfo: [NSLocalizedDescriptionKey : "File does not exist"]))
        }
        // TODO: using replaceItemAt gave errors, so wrote this workaround
        if (self.fileExists(atPath: destPath)) {
            if (self.fileExists(atPath: bakPath)) {
                try self.removeItem(atPath: bakPath)
            }
            try self.moveItem(atPath: destPath, toPath: bakPath)
        }
        try self.copyItem(atPath: srcPath, toPath: destPath)
    }
}

@main
struct OuinetExampleApp: App {
    var client : OuinetClient
    init() {
        UIApplication.shared.isIdleTimerDisabled = true
        let config = OuinetConfig.init()
        do {
            try FileManager.default.copyFileToDirectory(fileName: "cacert.pem", toPath: config!.getOuinetDirectory())
        }catch{
            print("\n")
            print(error)
        }
        config?.setCacheType(NSLocalizedString("CACHE_TYPE", comment: ""))
            .setCacheHttpPubKey(NSLocalizedString("CACHE_PUB_KEY", comment: ""))
            .setInjectorCredentials(NSLocalizedString("INJECTOR_CREDENTIALS", comment: ""))
            .setInjectorTlsCert(NSLocalizedString("INJECTOR_TLS_CERT", comment: ""))
            .setListenOnTcp("127.0.0.1:9077")
            .setFrontEndEp("127.0.0.1:9078")
            .setDisableOriginAccess(true)
        client = OuinetClient.init(config: config)
        client.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(client: client)
        }
    }
}
