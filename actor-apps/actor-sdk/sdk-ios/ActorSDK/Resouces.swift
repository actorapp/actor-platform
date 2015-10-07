//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class Resources {
    
    init(){
        fatalError("Unable to instantinate Resources");
    }
    
    private static var _iconCheck1:UIImage? = nil;
    private static var _iconCheck2:UIImage? = nil;
    private static var _iconError:UIImage? = nil;
    private static var _iconWarring:UIImage? = nil;
    private static var _iconClock:UIImage? = nil;
    
    public static var iconCheck1:UIImage {
        get {
            if (_iconCheck1 == nil){
                _iconCheck1 = UIImage(named: "msg_check_1")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconCheck1!;
        }
    }
    
    public static var iconCheck2:UIImage {
        get {
            if (_iconCheck2 == nil){
                _iconCheck2 = UIImage(named: "msg_check_2")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconCheck2!;
        }
    }

    public static var iconError:UIImage {
        get {
            if (_iconError == nil){
                _iconError = UIImage(named: "msg_error")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconError!;
        }
    }
    
    public static var iconWarring:UIImage {
        get {
            if (_iconWarring == nil){
                _iconWarring = UIImage(named: "msg_warring")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconWarring!;
        }
    }

    public static var iconClock:UIImage {
        get {
            if (_iconClock == nil){
                _iconClock = UIImage(named: "msg_clock")?
                    .imageWithRenderingMode(UIImageRenderingMode.AlwaysTemplate);
            }
            return _iconClock!;
        }
    }

    // static let PrimaryColor = UIColor.RGB(0x114669)
    public static let IsDarkTheme = true
    public static let PrimaryColor = UIColor(red: 0, green: 36/255.0, blue: 77/255.0, alpha: 1.0)
    
    public static let TintDarkColor = UIColor(red: 38/255.0, green: 109/255.0, blue: 204/255.0, alpha: 1.0);
    public static let TintColor = UIColor(red: 80/255.0, green: 133/255.0, blue: 204/255.0, alpha: 1.0);
    
    public static let BarTintColor = TintColor;
    public static let BarTintUnselectedColor = UIColor.RGB(0x5085CB, alpha: 0.56);
    
    public static let SearchBgColor = UIColor(red: 217/255.0, green: 218/255.0, blue: 220/255.0, alpha: 1)
    
    public static let placeHolderColors : [UIColor] = [
        UIColor.RGB(0x59b7d3),
        UIColor.RGB(0x1d4e6f),
        UIColor.RGB(0x995794),
        UIColor.RGB(0xff506c),
        UIColor.RGB(0xf99341),
        UIColor.RGB(0xe4d027),
        UIColor.RGB(0x87c743)];
    
    public static let TextPrimaryColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0xDE/255.0);
    public static let TextSecondaryColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0x8A/255.0);
    
    public static let BackyardColor = UIColor(red: 238/255.0, green: 238/255.0, blue: 238/255.0, alpha: 1)
    public static let SecondaryTint = UIColor(red: 0xb5/255.0, green: 0xb6/255.0, blue: 0xb7/255.0, alpha: 1)
    
    public static let SecondaryLightText = UIColor(red: 0.9, green: 0.9, blue: 0.9, alpha: 1)
    public static let PrimaryLightText = UIColor(red: 1, green: 1, blue: 1, alpha: 1)
    
    public static let SelectorColor = UIColor(red: 0, green: 0, blue: 0, alpha: 60/255.0)
    
    public static let PrimaryDarkText = UIColor(red: 0, green: 0, blue: 0, alpha: 0.9)
    public static let SecondaryDarkText = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)
    
    public static let PlaceholderText = UIColor(red: 200/255.0, green: 200/255.0, blue: 200/255.0, alpha: 1.0)
    
    public static let HintText = UIColor(red: 80/255.0, green: 80/255.0, blue: 80/255.0, alpha: 1.0)
}





