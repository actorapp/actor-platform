//
//  CocoaWebserviceRuntime.swift
//  ActorSDK
//
//  Created by zhangshanbo on 16/5/3.
//  Copyright © 2016年 Steve Kite. All rights reserved.
//

import Foundation
import SwiftyJSON
import MBProgressHUD
class CocoaWebServiceRuntime: NSObject {

    func asyncPostRequest(url:String,method:String,withParams paramsDic:NSMutableDictionary!,withCallback callback:WebserviceCallback) //异步请求1
{
    let req = NSMutableURLRequest(URL: NSURL(string: url+"/"+method)!)
    req.HTTPMethod = "POST"
    req.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
    req.timeoutInterval=60
    var bodyStr=""
    for pairs in paramsDic
    {
        bodyStr += NSString(format: "\(pairs.key)=\(pairs.value)&") as String
    }
    req.HTTPBody=NSString(string: bodyStr).dataUsingEncoding(NSUTF8StringEncoding)
    let window = UIApplication.sharedApplication().windows[1]
    let hud = MBProgressHUD(window: window)
    hud.mode = MBProgressHUDMode.Indeterminate
    hud.removeFromSuperViewOnHide = true
    window.addSubview(hud)
    window.bringSubviewToFront(hud)
    hud.show(true)
    NSURLConnection.sendAsynchronousRequest(req, queue: NSOperationQueue.mainQueue()) { (resp, data, error) -> Void in
        if let e = error {
            print(e)
            callback.onNetworkProblem();
                  }
        if let d = data {
            let k = String(data: d , encoding: NSUTF8StringEncoding)
            print(k)
            let json = JSON(data:d)
            if(json==nil)
            {
                hud.hide(true)

                callback.onServiceError(k!);
                return
            }
            if(json["result"]==false)
            {
                hud.hide(true)
                callback.onServiceFail(json)
                return
            }
            hud.hide(true)

            callback.onServiceSuccess(json)
            
        }
    }
}
}
