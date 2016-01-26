import UIKit

public class AttributedLabel: UIView {
    public enum ContentAlignment: Int {
        case Center
        case Top
        case Bottom
        case Left
        case Right
        case TopLeft
        case TopRight
        case BottomLeft
        case BottomRight
        
        func alignOffset(viewSize viewSize: CGSize, containerSize: CGSize) -> CGPoint {
            let xMargin = viewSize.width - containerSize.width
            let yMargin = viewSize.height - containerSize.height
            
            switch self {
            case Center:
                return CGPoint(x: max(xMargin / 2, 0), y: max(yMargin / 2, 0))
            case Top:
                return CGPoint(x: max(xMargin / 2, 0), y: 0)
            case Bottom:
                return CGPoint(x: max(xMargin / 2, 0), y: max(yMargin, 0))
            case Left:
                return CGPoint(x: 0, y: max(yMargin / 2, 0))
            case Right:
                return CGPoint(x: max(xMargin, 0), y: max(yMargin / 2, 0))
            case TopLeft:
                return CGPoint(x: 0, y: 0)
            case TopRight:
                return CGPoint(x: max(xMargin, 0), y: 0)
            case BottomLeft:
                return CGPoint(x: 0, y: max(yMargin, 0))
            case BottomRight:
                return CGPoint(x: max(xMargin, 0), y: max(yMargin, 0))
            }
        }
    }
    
    /// default is `0`.
    public var numberOfLines: Int = 0 {
        didSet { setNeedsDisplay() }
    }
    /// default is `Left`.
    public var contentAlignment: ContentAlignment = .Left {
        didSet { setNeedsDisplay() }
    }
    /// `lineFragmentPadding` of `NSTextContainer`. default is `0`.
    public var padding: CGFloat = 0 {
        didSet { setNeedsDisplay() }
    }
    /// default is system font 17 plain.
    public var font = UIFont.systemFontOfSize(17) {
        didSet { setNeedsDisplay() }
    }
    /// default is `ByTruncatingTail`.
    public var lineBreakMode: NSLineBreakMode = .ByTruncatingTail {
        didSet { setNeedsDisplay() }
    }
    /// default is nil (text draws black).
    public var textColor: UIColor? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    public var paragraphStyle: NSParagraphStyle? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    public var shadow: NSShadow? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    public var attributedText: NSAttributedString? {
        didSet { setNeedsDisplay() }
    }
    /// default is nil.
    public var text: String? {
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
    
    private var mergedAttributedText: NSAttributedString? {
        if let attributedText = attributedText {
            return mergeAttributes(attributedText)
        }
        return nil
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        opaque = false
        contentMode = .Redraw
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        opaque = false
        contentMode = .Redraw
    }
    
    public override func setNeedsDisplay() {
        if NSThread.isMainThread() {
            super.setNeedsDisplay()
        }
    }
    
    public override func drawRect(rect: CGRect) {
        guard let attributedText = mergedAttributedText else {
            return
        }

        let container = textContainer(rect.size)
        let manager = layoutManager(container)
        
        let storage = NSTextStorage(attributedString: attributedText)
        storage.addLayoutManager(manager)
        
        let frame = manager.usedRectForTextContainer(container)
        let point = contentAlignment.alignOffset(viewSize: rect.size, containerSize: CGRectIntegral(frame).size)
        
        let glyphRange = manager.glyphRangeForTextContainer(container)
        manager.drawBackgroundForGlyphRange(glyphRange, atPoint: point)
        manager.drawGlyphsForGlyphRange(glyphRange, atPoint: point)
    }
    
    public override func sizeThatFits(size: CGSize) -> CGSize {
        guard let attributedText = mergedAttributedText else {
            return super.sizeThatFits(size)
        }
        
        let container = textContainer(size)
        let manager = layoutManager(container)
        
        let storage = NSTextStorage(attributedString: attributedText)
        storage.addLayoutManager(manager)
        
        let frame = manager.usedRectForTextContainer(container)
        return CGRectIntegral(frame).size
    }
    
    public override func sizeToFit() {
        super.sizeToFit()
        
        frame.size = sizeThatFits(CGSize(width: bounds.width, height: CGFloat.max))
    }
    
    private func textContainer(size: CGSize) -> NSTextContainer {
        let container = NSTextContainer(size: size)
        container.lineBreakMode = lineBreakMode
        container.lineFragmentPadding = padding
        container.maximumNumberOfLines = numberOfLines
        return container
    }
    
    private func layoutManager(container: NSTextContainer) -> NSLayoutManager {
        let layoutManager = NSLayoutManager()
        layoutManager.addTextContainer(container)
        return layoutManager
    }
    
    private func mergeAttributes(attributedText: NSAttributedString) -> NSAttributedString {
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
    
    private func addAttribute(attrString: NSMutableAttributedString, attrName: String, attr: AnyObject) {
        let range = NSRange(location: 0, length: attrString.length)
        attrString.enumerateAttribute(attrName, inRange: range, options: .Reverse) { object, range, pointer in
            if object == nil {
                attrString.addAttributes([attrName: attr], range: range)
            }
        }
    }
}
