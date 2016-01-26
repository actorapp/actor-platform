//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

public class AATableViewController: AAViewController, UITableViewDataSource, UITableViewDelegate {
    
    public let tableView: UITableView
    public let tableViewStyle: UITableViewStyle
    
    public init(style: UITableViewStyle) {
        tableViewStyle = style
        tableView = UITableView(frame: CGRectZero, style: tableViewStyle)
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func loadView() {
        super.loadView()
        
        tableView.delegate = self
        tableView.dataSource = self
        view.addSubview(tableView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    public override func viewWillAppear(animated: Bool) {
        if let row = tableView.indexPathForSelectedRow {
            tableView.deselectRowAtIndexPath(row, animated: animated)
        }
        
        super.viewWillAppear(animated)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds;
    }

    public override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
    }
    
    private func placeholderViewFrame() -> CGRect {
        let navigationBarHeight: CGFloat = 64.5
        return CGRect(x: 0, y: navigationBarHeight, width: view.bounds.size.width, height: view.bounds.size.height - navigationBarHeight)
    }
    
    public func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    public func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 0
    }
    
    public func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        return UITableViewCell()
    }
    
}