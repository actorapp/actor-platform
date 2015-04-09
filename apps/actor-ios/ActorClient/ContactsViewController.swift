//
//  ContactsViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class ContactsViewController: ContactsBaseController, UISearchBarDelegate, UISearchDisplayDelegate {
    
    // MARK: -
    // MARK: Public vars
    
    @IBOutlet var rootView: UIView!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var emptyView: UIView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: ContactsSource?
    
    var binder = Binder()
    
    // MARK: -
    // MARK: Constructors
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        initCommon();
    }
    
    override init() {
        super.init(nibName: "ContactsViewController", bundle: nil)
        initCommon();
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        bindTable(tableView);
        
        searchView = UISearchBar()
        searchView!.searchBarStyle = UISearchBarStyle.Default
        searchView!.barStyle = UIBarStyle.Default
        searchView!.translucent = false
        
        let image = UIImage(named: "SearchBarBg")!
        searchView?.setSearchFieldBackgroundImage(image.stretchableImageWithLeftCapWidth(7, topCapHeight: 0), forState: UIControlState.Normal)
        
        // Enabled color
        searchView!.barTintColor = UIColor.whiteColor()
        
        // Disabled color
        searchView!.backgroundImage = Imaging.imageWithColor(UIColor.whiteColor(), size: CGSize(width: 320, height: 44))
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
        
        emptyView.hidden = true;
    
        super.viewDidLoad();
        
        navigationItem.title = "People";
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Add, target: self, action: "doAddContact")
        
        placeholder.setImage(nil, title: "Empty", subtitle: "Your contact list is empty. You can add friends by pressing top right button.")
        binder.bind(MSG.getAppState().getIsContactsEmpty(), closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                }
            }
        })
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        var selected = tableView.indexPathForSelectedRow();
        if (selected != nil){
            tableView.deselectRowAtIndexPath(selected!, animated: animated);
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func initCommon(){
        var icon = UIImage(named: "ic_users_blue_24")!;
        tabBarItem = UITabBarItem(title: nil,
            image: icon.tintImage(Resources.BarTintUnselectedColor)
                .imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal),
            selectedImage: icon);
        tabBarItem.imageInsets=UIEdgeInsetsMake(6, 0, -6, 0);
    }
    
    func doAddContact() {
        var alertView = UIAlertView(title: "Add Contact", message: "Please, specify phone number", delegate: nil, cancelButtonTitle: "Cancel")
        alertView.alertViewStyle = UIAlertViewStyle.PlainTextInput
        alertView.show()
    }
    
    // MARK: -
    // MARK: UITableView Delegate
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        var contact: AMContact!;
        
        if (tableView == self.tableView) {
            contact = objectAtIndexPath(indexPath) as! AMContact
        } else {
            contact = searchSource!.objectAtIndexPath(indexPath) as! AMContact
        }
        
        navigateToMessagesWithUid(contact.getUid())
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToMessagesWithUid(uid: jint) {
        let messagesController = MessagesViewController(peer: AMPeer.userWithInt(uid))
        messagesController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(messagesController, animated: true);
    }
    
}
