//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class Config {
    
    // Tech
    var endpoints = [String]()
    var hockeyapp: String? = nil
    var pushId: Int? = nil
    
    // Support
    var supportEmail: String? = nil
    var activationEmail: String? = nil
    var supportAccount: String? = nil
    
    // Twitter
    var appTwitter: String? = nil
    
    // HomePage
    var appHomePage: String? = nil
    
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
            
            self.hockeyapp = configData["hockeyapp"] as? String
            self.pushId = configData["push_id"] as? Int
            self.appHomePage = configData["homepage"] as? String
            self.appTwitter = configData["twitter"] as? String
            
            self.supportAccount = configData["supportAccount"] as? String
            self.supportEmail = configData["supportEmail"] as? String
            self.activationEmail = configData["activationEmail"] as? String
        }
    }
}