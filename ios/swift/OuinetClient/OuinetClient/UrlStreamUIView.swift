//
//  UrlSessionVIew.swift
//  OuinetExample
//
//  Created by grant on 10/11/23.
//

import SwiftUI

struct UrlStreamUIView: View {
    @StateObject var model : UrlSessionModel
    var streamDelegate = UrlStreamDelegate()
    @FocusState private var urlIsFocused: Bool
    
    var body: some View {
        if (model.showResponse) {
            Text(model.submitResponse)
        }
        VStack {
            HStack {
                TextField(
                    "",
                    text: $model.urlString
                )
                .onSubmit {
                    streamDelegate.startStreaming(model: model)
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
                        streamDelegate.startStreaming(model: model)
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

