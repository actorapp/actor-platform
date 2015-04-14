//
//  DialogsViewController.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 10.03.15.
//  Copyright (c) 2015 Anton Bukov. All rights reserved.
//

import UIKit

class DialogsViewController: EngineListController, UISearchBarDelegate, UISearchDisplayDelegate {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var loadingView: UIView!
    
    var searchView: UISearchBar?
    var searchDisplay: UISearchDisplayController?
    var searchSource: AADialogsListSearchSource?
    
    var binder = Binder()
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder);
        
        initCommon();
    }
    
    override init() {
        super.init(nibName: "DialogsViewController", bundle: nil)
        
        initCommon(); 
    }
    
    func initCommon(){
        
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
    }
    
    override func buildDisplayList() -> AMBindedDisplayList {
        return MSG.getDialogsGlobalList()
    }
    
    func isTableEditing() -> Bool {
        return self.tableView.editing;
    }
    
    override func viewDidLoad() {
        
        tableView.backgroundColor = Resources.BackyardColor
        
        // Footer
        var footer = UIView(frame: CGRectMake(0, 0, 320, 80));
        
        var footerHint = UILabel(frame: CGRectMake(0, 0, 320, 60));
        footerHint.textAlignment = NSTextAlignment.Center;
        footerHint.font = UIFont.systemFontOfSize(16);
        footerHint.textColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
        footerHint.text = "Swipe for more options";
        footer.addSubview(footerHint);
        
        var shadow = UIImageView(image: UIImage(named: "CardBottom2"));
        shadow.frame = CGRectMake(0, 0, 320, 4);
        shadow.contentMode = UIViewContentMode.ScaleToFill;
        footer.addSubview(shadow);
        
        self.tableView.tableFooterView = footer;
        
        var header = UIView(frame: CGRectMake(0, 0, 320, 0))
        
        var headerShadow = UIImageView(frame: CGRectMake(0, -4, 320, 4));
        headerShadow.image = UIImage(named: "CardTop2");
        headerShadow.contentMode = UIViewContentMode.ScaleToFill;
        header.addSubview(headerShadow);
        
        self.tableView.tableHeaderView = header;
        
        loadingView.hidden = true;
        
        bindTable(tableView);
        
        searchView = UISearchBar()
        searchView!.delegate = self
        searchView!.frame = CGRectMake(0, 0, 0, 44)
        
        MainAppTheme.search.styleSearchBar(searchView!)
        
        searchDisplay = UISearchDisplayController(searchBar: searchView, contentsController: self)
        searchDisplay?.searchResultsDelegate = self
        searchDisplay?.searchResultsTableView.rowHeight = 76
        searchDisplay?.searchResultsTableView.separatorStyle = UITableViewCellSeparatorStyle.None
        searchDisplay?.searchResultsTableView.backgroundColor = Resources.BackyardColor
        searchDisplay?.searchResultsTableView.frame = tableView.frame
        
        tableView.tableHeaderView = searchView
        
        searchSource = AADialogsListSearchSource(searchDisplay: searchDisplay!)
        
        super.viewDidLoad();
        
        navigationItem.title = NSLocalizedString("TabMessages", comment: "Messages Title")
        navigationItem.leftBarButtonItem = editButtonItem()
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "navigateToCompose")
        
        placeholder.setImage(nil, title: "Empty", subtitle: "Your dialog list is empty. You can start chat by pressing top right button.")
        binder.bind(MSG.getAppState().getIsDialogsEmpty(), closure: { (value: Any?) -> () in
            if let empty = value as? JavaLangBoolean {
                if Bool(empty.booleanValue()) == true {
                    self.showPlaceholder()
                } else {
                    self.hidePlaceholder()
                }
            }
        })
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        
        MSG.onDialogsOpen();
    }
    
    // MARK: -
    // MARK: Setters
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
        
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
            
            execute(MSG.deleteChatWithAMPeer(dialog.getPeer()));
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
    
    override func viewDidDisappear(animated: Bool) {
        MSG.onDialogsClosed();
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        if (searchDisplay != nil && searchDisplay!.active) {
            MainAppTheme.search.applyStatusBar()
        } else {
            MainAppTheme.navigation.applyStatusBar()
        }
    }
    
    // MARK: -
    // MARK: Navigation
    
    func navigateToCompose() {
        let composeController = ComposeController()
        composeController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(composeController, animated: true)
    }
    
    private func navigateToMessagesWithPeer(peer: AMPeer) {
        let conversationController = AAConversationController(peer: peer)
        conversationController.hidesBottomBarWhenPushed = true
        navigationController?.pushViewController(conversationController, animated: true);
        MainAppTheme.navigation.applyStatusBar()
    }
    
}
