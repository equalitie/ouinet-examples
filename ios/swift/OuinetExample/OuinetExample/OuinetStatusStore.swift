//
//  OuinetStatusStore.swift
//  OuinetExample
//
//  Created by grant on 10/13/23.
//
import Ouinet

@MainActor class OuinetStatusStore: ObservableObject {
     @Published var ouinetStatus = OuinetStatus(state: "Client State: ?")
     
     var client : OuinetClient? = nil

     init(client: OuinetClient) {
         self.client = client
     }

     func loadStats() async {
         ouinetStatus = OuinetStatus(state: "Client State: \(client!.getStateAsString())")
     }
}
