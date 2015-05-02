//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class AASettingsNotificationsController: AATableViewController {
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(style:
            UITableViewStyle.Grouped)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = "Notifications and Sounds" // TODO: Localize
    }
    
    // MARK: - 
    // MARK: UITableView Data Source
    
    // MARK: -
    // MARK: UITableView Delegate

}
