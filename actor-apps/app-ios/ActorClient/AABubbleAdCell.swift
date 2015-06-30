//
//  AAAdCell.swift
//  ActorApp
//
//  Created by Stepan Korshakov on 25.05.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

class AABubbleAdCell: AABubbleCell {
    
    let bgView = UIView()
    
    init(frame: CGRect) {
        super.init(frame: frame, isFullSize: true)
        
        bgView.backgroundColor = UIColor.whiteColor()
        
        mainView.addSubview(bgView)
        
        mainView.backgroundColor = UIColor.clearColor()
        
        mainView.userInteractionEnabled = true
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func bind(message: AMMessage, reuse: Bool, cellLayout: CellLayout, setting: CellSetting) {
        var content = message.getContent() as! AMBannerContent
        
    }
    
    override func layoutContent(maxWidth: CGFloat, offsetX: CGFloat) {
        bgView.frame = contentView.bounds
    }
}