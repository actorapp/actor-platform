//
//  AppCocoaHttpRuntime.swift
//  ActorApp
//
//  Created by Diego Ferreira da Silva on 07/12/16.
//  Copyright © 2016 Actor LLC. All rights reserved.
//

import Foundation
import ActorSDK

open class AppCocoaHttpRuntime: NSObject {
    
        static let queue:OperationQueue = OperationQueue()
    
        public class func getMethod(_ url: String) -> ARPromise! {
    
            return ARPromise { (resolver) in
    
                let request = NSMutableURLRequest(url: URL(string: url)!)
                
                let username = "LezxRxSDgFMA"
                let password = "X@U+e(>Hc^%h!V3]p*-6"
                let loginString = String(format: "%@:%@", username, password)
                let loginData = loginString.data(using: String.Encoding.utf8)!
                let base64LoginString = loginData.base64EncodedString()
                
                request.httpShouldHandleCookies = false
                request.cachePolicy = NSURLRequest.CachePolicy.reloadIgnoringLocalAndRemoteCacheData
                
                request.setValue("Basic \(base64LoginString)", forHTTPHeaderField: "Authorization")
                request.setValue("X@U+e(>Hc^%h!V3]p*-6", forHTTPHeaderField: "X-API-KEY")
                
                request.httpMethod = "GET"
    
                NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: self.queue, completionHandler:{ (response: URLResponse?, data: Data?, error: Error?) -> Void in
                    if let respHttp = response as? HTTPURLResponse {
                        if (respHttp.statusCode >= 200 && respHttp.statusCode < 300) {
                            resolver.result(ARHTTPResponse(code: jint(respHttp.statusCode), withContent: data!.toJavaBytes()))
                        } else {
                            resolver.error(ARHTTPError(int: jint(respHttp.statusCode)))
                        }
                    } else {
                        resolver.error(ARHTTPError(int: 0))
                    }
                })
            }
        }

}
