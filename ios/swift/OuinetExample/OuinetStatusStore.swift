//
//  OuinetStatusStore.swift
//  OuinetExample
//
//  Created by grant on 10/13/23.
//

@MainActor class OuinetStatusStore: ObservableObject {
     @Published var ouinetStatus = OuinetStatus(state: "Client State: ?")
     
     var client : Client? = nil

     init(client: Client) {
         self.client = client
     }

     func loadStats() async {
         ouinetStatus = OuinetStatus(state: "Client State: \(client!.getStateAsString())")
     }
}
