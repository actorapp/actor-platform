//
//  Copyright (c) 2015 Actor LLC. <https://actor.im>
//

import UIKit

class DialogsViewController: EngineListController, UISearchBarDelegate, UISearchDisplayDelegate {

    var tableView: UITableView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: AADialogsListSearchSource?
    
    var binder = Binder()
    
    init() {
        super.init(contentSection: 0)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        super.loadView()
        
        var title = "";
        if (MainAppTheme.tab.showText) {
            title = NSLocalizedString("TabMessages", comment: "Messages Title")
        }
        
        tabBarItem = UITabBarItem(title: title,
            image: MainAppTheme.tab.createUnselectedIcon("ic_chats_outline"),
            selectedImage: MainAppTheme.tab.createSelectedIcon("ic_chats_filled"))
        
        if (!MainAppTheme.tab.showText) {
            tabBarItem.imageInsets = UIEdgeInsetsMake(6, 0, -6, 0);
        }
        
        self.extendedLayoutIncludesOpaqueBars = true
        
        tableView = UITableView()
        tableView.separatorStyle = UITableViewCellSeparatorStyle.None
        tableView.rowHeight = 76
        tableView.backgroundColor = MainAppTheme.list.backyardColor
        view.addSubview(tableView)
        // view = tableView
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.getDialogsGlobalList()
    }
    
    func isTableEditing() -> Bool {
        return self.tableView.editing;
    }
    
    override func viewDidLoad() {
        
        // Footer
        var footer = AATableViewHeader(frame: CGRectMake(0, 0, 320, 80));
        
        var footerHint = UILabel(frame: CGRectMake(0, 0, 320, 60));
        footerHint.textAlignment = NSTextAlignment.Center;
        footerHint.font = UIFont.systemFontOfSize(16);
        footerHint.textColor = MainAppTheme.list.hintColor
        footerHint.text = NSLocalizedString("DialogsHint", comment: "Swipe hint")
        footer.addSubview(footerHint);
        
        var shadow = UIImageView(image: UIImage(named: "CardBottom2"));
        shadow.frame = CGRectMake(0, 0, 320, 4);
        shadow.contentMode = UIViewContentMode.ScaleToFill;
        footer.addSubview(shadow);
        
        self.tableView.tableFooterView = footer;
        
        bindTable(tableView, fade: true);
        
        searchView = UISearchBar()
        searchView!.delegate = self
        searchView!.frame = CGRectMake(0, 0, 320, 44)
        searchView!.keyboardAppearance = MainAppTheme.common.isDarkKeyboard ? UIKeyboardAppearance.Dark : UIKeyboardAppearance.Light
        
        MainAppTheme.search.styleSearchBar(searchView!)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 76
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        var header = AATableViewHeader(frame: CGRectMake(0, 0, 320, 44))
        header.addSubview(searchDisplay!.searchBar)

//        var headerShadow = UIImageView(frame: CGRectMake(0, -4, 320, 4));
//        headerShadow.image = UIImage(named: "CardTop2");
//        headerShadow.contentMode = UIViewContentMode.ScaleToFill;
//        header.addSubview(headerShadow);
        
        tableView.tableHeaderView = header
        
        searchSource = AADialogsListSearchSource(searchDisplay: searchDisplay!)
        
        super.viewDidLoad();
        
        navigationItem.title = NSLocalizedString("TabMessages", comment: "Messages Title")
        navigationItem.leftBarButtonItem = editButtonItem()
        navigationItem.leftBarButtonItem!.title = NSLocalizedString("NavigationEdit", comment: "Edit Title");
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "navigateToCompose")
        
        placeholder.setImage(
            UIImage(named: "chat_list_placeholder"),
            title: NSLocalizedString("Placeholder_Dialogs_Title", comment: "Placeholder Title"),
            subtitle: NSLocalizedString("Placeholder_Dialogs_Message", comment: "Placeholder Message"))
        
        binder.bind(MSG.getAppState().getIsDialogsEmpty(), closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.navigationItem.leftBarButtonItem = nil
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                    self.navigationItem.leftBarButtonItem = self.editButtonItem()
                }
            }
        })
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        MSG.onDialogsOpen();
    }
    
    
    override func viewDidDisappear(animated: Bool) {
        MSG.onDialogsClosed();
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        // SearchBar hack
        var searchBar = searchDisplay!.searchBar
        var superView = searchBar.superview
        if !(superView is UITableView) {
            searchBar.removeFromSuperview()
            superView?.addSubview(searchBar)
        }
        
        // Header hack
        tableView.tableHeaderView?.setNeedsLayout()
        tableView.tableFooterView?.setNeedsLayout()
        
        // tableView.frame = CGRectMake(0, 0, view.frame.width, view.frame.height)
        
        if (searchDisplay != nil && searchDisplay!.active) {
            MainAppTheme.search.applyStatusBar()
        } else {
            MainAppTheme.navigation.applyStatusBar()
        }
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        tableView.frame = CGRectMake(0, 0, view.frame.width, view.frame.height)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        searchDisplay?.setActive(false, animated: animated)
    }
    
    // MARK: -
    // MARK: Setters
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
        
        if (editing) {
            self.navigationItem.leftBarButtonItem!.title = NSLocalizedString("NavigationDone", comment: "Done Title");
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Done;
            
            navigationItem.rightBarButtonItem = nil
        }
        else {
            self.navigationItem.leftBarButtonItem!.title = NSLocalizedString("NavigationEdit", comment: "Edit Title");
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Bordered;
            
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "navigateToCompose")
        }
        
        if editing == true {
            navigationItem.rightBarButtonItem = nil
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "navigateToCompose")
        }
    }
    
    // MARK: -
    // MARK: UITableView
    
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if (editingStyle == UITableViewCellEditingStyle.Delete) {
            var dialog = objectAtIndexPath(indexPath) as! AMDialog
            
            execute(MSG.deleteChatCommandWithPeer(dialog.getPeer()));
        }
    }
    
    override func buildCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?) -> UITableViewCell {
        let reuseKey = "cell_dialog";
        
        var cell = tableView.dequeueReusableCellWithIdentifier(reuseKey) as! DialogCell?;
        
        if (cell == nil){
            cell = DialogCell(reuseIdentifier: reuseKey);
            cell?.awakeFromNib();
        }
        
        return cell!;
    }
    
    override func bindCell(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath, item: AnyObject?, cell: UITableViewCell) {
        var dialog = item as! AMDialog;
        let isLast = indexPath.row == tableView.numberOfRowsInSection(indexPath.section)-1;
        (cell as! DialogCell).bindDialog(dialog, isLast: isLast);
    }

    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if (tableView == self.tableView) {
            var dialog = objectAtIndexPath(indexPath) as! AMDialog
            navigateToMessagesWithPeer(dialog.getPeer())
        } else {
            var searchEntity = searchSource!.objectAtIndexPath(indexPath) as! AMSearchEntity
            navigateToMessagesWithPeer(searchEntity.getPeer())
        }
    }
    
    // MARK: -
    // MARK: Navigation
    
    func navigateToCompose() {
        navigateDetail(ComposeController())
    }
    
    private func navigateToMessagesWithPeer(peer: AMPeer) {
        navigateDetail(AAConversationController(peer: peer))
        MainAppTheme.navigation.applyStatusBar()
    }
    
}
