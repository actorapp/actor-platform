//
//  EditTextController.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 05.08.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class EditTextController: AAViewController {
    
    private var textView =  UITextView()
    private var completition: (String) -> ()
    
    init(title: String, actionTitle: String, content: String, completition: (String) -> ()) {
        self.completition = completition
        
        super.init(nibName: nil, bundle: nil)
        
        self.navigationItem.title = title
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(title: actionTitle, style: UIBarButtonItemStyle.Done, target: self, action: "doSave")
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: localized("NavigationCancel"), style: UIBarButtonItemStyle.Plain, target: self, action: "doCancel")
        
        self.textView.text = content
        self.textView.font = UIFont.systemFontOfSize(18)
        
        self.view.backgroundColor = UIColor.whiteColor()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.addSubview(textView)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        
        self.textView.becomeFirstResponder()
    }
    
    func doSave() {
        self.completition(textView.text)
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    func doCancel() {
        self.dismissViewControllerAnimated(true, completion: nil)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        self.textView.frame = CGRectMake(7, 7, self.view.bounds.width - 14, self.view.bounds.height - 14)
    }
}


