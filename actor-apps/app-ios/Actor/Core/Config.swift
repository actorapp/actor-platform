//
//  Config.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 06.07.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class Config {
    
    var endpoints = [String]()
    var mixpanel: String? = nil
    var hockeyapp: String? = nil
    var mint: String? = nil
    var enableCommunity: Bool? = nil
    var pushId: Int? = nil
    
    init() {
        let path = NSBundle.mainBundle().pathForResource("app", ofType: "json")
        var text = String(contentsOfFile: path!, encoding: NSUTF8StringEncoding, error: nil)!

        var parseError: NSError?
        let parsedObject: AnyObject? = NSJSONSerialization.JSONObjectWithData((text as NSString).dataUsingEncoding(NSUTF8StringEncoding)!,
            options: NSJSONReadingOptions.AllowFragments,
            error:&parseError)
        if let configData = parsedObject as? NSDictionary {
            if let endpoints = configData["endpoints"] as? NSArray {
                for endpoint in endpoints {
                    self.endpoints.append(endpoint as! String)
                }
            }
            if let mixpanel = configData["mixpanel"] as? String {
                self.mixpanel = mixpanel
            }
            if let hockeyapp = configData["hockeyapp"] as? String {
                self.hockeyapp = hockeyapp
            }
            if let mint = configData["mint"] as? String {
                self.mint = mint
            }
            if let enableCommunity = configData["community"] as? Bool {
                self.enableCommunity = enableCommunity
            }
            if let pushId = configData["push_id"] as? Int {
                self.pushId = pushId
            }
        }
    }
}