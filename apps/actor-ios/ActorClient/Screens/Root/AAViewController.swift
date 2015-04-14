//
//  AAViewController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AAViewController: UIViewController {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholder = AAPlaceholderView()
    
    // MARK: -
    // MARK: Constructors
    
    init() {
        super.init(nibName: nil, bundle: nil)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: NSBundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.backBarButtonItem = UIBarButtonItem(title: NSLocalizedString("NavigationBack",comment: "Back button"), style: UIBarButtonItemStyle.Plain, target: nil, action: nil)
    }
    
    // MARK: -
    // MARK: Placeholder
    
    func showPlaceholder() {
        if placeholder.superview == nil {
            placeholder.frame = view.bounds
            view.addSubview(placeholder)
        }
    }
    
    func hidePlaceholder() {
        if placeholder.superview != nil {
            placeholder.removeFromSuperview()
        }
    }
    
    // MARK: -
    // MARK: Layout
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        placeholder.frame = view.bounds
    }
    
    // MARK: -
    // MARK: Navigation
    
    func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }

}
