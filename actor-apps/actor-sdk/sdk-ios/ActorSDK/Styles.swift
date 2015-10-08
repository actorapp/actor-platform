//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public func initStyles() {
    
    // Text root styles
    
    registerStyle("root.text") { (s) -> () in
        s.foregroundColor = UIColor.blackColor()
    }
    
    registerStyle("root.accent") { (s) -> () in
        s.foregroundColor = UIColor.RGB(0x5085CB)
    }
    
    registerStyle("label", parent: "root.text") { (s) -> () in
        s.foregroundColor = s.foregroundColor!.alpha(0xDE/255.0)
    }
    
    registerStyle("hint") { (s) -> () in
        s.foregroundColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
    }
    
    // User online style
    
    registerStyle("user.online", parent: "root.accent")
    
    registerStyle("user.offline", parent: "hint")
}