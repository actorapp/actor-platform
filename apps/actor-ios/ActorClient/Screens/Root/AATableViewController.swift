//
//  AATableViewController.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 3/31/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

class AATableViewController: UITableViewController {
    
    // MARK: -
    // MARK: Public vars
    
    var placeholderView: AAPlaceholderView?
    
    // MARK: - 
    // MARK: Constructors
    
    init() {
        super.init(style: UITableViewStyle.Plain)
    }
    
    override init(style: UITableViewStyle) {
        super.init(style: style)
    }

    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    // MARK: -

    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        adjustPlaceholderWhenDisappearIfNeeded()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        adjustPlaceholderWhenAppearIfNeeded()
    }

    // MARK: -
    // MARK: Methods
    
    func showPlaceholderWithImage(image: UIImage?, title: String?, subtitle: String?) {
        if self.placeholderView == nil {
            self.placeholderView = AAPlaceholderView()
        }
        
        self.placeholderView!.frame = placeholderViewFrame()
        self.placeholderView!.setImage(image, title: title, subtitle: subtitle)
        self.navigationController!.view.addSubview(self.placeholderView!)
    }
    
    func hidePlaceholder() {
        if self.placeholderView != nil {
            self.placeholderView!.removeFromSuperview()
        }
    }
    
    private func adjustPlaceholderWhenAppearIfNeeded() {
        if self.placeholderView != nil {
            self.placeholderView!.removeFromSuperview()
            self.placeholderView!.frame = placeholderViewFrame()
            self.navigationController!.view.addSubview(self.placeholderView!)
        }
    }
    
    private func adjustPlaceholderWhenDisappearIfNeeded() {
        if self.placeholderView != nil {
            self.placeholderView!.removeFromSuperview()
            self.placeholderView!.frame = placeholderViewFrameInTableView()
            self.tableView.addSubview(self.placeholderView!)
        }
    }
    
    // MARK: -
    // MARK: Getters
    
    private func placeholderViewFrame() -> CGRect {
        let navigationBarHeight: CGFloat = 64.0 + Utils.retinaPixel() // TODO: if will be landscape then change to manual calc
        println("\(self.navigationController) ... \(navigationController)")
        return CGRect(x: 0, y: navigationBarHeight, width: self.navigationController!.view.bounds.size.width, height: self.navigationController!.view.bounds.size.height - navigationBarHeight)
    }
    
    private func placeholderViewFrameInTableView() -> CGRect {
        let navigationBarHeight: CGFloat = 64.0 + Utils.retinaPixel() // TODO: if will be landscape then change to manual calc
        return CGRect(x: 0, y: navigationBarHeight + self.tableView.contentOffset.y, width: self.navigationController!.view.bounds.size.width, height: self.navigationController!.view.bounds.size.height - navigationBarHeight)
    }
    
    // MARK: -
    // MARK: Navigation
    
    func dismiss() {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
}
