//
//  AATableViewController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AATableViewController: AAViewController {
    
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
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.Plain, target: nil, action: nil) // TODO: Localize
    }
    
    override func loadView() {
        super.loadView()
        
        view.backgroundColor = UIColor.whiteColor()
        
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
    
}

// MARK: -
// MARK: UITableViewController

extension AATableViewController: UITableViewDataSource {
    
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

extension AATableViewController: UITableViewDelegate {
    
}