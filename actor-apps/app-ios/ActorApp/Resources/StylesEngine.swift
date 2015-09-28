//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

private var styles = [String: Style]()

class Style {
    
    // Colors
    
    var foregroundColor: UIColor?
    var backgroundColor: UIColor?
    var selectedColor: UIColor?
    var tintColor: UIColor?
    var onTintColor: UIColor?
    
    // Font
    
    var font: UIFont?
    
    // Alignment
    
    var contentMode: UIViewContentMode?
    var textAlignment: NSTextAlignment?
    
    // Cell
    
    var cellStyle: UITableViewCellStyle?
    var cellSeparatorsLeftInset: CGFloat?
    var cellTopSeparatorLeftInset: CGFloat?
    var cellBottomSeparatorLeftInset: CGFloat?
    var cellSeparatorsVisible: Bool?
    var cellTopSeparatorVisible: Bool?
    var cellBottomSeparatorVisible: Bool?
    
    // Avatar
    
    var avatarSize: Int?
    var avatarType: AvatarType?
    
    // Image
    
    var image: UIImage?
    
    // Title

    var title: String?
    
    init() {
        
    }
    
    init(base: Style) {
        self.foregroundColor = base.foregroundColor
        self.backgroundColor = base.backgroundColor
        self.selectedColor = base.selectedColor
        self.font = base.font
        self.contentMode = base.contentMode
        self.textAlignment = base.textAlignment
        
        self.cellStyle = base.cellStyle
        self.cellSeparatorsLeftInset = base.cellSeparatorsLeftInset
        self.cellTopSeparatorLeftInset = base.cellTopSeparatorLeftInset
        self.cellBottomSeparatorLeftInset = base.cellBottomSeparatorLeftInset
        self.cellSeparatorsVisible = base.cellSeparatorsVisible
        self.cellTopSeparatorVisible = base.cellTopSeparatorVisible
        self.cellBottomSeparatorVisible = base.cellBottomSeparatorVisible
        
        self.avatarSize = base.avatarSize
        self.avatarType = base.avatarType
        
        self.image = base.image
        self.title = base.title
    }
}

func pickStyle(key: String) -> Style? {
    return styles[key]
}

func registerStyle(key: String, closure: (s: Style) -> ()) {
    let style = Style()
    closure(s: style)
    styles[key] = style
}

func registerStyle(key: String, parent: String, closure: (s: Style) -> ()) {
    let style = Style(base: styles[parent]!)
    closure(s: style)
    styles[key] = style
}

func registerStyle(key: String, parent: String) {
    styles[key] = Style(base: styles[parent]!)
}

protocol UIViewStylable {
    func applyStyle(s: Style)
}

extension UIViewStylable {
    
    func applyStyle(style: String) {
        if let s = styles[style] {
            applyStyle(s)
        }
    }
    
    func applyStyles(styles: [String]) {
        for s in styles {
            applyStyle(s)
        }
    }
}

extension UILabel: UIViewStylable {
    
    func applyStyle(s: Style) {
        
        // Foreground color
        if let c = s.foregroundColor {
            self.textColor = c
        }
            
        // Background color
        if let c = s.backgroundColor {
            self.backgroundColor = c
        }
            
        // Font
        if let f = s.font {
            self.font = f
        }
            
        // Content mode
        if let v = s.contentMode {
            self.contentMode = v
        }
            
        // Text alignment
        if let v = s.textAlignment {
            self.textAlignment = v
        }
    }
    
    convenience init(style: String) {
        self.init()
        self.applyStyle(style)
    }
 }

extension UITableViewCell: UIViewStylable {
    
    convenience init(reuseIdentifier: String) {
        self.init(_style: "cell", reuseIdentifier: reuseIdentifier)
    }
    
    convenience init(_style: String, reuseIdentifier: String) {
        if let s = styles[_style] {
            if let v = s.cellStyle {
                self.init(style: v, reuseIdentifier: reuseIdentifier)
            } else {
                self.init(style: .Default, reuseIdentifier: reuseIdentifier)
            }
        } else {
            self.init(style: .Default, reuseIdentifier: reuseIdentifier)
        }
        self.applyStyle(_style)
    }
    
    func applyStyle(s: Style) {
        if let v = s.selectedColor {
            let selectedView = UIView()
            selectedView.backgroundColor = v
            selectedBackgroundView = selectedView
        }
        
        if let v = s.backgroundColor {
            backgroundColor = v
        }
        
        if let ua = self as? UATableViewCell {
            
            if let v = s.cellSeparatorsVisible {
                ua.topSeparatorVisible = v
                ua.bottomSeparatorVisible = v
            }
            
            if let v = s.cellTopSeparatorVisible {
                ua.topSeparatorVisible = v
            }
            
            if let v = s.cellBottomSeparatorVisible {
                ua.bottomSeparatorVisible = v
            }
            
            if let v = s.cellSeparatorsLeftInset {
                ua.topSeparatorLeftInset = v
                ua.bottomSeparatorLeftInset = v
            }
            
            if let v = s.cellTopSeparatorLeftInset {
                ua.topSeparatorLeftInset = v
            }
            
            if let v = s.cellBottomSeparatorLeftInset {
                ua.bottomSeparatorLeftInset = v
            }
        }
    }
}

extension UIImageView: UIViewStylable {
    
    convenience init(style: String) {
        self.init()
        applyStyle(style)
    }
    
    func applyStyle(s: Style) {
        
        // Background color
        if let v = s.backgroundColor {
            self.backgroundColor = v
        }
        
        // Tint color
        if let v = s.tintColor {
            self.tintColor = v
        }
        
        // Image
        if let v = s.image {
            self.image = v
        }
        
        // Content Mode
        if let v = s.contentMode {
            self.contentMode = v
        }
    }
}

extension AvatarView {

    convenience init(style: String) {
        if let s = styles[style] {
            if let size = s.avatarSize, let type = s.avatarType {
                self.init(frameSize: size, type: type)
            } else {
                fatalError("Unknown style \(style)")
            }
        } else {
            fatalError("Unknown style \(style)")
        }
    }
}

extension UIImage {
    func styled(style: String) -> UIImage {
        let style = pickStyle(style)!
        var res = self
        if let v = style.foregroundColor {
            res = res.tintImage(v).imageWithRenderingMode(UIImageRenderingMode.AlwaysOriginal)
        }
        return res
    }
}

extension UIColor {
    class func style(style: String) -> UIColor {
        return pickStyle(style)!.foregroundColor!
    }
}

extension UIViewController: UIViewStylable {
    func applyStyle(s: Style) {
        if let v = s.title {
            self.navigationItem.title = v
        }
        if let v = s.backgroundColor {
            self.view.backgroundColor = v
        }
    }
}