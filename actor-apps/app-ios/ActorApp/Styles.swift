//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

func initStyles() {
    
    // Text root styles
    
    registerStyle("root.text") { (s) -> () in
        s.foregroundColor = UIColor.blackColor()
    }
    
    registerStyle("root.accent") { (s) -> () in
        s.foregroundColor = UIColor.RGB(0x5085CB)
    }
    
    registerStyle("label", parent: "root.text") { (s) -> () in
        s.foregroundColor = s.foregroundColor!.alpha(0xDE/255.0)
    }
    
    registerStyle("hint") { (s) -> () in
        s.foregroundColor = UIColor(red: 164/255.0, green: 164/255.0, blue: 164/255.0, alpha: 1)
    }
    
    // TabView
    
    registerStyle("tab.icon") { (s) -> () in
        s.foregroundColor = MainAppTheme.tab.unselectedIconColor
    }
    
    registerStyle("tab.icon.selected", parent: "root.accent") { (s) -> () in
        s.foregroundColor = MainAppTheme.tab.selectedIconColor
    }
    
    registerStyle("tab.text") { (s) -> () in
        s.foregroundColor = MainAppTheme.tab.unselectedTextColor
    }
    
    registerStyle("tab.text.selected", parent: "root.accent") { (s) -> () in
        s.foregroundColor = MainAppTheme.tab.selectedTextColor
    }
    
    // Cell style
    
    registerStyle("cell") { (s) -> () in
        s.backgroundColor = MainAppTheme.list.bgColor
        // s.selectedColor = MainAppTheme.list.bgSelectedColor
        s.cellTopSeparatorVisible = false
        s.cellBottomSeparatorVisible = true
        s.cellSeparatorsLeftInset = 15
    }
    
    registerStyle("cell.titled", parent: "cell")
    
    registerStyle("cell.titled.title", parent: "root.accent") { (s) -> () in
        s.font = UIFont.systemFontOfSize(14.0)
    }
    
    registerStyle("cell.titled.content") { (s) -> () in
        s.font = UIFont.systemFontOfSize(17.0)
    }
    
    // User online style
    
    registerStyle("user.online", parent: "root.accent")
    
    registerStyle("user.offline", parent: "hint")
    
    // Avatars
    
    registerStyle("avatar.round") { (s) -> () in
        s.avatarType = .Rounded
    }
    
    registerStyle("avatar.square") { (s) -> () in
        s.avatarType = .Square
    }
    
    registerStyle("avatar.round.small", parent: "avatar.round") { (s) -> () in
        s.avatarSize = 40
    }
    
    // Dialog list style
    
    registerStyle("dialogs.cell", parent: "cell") { (s) -> () in
        s.cellSeparatorsLeftInset = 75
    }
    
    registerStyle("dialogs.cell.last", parent: "cell") { (s) -> () in
        s.cellSeparatorsLeftInset = 0
    }
    
    registerStyle("dialogs.avatar", parent: "avatar.round") { (s) -> () in
        s.avatarSize = 48
    }
    
    registerStyle("dialogs.title", parent: "label") { (s) -> () in
        s.font = UIFont.mediumSystemFontOfSize(17)
        s.foregroundColor = MainAppTheme.list.dialogTitle
    }
    
    registerStyle("dialogs.date", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(14)
        s.foregroundColor = MainAppTheme.list.dialogDate
        s.textAlignment = .Right
    }
    
    registerStyle("dialogs.message", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(16)
        s.foregroundColor = MainAppTheme.list.dialogText
    }
    
    registerStyle("dialogs.message.highlight", parent: "dialogs.message") { (s) -> () in
        s.foregroundColor = MainAppTheme.list.dialogTitle
    }
    
    registerStyle("dialogs.counter", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(14)
        s.foregroundColor = MainAppTheme.list.unreadText
        s.textAlignment = .Center
    }
    
    registerStyle("dialogs.counter.bg") { (s) -> () in
        s.image = Imaging.imageWithColor(MainAppTheme.list.unreadBg, size: CGSizeMake(18, 18))
            .roundImage(18).resizableImageWithCapInsets(UIEdgeInsetsMake(9, 9, 9, 9))
    }
    
    registerStyle("dialogs.status") { (s) -> () in
        s.contentMode = UIViewContentMode.Center
    }
    
    // Members
    
    registerStyle("members.cell", parent: "cell") { (s) -> () in
        s.cellSeparatorsLeftInset = 75
    }
    
    registerStyle("members.name", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(18.0)
    }
    
    registerStyle("members.online", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(14.0)
    }
    
    registerStyle("members.admin", parent: "label") { (s) -> () in
        s.font = UIFont.systemFontOfSize(14.0)
        s.foregroundColor = UIColor.redColor()
    }

    // List
    registerStyle("list.label", parent: "label") { (s) -> () in
        
    }
    
    // Controllers
    
    registerStyle("controller.settings") { (s) -> () in
        s.backgroundColor = MainAppTheme.list.bgColor
        s.title = localized("TabSettings")
    }
}