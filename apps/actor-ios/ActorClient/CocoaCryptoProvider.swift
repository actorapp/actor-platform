//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import Foundation

class CocoaCryptoProvider : BCBouncyCastleProvider {
    override init() {
        super.init(BCRandomProvider: AMJavaRandomProvider())
    }
}