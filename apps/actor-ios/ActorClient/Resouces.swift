//
//  Resouces.swift
//  ActorClient
//
//  Created by Stepan Korshakov on 11.03.15.
//  Copyright (c) 2015 Actor LLC. All rights reserved.
//

import Foundation

 @objc class Resources {
    
    init(){
        fatalError("Unable to instantinate Resources");
    }
    
    private static var _iconCheck1:UIImage? = nil;
    private static var _iconCheck2:UIImage? = nil;
    private static var _iconError:UIImage? = nil;
    private static var _iconWarring:UIImage? = nil;
    private static var _iconClock:UIImage? = nil;
    
    static var iconCheck1:UIImage {
        get {
            if (_iconCheck1 == nil){
                _iconCheck1 = UIImage(named: "msg_check_1")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconCheck1!;
        }
    }
    
    static var iconCheck2:UIImage {
        get {
            if (_iconCheck2 == nil){
                _iconCheck2 = UIImage(named: "msg_check_2")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconCheck2!;
        }
    }

    static var iconError:UIImage {
        get {
            if (_iconError == nil){
                _iconError = UIImage(named: "msg_error")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconError!;
        }
    }
    
    static var iconWarring:UIImage {
        get {
            if (_iconWarring == nil){
                _iconWarring = UIImage(named: "msg_warring")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconWarring!;
        }
    }

    static var iconClock:UIImage {
        get {
            if (_iconClock == nil){
                _iconClock = UIImage(named: "msg_clock")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconClock!;
        }
    }
    
    static let TintDarkColor = UIColor(red: 38/255.0, green: 109/255.0, blue: 204/255.0, alpha: 1.0);
    static let TintColor = UIColor(red: 80/255.0, green: 133/255.0, blue: 204/255.0, alpha: 1.0);
    static let BarTintColor = TintColor;
    static let BarTintUnselectedColor = UIColor(red: 171/255.0, green: 182/255.0, blue: 202/255.0, alpha: 1);
    static let SearchBgColor = UIColor(red: 217/255.0, green: 218/255.0, blue: 220/255.0, alpha: 1)
    
    static let placeHolderColors : [UIColor] = [
        UIColor(red: 0x59/255.0, green: 0xa2/255.0, blue: 0xbe/255.0, alpha: 1),
        UIColor(red: 0x20/255.0, green: 0x93/255.0, blue: 0xcd/255.0, alpha: 1),
        UIColor(red: 0xad/255.0, green: 0x62/255.0, blue: 0xa7/255.0, alpha: 1),
        UIColor(red: 0xf1/255.0, green: 0x63/255.0, blue: 0x64/255.0, alpha: 1),
        UIColor(red: 0xf9/255.0, green: 0xa4/255.0, blue: 0x3e/255.0, alpha: 1),
        UIColor(red: 0xe4/255.0, green: 0xc6/255.0, blue: 0x2e/255.0, alpha: 1),
        UIColor(red: 0x67/255.0, green: 0xbf/255.0, blue: 0x74/255.0, alpha: 1)];
    
    static let TextPrimaryColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0);
    static let TextSecondaryColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0);
    static let SeparatorColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0x1e/255.0)
    
    static let BackyardColor = UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1)
    static let SecondaryTint = UIColor(red: 0xb5/255.0, green: 0xb6/255.0, blue: 0xb7/255.0, alpha: 1)
    
    static let SecondaryLightText = UIColor(red: 1, green: 1, blue: 1, alpha: 128/255.0)
    static let PrimaryLightText = UIColor(red: 1, green: 1, blue: 1, alpha: 1)
    
    static let SelectorColor = UIColor(red: 0, green: 0, blue: 0, alpha: 60/255.0)
}





