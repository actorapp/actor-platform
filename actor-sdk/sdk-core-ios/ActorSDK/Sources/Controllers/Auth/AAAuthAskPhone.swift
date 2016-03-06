//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

class AAAuthAskPhone: AAAuthViewController {
    
    let name: String
    
    init(name: String) {
        self.name = name
        super.init()
        
        navigationItem.title = "Phone Number"
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = UIColor.whiteColor()
    }
    
}