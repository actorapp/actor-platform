//
//  ElegantPresentations.swift
//  TwitterPresentationController
//
//  Created by Kyle Bashour on 2/21/16.
//  Copyright © 2016 Kyle Bashour. All rights reserved.
//

import UIKit

/**
 *  Elegant modal presentations. Contains the function `controller` for creating presentation controllers.
 */
public struct ElegantPresentations {
    
    /**
     Initializes and returns an elegant presentation controller for transitioning between the specified view controllers.
     
     - parameter presentedViewController:  The view controller being presented modally.
     - parameter presentingViewController: The view controller whose content represents the starting point of the transition.
     - parameter options:                  An options set for customizing the appearance and behavior of the presentation.
     
     - returns: An initialized presentation controller object.
     */
    public static func controller(presentedViewController presented: UIViewController,
                                                          presentingViewController presenting: UIViewController,
                                                                                   options: Set<PresentationOption>) -> UIPresentationController
    {
        
        let options = PresentationOptions(options: options)
        
        return ElegantPresentationController(presentedViewController: presented, presentingViewController: presenting, options: options)
    }
}

// For storing the options set in the options array
struct PresentationOptions {
    var dimmingViewAlpha: CGFloat = 1
    var dimmingViewTapDismisses = false
    var presentingTransform = CGAffineTransform(scaleX: 0.93, y: 0.93)
    var presentedHeight: CGFloat = -1
    var presentedPercentHeight = 1.0
    var usePercentHeight = true
    
    init() { }
    
    init(options: Set<PresentationOption>) {
        for option in options {
            switch option {
            case .noDimmingView: dimmingViewAlpha = 0
            case .customDimmingViewAlpha(let alpha): dimmingViewAlpha = alpha
            case .dismissOnDimmingViewTap: dimmingViewTapDismisses = true
            case .presentingViewKeepsSize: presentingTransform = CGAffineTransform.identity
            case .presentedHeight(let height):
                usePercentHeight = false
                presentedHeight = height
            case .presentedPercentHeight(let percentHeight):
                usePercentHeight = true
                presentedPercentHeight = percentHeight
            case .customPresentingScale(let scale):
                presentingTransform = CGAffineTransform(scaleX: CGFloat(min(1, scale)), y: CGFloat(min(1, scale)))
            }
        }
        
        // They tried to do both — bad!
        if presentedHeight != -1 && usePercentHeight {
            NSLog("\n-------------------------\nElegant Presentation Warning:\nDO NOT set a height and a percent height! Only one will be respected.\n-------------------------")
        }
        
        if options.contains(.noDimmingView) &&  dimmingViewAlpha != 0 {
            NSLog("\n-------------------------\nElegant Presentation Warning:\nDO NOT set no dimming view and a custom dimming view alpha! Only one will be respected.\n-------------------------")
        }
    }
}

/**
 Options for customizing the presentation animations and behavior
 
 - NoDimmingView:           Do not dim the presenting view controller
 - DismissOnDimmingViewTap: Dismiss the presented view controller when the area outside its view is tapped
 - PresentingViewKeepsSize: Do not shrink the presenting view controller into the background
 - PresentedHeight:         Give the presenting view controller a fixed height (may not work well with rotation)
 - PresentedPercentHeight:  Give the presenting view controller a percent height of the presented view controller (should work well with rotation)
 */
public enum PresentationOption: Hashable {
    
    case noDimmingView
    case customDimmingViewAlpha(CGFloat)
    case dismissOnDimmingViewTap
    case presentingViewKeepsSize
    case presentedHeight(CGFloat)
    case presentedPercentHeight(Double)
    case customPresentingScale(Double)
    
    var description: String {
        switch self {
        case .noDimmingView:                        return "No dimming view"
        case .customDimmingViewAlpha(let alpha):    return "Custom dimming view alpha \(alpha)"
        case .dismissOnDimmingViewTap:              return "Dismiss on dimming view tap"
        case .presentingViewKeepsSize:              return "Presenting view keeps size"
        case .presentedHeight(let height):          return "Presented height \(height)"
        case .presentedPercentHeight(let percent):  return "Presented percent height \(percent)"
        case .customPresentingScale(let scale):     return "Custom presenting scale \(scale)"
        }
    }
    
    public var hashValue: Int {
        return description.hashValue
    }
}

public func ==(lhs: PresentationOption, rhs: PresentationOption) -> Bool {
    return lhs.hashValue == rhs.hashValue
}
