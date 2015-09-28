//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AATableViewController: AAViewController, UITableViewDataSource, UITableViewDelegate {
    
    var tableView: UITableView!
    var tableViewStyle: UITableViewStyle!
    
    init(style: UITableViewStyle) {
        super.init()
        
        tableViewStyle = style
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func loadView() {
        super.loadView()
        
        tableView = UITableView(frame: view.bounds, style: tableViewStyle)
        tableView.delegate = self
        tableView.dataSource = self
        view.addSubview(tableView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment:"Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func viewWillAppear(animated: Bool) {
        if let row = tableView.indexPathForSelectedRow {
            tableView.deselectRowAtIndexPath(row, animated: animated)
        }
        
        super.viewWillAppear(animated)
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds;
    }

    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
    }
    
    private func placeholderViewFrame() -> CGRect {
        let navigationBarHeight: CGFloat = 64.5
        return CGRect(x: 0, y: navigationBarHeight, width: view.bounds.size.width, height: view.bounds.size.height - navigationBarHeight)
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 0
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 0
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return UITableViewCell()
    }
    
}