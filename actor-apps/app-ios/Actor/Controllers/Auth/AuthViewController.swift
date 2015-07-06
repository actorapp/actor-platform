//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class AuthViewController: AAViewController {
    func onAuthenticated() {
        (UIApplication.sharedApplication().delegate as! AppDelegate).onLoggedIn(true)
    }
}