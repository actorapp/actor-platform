//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class ComposeController: ContactsBaseViewController, UISearchBarDelegate, UISearchDisplayDelegate {

    
    var searchView: UISearchBar!
    var searchDisplay: UISearchDisplayController!
    var searchSource: ContactsSearchSource!
    var tableView = UITableView()
    
    init() {
        super.init(contentSection: 1, nibName: nil, bundle: nil)
        self.navigationItem.title = NSLocalizedString("ComposeTitle", comment: "Compose Title")
        self.extendedLayoutIncludesOpaqueBars = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        view.backgroundColor = UIColor.whiteColor()
        view.addSubview(tableView)
        
        bindTable(tableView, fade: true)
        
        searchView = UISearchBar()
        searchView.delegate = self
        searchView.frame = CGRectMake(0, 0, 0, 44)
        searchView.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        
        MainAppTheme.search.styleSearchBar(searchView)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay.searchResultsDelegate = self
        searchDisplay.searchResultsTableView.rowHeight = 56
        searchDisplay.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay.searchResultsTableView.frame = tableView.frame
        
        let header = TableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(searchView)
        
        let headerShadow = UIImageView(frame: CGRectMake(0, -4, 320, 4));
        headerShadow.image = UIImage(named: "CardTop2");
        headerShadow.contentMode = UIViewContentMode.ScaleToFill;
        header.addSubview(headerShadow);
        
        tableView.tableHeaderView = header
 
        searchSource = ContactsSearchSource(searchDisplay: searchDisplay!)

        super.viewDidLoad()
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (section == 1) {
            return super.tableView(tableView, numberOfRowsInSection: section)
        } else {
            return 1
        }
    }
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if (indexPath.section == 1) {
            return super.tableView(tableView, cellForRowAtIndexPath: indexPath)
        } else {
            if (indexPath.row == 0) {
                let reuseId = "create_group";
                let res = ContactActionCell(reuseIdentifier: reuseId)
                res.bind("ic_add_user",
                    actionTitle: NSLocalizedString("CreateGroup", comment: "Create Group"),
                    isLast: false)
                return res
            } else {
                let reuseId = "find_public";
                let res = ContactActionCell(reuseIdentifier: reuseId)
                res.bind("ic_add_user",
                    actionTitle: NSLocalizedString("Join public group", comment: "Create Group"),
                    isLast: false)
                return res
            }
        }
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if (tableView == self.tableView) {
            if (indexPath.section == 0) {
                if (indexPath.row == 0) {
                    navigateNext(GroupCreateViewController(), removeCurrent: true)
                } else {
                    navigateNext(DiscoverViewController(), removeCurrent: true)
                }
                MainAppTheme.navigation.applyStatusBar()
            } else {
                let contact = objectAtIndexPath(indexPath) as! ACContact
                navigateToMessagesWithPeerId(contact.getUid())
            }
        } else {
            let contact = searchSource!.objectAtIndexPath(indexPath) as! ACContact
            navigateToMessagesWithPeerId(contact.getUid())
        }
    }
    
    // MARK: -
    // MARK: Navigation
    
    private func navigateToMessagesWithPeerId(peerId: jint) {
        navigateNext(ConversationViewController(peer: ACPeer.userWithInt(peerId)), removeCurrent: true)
        MainAppTheme.navigation.applyStatusBar()
    }
    
    func createGroup() {
        navigateNext(GroupCreateViewController(), removeCurrent: true)
        MainAppTheme.navigation.applyStatusBar()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        tableView.frame = CGRectMake(0, 0, view.frame.width, view.frame.height)
    }
}