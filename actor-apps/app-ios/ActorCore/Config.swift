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
    
    // Info
    var appTwitter: String? = nil
    var appHomePage: String? = nil
    var appInviteUrl: String? = nil
    
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
            
            if let info = configData["info"] as? NSDictionary {
                if let about = info["about"] as? NSDictionary {
                    self.appHomePage = about["web"] as? String
                    self.appTwitter = about["twitter"] as? String
                    self.appInviteUrl = about["invite"] as? String
                }
                if let support = info["support"] as? NSDictionary {
                    self.supportEmail = support["email"] as? String
                    self.activationEmail = support["activation_email"] as? String
                    self.supportAccount = support["in_app"] as? String
                }
            }
        }
    }
}