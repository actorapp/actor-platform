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
    
    var placeholder = AAPlaceholderView(topOffset: 0)
    
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
    // MARK: Methods
    
    func shakeView(view: UIView, originalX: CGFloat) {
        var r = view.frame
        r.origin.x = originalX
        var originalFrame = r
        var rFirst = r
        rFirst.origin.x = r.origin.x + 4
        r.origin.x = r.origin.x - 4
        
        UIView.animateWithDuration(0.05, delay: 0.0, options: UIViewAnimationOptions.Autoreverse, animations: { () -> Void in
            view.frame = rFirst
            }) { (finished) -> Void in
                if (finished) {
                    UIView.animateWithDuration(0.05, delay: 0.0, options: (UIViewAnimationOptions.Repeat | UIViewAnimationOptions.Autoreverse), animations: { () -> Void in
                        UIView.setAnimationRepeatCount(3)
                        view.frame = r
                        }, completion: { (finished) -> Void in
                            view.frame = originalFrame
                    })
                } else {
                    view.frame = originalFrame
                }
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
