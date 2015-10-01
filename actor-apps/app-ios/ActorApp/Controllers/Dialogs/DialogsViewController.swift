//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import UIKit

class DialogsViewController: RecentContentViewController, RecentContentViewControllerDelegate {

    override init() {
        
        super.init()
        
        // Enabling dialogs page tracking
        
        content = ACAllEvents_Main.RECENT()
        
        // Setting delegate
        
        self.delegate = self
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setting UITabBarItem
        
        tabBarItem = UITabBarItem(
            title: localized("TabMessages"),
            image: UIImage(named: "TabIconChats")?.styled("tab.icon"),
            selectedImage: UIImage(named: "TabIconChatsHighlighted")?.styled("tab.icon.selected"))
        
        binder.bind(Actor.getAppState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
            if value != nil {
                if value!.integerValue > 0 {
                    self.tabBarItem.badgeValue = "\(value!.integerValue)"
                } else {
                    self.tabBarItem.badgeValue = nil
                }
            } else {
                self.tabBarItem.badgeValue = nil
            }
        })
        
        // Setting navigation item
        
        navigationItem.title = localized("TabMessages")
        navigationItem.leftBarButtonItem = editButtonItem()
        navigationItem.leftBarButtonItem!.title = localized("NavigationEdit")
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "compose")
    }
    
    // Implemention of editing
    
    override func setEditing(editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
        
        if (editing) {
            self.navigationItem.leftBarButtonItem!.title = localized("NavigationDone")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Done
            
            navigationItem.rightBarButtonItem = nil
        }
        else {
            self.navigationItem.leftBarButtonItem!.title = localized("NavigationEdit")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.Plain
            
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "compose")
        }
        
        if editing == true {
            navigationItem.rightBarButtonItem = nil
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.Compose, target: self, action: "compose")
        }
    }
    
    func compose() {
        navigateNext(ComposeController())
    }
    
    // Tracking app state
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        Actor.onDialogsOpen()
    }
    
    override func viewDidDisappear(animated: Bool) {
        super.viewDidDisappear(animated)
        Actor.onDialogsClosed()
    }
    
    // Handling selections
    
    func recentsDidTap(controller: RecentContentViewController, dialog: ACDialog) -> Bool {
        navigateDetail(ConversationViewController(peer: dialog.peer))
        return false
    }
    
    func searchDidTap(controller: RecentContentViewController, entity: ACSearchEntity) {
        navigateDetail(ConversationViewController(peer: entity.peer))
    }
}
