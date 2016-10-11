import UIKit

open class AttributedLabel: UIView {
    public enum ContentAlignment: Int {
        case center
        case top
        case bottom
        case left
        case right
        case topLeft
        case topRight
        case bottomLeft
        case bottomRight
        
        func alignOffset(viewSize: CGSize, containerSize: CGSize) -> CGPoint {
            let xMargin = viewSize.width - containerSize.width
            let yMargin = viewSize.height - containerSize.height
            
            switch self {
            case .center:
                return CGPoint(x: max(xMargin / 2, 0), y: max(yMargin / 2, 0))
            case .top:
                return CGPoint(x: max(xMargin / 2, 0), y: 0)
            case .bottom:
                return CGPoint(x: max(xMargin / 2, 0), y: max(yMargin, 0))
            case .left:
                return CGPoint(x: 0, y: max(yMargin / 2, 0))
            case .right:
                return CGPoint(x: max(xMargin, 0), y: max(yMargin / 2, 0))
            case .topLeft:
                return CGPoint(x: 0, y: 0)
            case .topRight:
                return CGPoint(x: max(xMargin, 0), y: 0)
            case .bottomLeft:
                return CGPoint(x: 0, y: max(yMargin, 0))
            case .bottomRight:
                return CGPoint(x: max(xMargin, 0), y: max(yMargin, 0))
            }
        }
    }
    
    /// default is `0`.
    open var numberOfLines: Int = 0 {
        didSet { setNeedsDisplay() }
    }
    /// default is `Left`.
    open var contentAlignment: ContentAlignment = .left {
        didSet { setNeedsDisplay() }
    }
    /// `lineFragmentPadding` of `NSTextContainer`. default is `0`.
    open var padding: CGFloat = 0 {
        didSet { setNeedsDisplay() }
    }
    /// default is system font 17 plain.
    open var font = UIFont.systemFont(ofSize: 17) {
        didSet { setNeedsDisplay() }
    }
    /// default is `ByTruncatingTail`.
    open var lineBreakMode: NSLineBreakMode = .byTruncatingTail {
        didSet { setNeedsDisplay() }
    }
    /// default is nil (text draws black).
    open var textColor: UIColor? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    open var paragraphStyle: NSParagraphStyle? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    open var shadow: NSShadow? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    open var attributedText: NSAttributedString? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    open var text: String? {
        get {
            return attributedText?.string
        }
        set {
            if let value = newValue {
                attributedText = NSAttributedString(string: value)
            } else {
                attributedText = nil
            }
        }
    }
    
    fileprivate var mergedAttributedText: NSAttributedString? {
        if let attributedText = attributedText {
            return mergeAttributes(attributedText)
        }
        return nil
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        isOpaque = false
        contentMode = .redraw
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        isOpaque = false
        contentMode = .redraw
    }
    
    open override func setNeedsDisplay() {
        if Thread.isMainThread {
            super.setNeedsDisplay()
        }
    }
    
    open override func draw(_ rect: CGRect) {
        guard let attributedText = mergedAttributedText else {
            return
        }

        let container = textContainer(rect.size)
        let manager = layoutManager(container)
        
        let storage = NSTextStorage(attributedString: attributedText)
        storage.addLayoutManager(manager)
        
        let frame = manager.usedRect(for: container)
        let point = contentAlignment.alignOffset(viewSize: rect.size, containerSize: frame.integral.size)
        
        let glyphRange = manager.glyphRange(for: container)
        manager.drawBackground(forGlyphRange: glyphRange, at: point)
        manager.drawGlyphs(forGlyphRange: glyphRange, at: point)
    }
    
    open override func sizeThatFits(_ size: CGSize) -> CGSize {
        guard let attributedText = mergedAttributedText else {
            return super.sizeThatFits(size)
        }
        
        let container = textContainer(size)
        let manager = layoutManager(container)
        
        let storage = NSTextStorage(attributedString: attributedText)
        storage.addLayoutManager(manager)
        
        let frame = manager.usedRect(for: container)
        return frame.integral.size
    }
    
    open override func sizeToFit() {
        super.sizeToFit()
        
        frame.size = sizeThatFits(CGSize(width: bounds.width, height: CGFloat.greatestFiniteMagnitude))
    }
    
    fileprivate func textContainer(_ size: CGSize) -> NSTextContainer {
        let container = NSTextContainer(size: size)
        container.lineBreakMode = lineBreakMode
        container.lineFragmentPadding = padding
        container.maximumNumberOfLines = numberOfLines
        return container
    }
    
    fileprivate func layoutManager(_ container: NSTextContainer) -> NSLayoutManager {
        let layoutManager = NSLayoutManager()
        layoutManager.addTextContainer(container)
        return layoutManager
    }
    
    fileprivate func mergeAttributes(_ attributedText: NSAttributedString) -> NSAttributedString {
        let attrString = NSMutableAttributedString(attributedString: attributedText)
        
        addAttribute(attrString, attrName: NSFontAttributeName, attr: font)
        
        if let textColor = textColor {
            addAttribute(attrString, attrName: NSForegroundColorAttributeName, attr: textColor)
        }
        
        if let paragraphStyle = paragraphStyle {
            addAttribute(attrString, attrName: NSParagraphStyleAttributeName, attr: paragraphStyle)
        }
        
        if let shadow = shadow {
            addAttribute(attrString, attrName: NSShadowAttributeName, attr: shadow)
        }
        
        return attrString
    }
    
    fileprivate func addAttribute(_ attrString: NSMutableAttributedString, attrName: String, attr: AnyObject) {
        let range = NSRange(location: 0, length: attrString.length)
        attrString.enumerateAttribute(attrName, in: range, options: .reverse) { object, range, pointer in
            if object == nil {
                attrString.addAttributes([attrName: attr], range: range)
            }
        }
    }
}
