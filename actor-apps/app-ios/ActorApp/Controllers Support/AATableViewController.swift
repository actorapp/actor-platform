//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class AATableViewController: AAViewController, UITableViewDataSource, UITableViewDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    var tableView: UITableView!
    var tableViewStyle: UITableViewStyle!
    
    // MARK: - 
    // MARK: Constructors
    
    init(style: UITableViewStyle) {
        super.init()
        
        tableViewStyle = style
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment:"Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    override func loadView() {
        super.loadView()
        
        // view.backgroundColor = UIColor.whiteColor()
        
        tableView = UITableView(frame: view.bounds, style: tableViewStyle)
        tableView.delegate = self
        tableView.dataSource = self
        view.addSubview(tableView)
    }

    // MARK: -
    // MARK: Methods
    
    func showPlaceholderWithImage(image: UIImage?, title: String?, subtitle: String?) {
        placeholder.setImage(image, title: title, subtitle: subtitle)
        super.showPlaceholder()
    }
    
    // MARK: -
    // MARK: Setters
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
    }
    
    // MARK: -
    // MARK: Getters
    
    private func placeholderViewFrame() -> CGRect {
        let navigationBarHeight: CGFloat = 64.0 + Utils.retinaPixel() // TODO: if will be landscape then change to manual calc
        return CGRect(x: 0, y: navigationBarHeight, width: view.bounds.size.width, height: view.bounds.size.height - navigationBarHeight)
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        tableView.frame = view.bounds;
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