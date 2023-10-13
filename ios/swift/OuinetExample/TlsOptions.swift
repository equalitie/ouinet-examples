//
//  TlsOptions.swift
//  OuinetExample
//
//  Created by grant on 10/10/23.
//

enum CipherSuite: UInt16, CaseIterable, Identifiable {

    case RSA_WITH_3DES_EDE_CBC_SHA = 10 //

    case RSA_WITH_AES_128_CBC_SHA = 47 //

    case RSA_WITH_AES_256_CBC_SHA = 53 //

    case RSA_WITH_AES_128_GCM_SHA256 = 156

    case RSA_WITH_AES_256_GCM_SHA384 = 157

    case RSA_WITH_AES_128_CBC_SHA256 = 60 //

    case RSA_WITH_AES_256_CBC_SHA256 = 61 //

    case ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = 49160 //

    case ECDHE_ECDSA_WITH_AES_128_CBC_SHA = 49161 //

    case ECDHE_ECDSA_WITH_AES_256_CBC_SHA = 49162 //

    case ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = 49170 //

    case ECDHE_RSA_WITH_AES_128_CBC_SHA = 49171 //

    case ECDHE_RSA_WITH_AES_256_CBC_SHA = 49172 //

    case ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = 49187 //

    case ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = 49188 //

    case ECDHE_RSA_WITH_AES_128_CBC_SHA256 = 49191 //

    case ECDHE_RSA_WITH_AES_256_CBC_SHA384 = 49192 //

    case ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = 49195

    case ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = 49196

    case ECDHE_RSA_WITH_AES_128_GCM_SHA256 = 49199

    case ECDHE_RSA_WITH_AES_256_GCM_SHA384 = 49200

    case ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = 52392

    case ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 = 52393

    case AES_128_GCM_SHA256 = 4865

    case AES_256_GCM_SHA384 = 4866

    case CHACHA20_POLY1305_SHA256 = 4867
    
    var id: Self { self }
}

enum ProtocolVersion : UInt16, CaseIterable, Identifiable {

    
    //@available(iOS, introduced: 13.0, deprecated: 15.0, message: "Use tls_protocol_version_TLSv12 or tls_protocol_version_TLSv13 instead.")
    case TLSv10 = 769

    //@available(iOS, introduced: 13.0, deprecated: 15.0, message: "Use tls_protocol_version_TLSv12 or tls_protocol_version_TLSv13 instead.")
    case TLSv11 = 770

    case TLSv12 = 771

    case TLSv13 = 772

    //@available(iOS, introduced: 13.0, deprecated: 15.0, message: "Use tls_protocol_version_DTLSv12 instead.")
    case DTLSv10 = 65279

    case DTLSv12 = 65277
    
    var id: Self { self }
}
