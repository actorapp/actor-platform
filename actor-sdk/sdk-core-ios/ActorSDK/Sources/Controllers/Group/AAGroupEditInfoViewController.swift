//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AAGroupEditInfoController: AAViewController {
    
    private let scrollView = UIScrollView()
    private let bgContainer = UIView()
    private let topSeparator = UIView()
    private let bottomSeparator = UIView()
    
    private let nameInput = UITextField()
    private let separator = UIView()
    
    public init(gid: Int) {
        super.init()
        self.gid = gid
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = appStyle.vcBackyardColor
        
        scrollView.alwaysBounceVertical = true
        
        separator.backgroundColor = appStyle.vcSeparatorColor
        topSeparator.backgroundColor = appStyle.vcSeparatorColor
        bottomSeparator.backgroundColor = appStyle.vcSeparatorColor
        
        bgContainer.backgroundColor = appStyle.vcBgColor
        
        scrollView.addSubview(bgContainer)
        bgContainer.addSubview(nameInput)
        bgContainer.addSubview(separator)
        bgContainer.addSubview(topSeparator)
        bgContainer.addSubview(bottomSeparator)
        view.addSubview(scrollView)
    }
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        
        scrollView.frame = CGRectMake(0, 0, view.width, view.height)
        nameInput.frame = CGRectMake(72, 22, view.width - 72 - 10, 44)
        separator.frame = CGRectMake(72, 66, view.width - 72, 0.5)
        bgContainer.frame = CGRectMake(0, 0, view.width, 144)
        topSeparator.frame = CGRectMake(0, 0, bgContainer.width, 0.5)
        bottomSeparator.frame = CGRectMake(0, bgContainer.height - 0.5, bgContainer.width, 0.5)
    }
}