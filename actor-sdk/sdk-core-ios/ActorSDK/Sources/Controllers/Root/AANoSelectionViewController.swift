//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AANoSelectionViewController: AAViewController {
    
    public override init() {
        super.init(nibName: nil, bundle: nil)
        
        view.backgroundColor = appStyle.vcBackyardColor
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}