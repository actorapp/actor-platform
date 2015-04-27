//
//  HttpProvider.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 27.04.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation
class HttpProvider: NSObject, AMHttpDownloaderProvider {
    func downloadPartWithNSString(url: String!, withInt startOffset: jint, withInt size: jint, withInt totalSize: jint, withImActorModelHttpFileDownloadCallback callback: ImActorModelHttpFileDownloadCallback!) {

        var nsUrl = NSURL(string: url)!
        var request = NSMutableURLRequest(URL: nsUrl)
        request.HTTPMethod = "GET"
        request.timeoutInterval = 60
        request.HTTPShouldHandleCookies=false
        request.addValue("bytes \(startOffset)-\(startOffset+size)/\(totalSize)", forHTTPHeaderField: "Content-Range")
        
        let queue:NSOperationQueue = NSOperationQueue()
        
        NSURLConnection.sendAsynchronousRequest(request, queue: queue, completionHandler:{ (response: NSURLResponse!, data: NSData!, error: NSError!) -> Void in
            
            callback.onDownloadedWithByteArray(data.toJavaBytes)
            
//            var err: NSError
            
//            var jsonResult: NSDictionary = NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers, error: nil) as NSDictionary
//             println("AsSynchronous\(jsonResult)")
            
            
        })
    }
    
    func uploadPartWithNSString(url: String!, withByteArray contents: IOSByteArray!, withImActorModelHttpFileUploadCallback callback: ImActorModelHttpFileUploadCallback!) {
        
    }
}