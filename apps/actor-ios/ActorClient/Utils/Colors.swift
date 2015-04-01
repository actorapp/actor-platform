//
//  Colors.swift
//  ActorClient
//
//  Created by Danil Gontovnik on 4/1/15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import UIKit

extension UIColor {
    
    class func RGB(rgbValue: UInt) -> UIColor {
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
   
}
