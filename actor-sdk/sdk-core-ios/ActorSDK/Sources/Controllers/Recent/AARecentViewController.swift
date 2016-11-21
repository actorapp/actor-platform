//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit

open class AARecentViewController: AADialogsListContentController, AADialogsListContentControllerDelegate {

    fileprivate var isBinded = true
    
    public override init() {
        
        super.init()
        
        // Enabling dialogs page tracking
        
        content = ACAllEvents_Main.recent()
        
        // Setting delegate
        
        self.delegate = self
        
        // Setting UITabBarItem
        
        tabBarItem = UITabBarItem(title: "TabMessages", img: "TabIconChats", selImage: "TabIconChatsHighlighted")
        
        // Setting navigation item
        
        navigationItem.title = AALocalized("TabMessages")
        navigationItem.leftBarButtonItem = editButtonItem
        navigationItem.leftBarButtonItem!.title = AALocalized("NavigationEdit")
        navigationItem.backBarButtonItem = UIBarButtonItem(title: AALocalized("DialogsBack"), style: UIBarButtonItemStyle.plain, target: nil, action: nil)
        navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.compose, target: self, action: #selector(AARecentViewController.compose))
        
        bindCounter()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // Implemention of editing
    
    open override func setEditing(_ editing: Bool, animated: Bool) {
        super.setEditing(editing, animated: animated)
        tableView.setEditing(editing, animated: animated)
        
        if (editing) {
            self.navigationItem.leftBarButtonItem!.title = AALocalized("NavigationDone")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.done
            
            navigationItem.rightBarButtonItem = nil
        } else {
            self.navigationItem.leftBarButtonItem!.title = AALocalized("NavigationEdit")
            self.navigationItem.leftBarButtonItem!.style = UIBarButtonItemStyle.plain
            
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.compose, target: self, action: #selector(AARecentViewController.compose))
        }
        
        if editing == true {
            navigationItem.rightBarButtonItem = nil
        } else {
            navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: UIBarButtonSystemItem.compose, target: self, action: #selector(AARecentViewController.compose))
        }
    }
    
    open func compose() {
        if AADevice.isiPad {
            self.presentElegantViewController(AANavigationController(rootViewController: AAComposeController()))
        } else {
            navigateNext(AAComposeController())
        }
    }
    
    // Tracking app state
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        bindCounter()
    }
    
    func bindCounter() {
        if !isBinded {
            isBinded = true
            binder.bind(Actor.getGlobalState().globalCounter, closure: { (value: JavaLangInteger?) -> () in
                if value != nil {
                    if value!.intValue > 0 {
                        self.tabBarItem.badgeValue = "\(value!.intValue)"
                    } else {
                        self.tabBarItem.badgeValue = nil
                    }
                } else {
                    self.tabBarItem.badgeValue = nil
                }
            })
            
        }

    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        isBinded = false
    }
    
    open override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        Actor.onDialogsOpen()
    }
    
    open override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        Actor.onDialogsClosed()
    }
    
    // Handling selections
    
    open func recentsDidTap(_ controller: AADialogsListContentController, dialog: ACDialog) -> Bool {
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(dialog.peer) {
            self.navigateDetail(customController)
        } else {
            self.navigateDetail(ConversationViewController(peer: dialog.peer))
        }
        return false
    }
    
    open func searchDidTap(_ controller: AADialogsListContentController, entity: ACSearchResult) {
        if let customController = ActorSDK.sharedActor().delegate.actorControllerForConversation(entity.peer) {
            self.navigateDetail(customController)
        } else {
            self.navigateDetail(ConversationViewController(peer: entity.peer))
        }
    }
}
