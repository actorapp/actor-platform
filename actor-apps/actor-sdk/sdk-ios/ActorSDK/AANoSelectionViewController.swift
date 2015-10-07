//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AANoSelectionViewController: UIViewController {
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        view.backgroundColor = ActorSDK.sharedActor().style.tableBackyardColor
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}