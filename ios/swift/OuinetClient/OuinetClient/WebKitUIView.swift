//
//  WebKitUIView.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import Foundation
import SwiftUI
import WebKit

struct WebKitUIView: View {
    @StateObject var model : WebKitViewModel
    @FocusState private var urlIsFocused: Bool
    var body: some View {
        WebKitView(webView: model.webView)
        VStack {
            HStack {
                TextField(
                    "",
                    text: $model.urlString
                )
                .onSubmit {
                    model.loadUrl()
                }
                .textInputAutocapitalization(.never)
                .disableAutocorrection(true)
                .frame(height: 48)
                .multilineTextAlignment(.center)
                .padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                .cornerRadius(5)
                .overlay(
                    RoundedRectangle(cornerRadius: 5)
                        .stroke(lineWidth: 1.0)
                )
                .submitLabel(.go)
                .onAppear { UITextField.appearance().clearButtonMode = .whileEditing }
                .focused($urlIsFocused)
                
                if !urlIsFocused {
                    Button(action: {
                        model.loadUrl()
                    }){
                        Text("Go")
                            .foregroundColor(.blue)
                    }
                    .frame(width: 48, height: 48)
                    .multilineTextAlignment(.center)
                    .padding(EdgeInsets(top: 0, leading: 6, bottom: 0, trailing: 6))
                    .cornerRadius(5)
                    .overlay(
                        RoundedRectangle(cornerRadius: 5)
                            .stroke(lineWidth: 2.0)
                            .fill(.blue)
                        )

                }
            }
        }.toolbar {
            ToolbarItem(placement: .keyboard) {
                    Spacer()
            }
            ToolbarItem(placement: .keyboard) {
                Button(action: {
                        urlIsFocused = false
                }) {
                    Image(systemName: "chevron.down")
                }
            }
        }
    }
}
