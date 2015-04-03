//
//  AATableViewCell.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/2/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

enum AATableViewCellStyle {
    case Normal
    case Destructive
    case Switch
    case Blue
}

class AATableViewCell: UITableViewCell {
    
    // MARK: -
    // MARK: Private vars
    
    private var switcher: UISwitch?
    
    // MARK: -
    // MARK: Public vars
    
    var style: AATableViewCellStyle = AATableViewCellStyle.Normal {
        didSet {
            updateCellStyle()
        }
    }
    
    var switchBlock: ((Bool) -> ())?
    
    // MARK: -
    // MARK: Constructors
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        textLabel!.font = UIFont.systemFontOfSize(17.0)
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: -
    // MARK: Methods
    
    private func updateCellStyle() {
        switch (style) {
        case .Normal:
            textLabel!.textColor = UIColor.blackColor()
            switcher?.hidden = true
            break
        case .Destructive:
            textLabel!.textColor = UIColor.redColor()
            textLabel!.textAlignment = NSTextAlignment.Center
            switcher?.hidden = true
            break
        case .Switch:
            textLabel!.textColor = UIColor.blackColor()
            setupSwitchIfNeeded()
            switcher?.hidden = false
            break
        case .Blue:
            textLabel!.textColor = UIColor.RGB(0x007ee5)
            textLabel!.textAlignment = NSTextAlignment.Left
            switcher?.hidden = true
            break
        default:
            break
        }
    }
    
    private func setupSwitchIfNeeded() {
        if switcher == nil {
            switcher = UISwitch()
            switcher!.addTarget(self, action: Selector("switcherSwitched"), forControlEvents: UIControlEvents.ValueChanged)
            contentView.addSubview(switcher!)
        }
    }
    
    // MARK: -
    // MARK: Methods
    
    func switcherSwitched() {
        if switchBlock != nil {
            switchBlock!(switcher!.on)
        }
    }
    
    // MARK: -
    // MARK: Setters
    
    func setTitle(title: String) {
        textLabel!.text = title
    }
    
    func setLeftInset(leftInset: CGFloat) {
        separatorInset = UIEdgeInsets(top: 0.0, left: leftInset, bottom: 0.0, right: 0.0)
    }
    
    func setSwitcherOn(on: Bool) {
        switcher?.setOn(on, animated: false)
    }
    
    // MARK: -
    // MARK: Layout
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        if switcher != nil {
            let switcherSize = switcher!.bounds.size
            switcher!.frame = CGRect(x: contentView.bounds.size.width - switcherSize.width - 15, y: (contentView.bounds.size.height - switcherSize.height) / 2, width: switcherSize.width, height: switcherSize.height)
        }
    }

}
