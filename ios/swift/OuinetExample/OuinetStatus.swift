//
//  OuinetStatus.swift
//  OuinetExample
//
//  Created by grant on 10/13/23.
//

extension Client {
    func getStateAsString() -> String {
        switch self.getState() {
        case 0:
            return "Created"
        case 1:
            return "Failed"
        case 2:
            return "Starting"
        case 3:
            return "Degraded"
        case 4:
            return "Started"
        case 5:
            return "Stopping"
        case 6:
            return "Stopped"
        default:
            return "Unknown"
        }
    }
}

struct OuinetStatus: Identifiable, Hashable {
     let state: String
     let id = UUID()
 }
