//
//  ComposeController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 23.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class ComposeController: ContactsBaseController, UISearchBarDelegate, UISearchDisplayDelegate {

    @IBOutlet weak var tableView: UITableView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: ContactsSource?
    
    override init() {
        super.init(nibName: "ComposeController", bundle: nil)
        
        self.navigationItem.title = "New Message";
        self.extendedLayoutIncludesOpaqueBars = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        
        bindTable(tableView)
        
        searchView = UISearchBar()
        searchView!.searchBarStyle = UISearchBarStyle.Default
        searchView!.barStyle = UIBarStyle.Default
        searchView!.translucent = false
        
        searchView?.setSearchFieldBackgroundImage(Imaging.imageWithColor(Resources.SearchBgColor, size: CGSize(width: 1, height: 28)), forState: UIControlState.Normal)

        // Enabled color
        searchView!.barTintColor = UIColor.whiteColor()
        
        // Disabled color
        searchView!.backgroundImage = Imaging.imageWithColor(UIColor.whiteColor(), size: CGSize(width: 1, height: 1))
        searchView!.backgroundColor = UIColor.whiteColor()
        
        // Enabled Cancel button color
        searchView!.tintColor = Resources.TintColor
        
        searchView!.placeholder = "";
        searchView!.delegate = self
        searchView!.frame = CGRectMake(0, 0, 0, 44)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 56
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        tableView.tableHeaderView = searchView
        
        searchSource = ContactsSource(searchDisplay: searchDisplay!)

        super.viewDidLoad()
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if (tableView == self.tableView) {
            var contact = objectAtIndexPath(indexPath) as! AMContact
            var controllers = NSMutableArray(array: navigationController!.viewControllers)
            controllers.removeLastObject()
            controllers.addObject(MessagesViewController(peer: AMPeer.userWithInt(contact.getUid())))
            navigationController!.setViewControllers(controllers as [AnyObject], animated: true)
        } else {
            var contact = searchSource!.objectAtIndexPath(indexPath) as! AMContact
            var controllers = NSMutableArray(array: navigationController!.viewControllers)
            controllers.removeLastObject()
            controllers.addObject(MessagesViewController(peer: AMPeer.userWithInt(contact.getUid())))
            navigationController!.setViewControllers(controllers as [AnyObject], animated: true)
        }
    }

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.sharedApplication().setStatusBarStyle(UIStatusBarStyle.LightContent, animated: true)
    }
}