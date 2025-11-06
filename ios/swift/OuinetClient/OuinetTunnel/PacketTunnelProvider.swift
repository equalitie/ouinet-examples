//
//  PacketTunnelProvider.swift
//  OuinetTunnel
//
//  Created by grant on 1/5/24.
//

import NetworkExtension
import os.log
import Ouinet

class PacketTunnelProvider: NEPacketTunnelProvider {

    private var configuration: Configuration!
    private let log = OSLog(subsystem: "vpn-tunnel-ptp", category: "default")
    private var pendingCompletion: ((Error?) -> Void)?
    private var connection: NWTCPConnection!

    override func startTunnel(options: [String: NSObject]?, completionHandler: @escaping (Error?) -> Void) {
        os_log(.default, log: log, "Starting tunnel, options: %{private}@", "\(String(describing: options))")

        do {
            guard let proto = protocolConfiguration as? NETunnelProviderProtocol else {
                throw NEVPNError(.configurationInvalid)
            }
            self.configuration = try Configuration(proto: proto)
        } catch {
            os_log(.error, log: log, "Failed to read the configuration", error.localizedDescription)
            completionHandler(error)
        }

        os_log(.default, log: log, "Read configuration %{private}@", "\(String(describing: configuration))")

        self.pendingCompletion = completionHandler

        self.startTunnel()
    }
        
    private func startTunnel() {
        self.startOuinetClient()
        self.didSetupTunnel(address: "127.0.0.1")
    }
    
    private func startOuinetClient() {
        var client : OuinetClient
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
    
    private func initTunnelSettings(proxyHost: String, proxyPort: Int) -> NEPacketTunnelNetworkSettings {
        let settings: NEPacketTunnelNetworkSettings = NEPacketTunnelNetworkSettings(tunnelRemoteAddress: "127.0.0.1")

        /* proxy settings */
        let proxySettings: NEProxySettings = NEProxySettings()
        proxySettings.httpServer = NEProxyServer(
            address: proxyHost,
            port: proxyPort
        )
        proxySettings.httpsServer = NEProxyServer(
            address: proxyHost,
            port: proxyPort
        )
        proxySettings.autoProxyConfigurationEnabled = false
        proxySettings.httpEnabled = true
        proxySettings.httpsEnabled = true
        proxySettings.excludeSimpleHostnames = true
        proxySettings.exceptionList = [
            "192.168.0.0/16",
            "10.0.0.0/8",
            "172.16.0.0/12",
            "127.0.0.1",
            "localhost",
            "*.local",
            "0.0.0.0"
        ]
        settings.proxySettings = proxySettings

        /* ipv4 settings */
        let ipv4Settings: NEIPv4Settings = NEIPv4Settings(
            addresses: [settings.tunnelRemoteAddress],
            subnetMasks: ["255.255.255.255"]
        )
        ipv4Settings.includedRoutes = [NEIPv4Route.default()]
        ipv4Settings.excludedRoutes = [
            NEIPv4Route(destinationAddress: "192.168.0.0", subnetMask: "255.255.0.0"),
            NEIPv4Route(destinationAddress: "10.0.0.0", subnetMask: "255.0.0.0"),
            NEIPv4Route(destinationAddress: "172.16.0.0", subnetMask: "255.240.0.0")
        ]
        settings.ipv4Settings = ipv4Settings

        /* MTU */
        settings.mtu = 1500

        return settings
    }
    
    private func didSetupTunnel(address: String) {
        os_log(.default, log: self.log, "Did setup tunnel with address: %{public}@", "\(address)")

        let settings = initTunnelSettings(proxyHost: "127.0.0.1", proxyPort: 9077)
        // TODO: Configure DNS/split-tunnel/etc settings if needed

        setTunnelNetworkSettings(settings) { error in
            os_log(.default, log: self.log, "Did setup tunnel settings: %{public}@, error: %{public}@", "\(settings)", "\(String(describing: error))")

            self.pendingCompletion?(error)
            self.pendingCompletion = nil

            self.didStartTunnel()
        }
    }

    private func didStartTunnel() {
        readPackets()
    }

    private func readPackets() {
        let endpoint = NWHostEndpoint(hostname: "127.0.0.1", port: "9077")
        self.connection = self.createTCPConnection(to: endpoint, enableTLS: false, tlsParameters: nil, delegate: nil)

        packetFlow.readPackets {[weak self] (packets, protocols) in
            guard let strongSelf = self else { return }
            for packet in packets {
                strongSelf.connection.write(packet, completionHandler: { (error) in
                })
            }
            // Repeat
            strongSelf.readPackets()
        }
    }

    override func stopTunnel(with reason: NEProviderStopReason, completionHandler: @escaping () -> Void) {
        // Add code here to start the process of stopping the tunnel.
        completionHandler()
    }
    
    override func handleAppMessage(_ messageData: Data, completionHandler: ((Data?) -> Void)?) {
        // Add code here to handle the message.
        if let handler = completionHandler {
            handler(messageData)
        }
    }
    
    override func sleep(completionHandler: @escaping () -> Void) {
        // Add code here to get ready to sleep.
        completionHandler()
    }
    
    override func wake() {
        // Add code here to wake up.
    }
}

private struct Configuration {
    let username: String
    let password: String
    let hostname: String
    let port: String

    init(proto: NETunnelProviderProtocol) throws {
        guard let fullServerAddress = proto.serverAddress else {
            throw NEVPNError(.configurationInvalid)
        }
        let serverAddressParts = fullServerAddress.split(separator: ":")
        guard serverAddressParts.count == 2 else {
            throw NEVPNError(.configurationInvalid)
        }

        self.hostname = String(serverAddressParts[0])
        self.port = String(serverAddressParts[1])

        guard let username = proto.username else {
            throw NEVPNError(.configurationInvalid)
        }
        self.username = username

        self.password = "password"
    }
}

extension NWUDPSessionState: CustomStringConvertible {
    public var description: String {
        switch self {
        case .cancelled: return ".cancelled"
        case .failed: return ".failed"
        case .invalid: return ".invalid"
        case .preparing: return ".preparing"
        case .ready: return ".ready"
        case .waiting: return ".waiting"
        @unknown default: return "unknown"
        }
    }
}

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
