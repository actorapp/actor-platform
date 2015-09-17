//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class Config {
    
    var endpoints = [String]()
    var mixpanel: String? = nil
    var hockeyapp: String? = nil
    var mint: String? = nil
    var pushId: Int? = nil
    
    init() {
        let path = NSBundle.mainBundle().pathForResource("app", ofType: "json")
        let text: String
        let parsedObject: AnyObject?
        do {
            text = try String(contentsOfFile: path!, encoding: NSUTF8StringEncoding)
            parsedObject = try NSJSONSerialization.JSONObjectWithData(text.asNS.dataUsingEncoding(NSUTF8StringEncoding)!,
                options: NSJSONReadingOptions.AllowFragments)
        } catch _ {
            fatalError("Unable to load config")
        }

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
            if let pushId = configData["push_id"] as? Int {
                self.pushId = pushId
            }
        }
    }
}