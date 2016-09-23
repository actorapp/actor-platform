//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AATableViewController: AAViewController, UITableViewDataSource, UITableViewDelegate {
    
    open let tableView: UITableView
    open let tableViewStyle: UITableViewStyle
    
    public init(style: UITableViewStyle) {
        tableViewStyle = style
        tableView = UITableView(frame: CGRect.zero, style: tableViewStyle)
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func loadView() {
        super.loadView()
        
        tableView.delegate = self
        tableView.dataSource = self
        view.addSubview(tableView)
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: UIBarButtonItemStyle.plain, target: nil, action: nil)
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        if let row = tableView.indexPathForSelectedRow {
            tableView.deselectRow(at: row, animated: animated)
        }
        
        super.viewWillAppear(animated)
    }
    
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds;
    }

    open override func setEditing(_ editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
    }
    
    fileprivate func placeholderViewFrame() -> CGRect {
        let navigationBarHeight: CGFloat = 64.5
        return CGRect(x: 0, y: navigationBarHeight, width: view.bounds.size.width, height: view.bounds.size.height - navigationBarHeight)
    }
    
    open func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    open func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 0
    }
    
    open func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        return UITableViewCell()
    }
    
}
