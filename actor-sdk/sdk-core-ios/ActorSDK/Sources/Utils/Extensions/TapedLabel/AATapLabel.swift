//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class TapLabel: UILabel, NSLayoutManagerDelegate {
    
    public static let LinkContentName = "TapLabelLinkContentName"
    public static let SelectedForegroudColorName = "TapLabelSelectedForegroudColorName"
    
    public weak var delegate: AATapLabelDelegate?
    
    private let layoutManager = NSLayoutManager()
    private let textContainer = NSTextContainer()
    private let textStorage = NSTextStorage()
    private var rangesForUrls = [NSRange]()
    private var links = [String: NSRange]()
    private var isTouchMoved = false
    private var defaultSelectedForegroundColor: UIColor?
    
    private var selected: (String, NSRange)? {
        didSet {
            if let (_, range) = selected
            {
                if let currentColor = textStorage.attribute(NSForegroundColorAttributeName,
                    atIndex: range.location,
                    effectiveRange: nil) as? UIColor
                {
                    defaultSelectedForegroundColor = currentColor
                }
                
                if let color = textStorage.attribute(TapLabel.SelectedForegroudColorName,
                    atIndex: range.location,
                    effectiveRange: nil) as? UIColor
                {
                    textStorage.addAttribute(NSForegroundColorAttributeName, value: color, range: range)
                }
            }
            else if let (_, range) = oldValue
            {
                textStorage.addAttribute(NSForegroundColorAttributeName,
                    value: defaultSelectedForegroundColor!,
                    range: range)
            }
            
            setNeedsDisplay()
        }
    }
    
    public override var lineBreakMode: NSLineBreakMode {
        didSet {
            textContainer.lineBreakMode = lineBreakMode
        }
    }
    
    public override var numberOfLines: Int {
        didSet {
            textContainer.maximumNumberOfLines = numberOfLines
        }
    }
    
    public override var attributedText: NSAttributedString! {
        didSet {
            textStorage.setAttributedString(attributedText)
            updateLinks()
            updateRangesForUrls()
        }
    }
    
    public override var frame: CGRect {
        didSet {
            textContainer.size = frame.size
        }
    }
    
    public override var bounds: CGRect {
        didSet {
            textContainer.size = bounds.size
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        textContainer.lineFragmentPadding = 0
        textContainer.lineBreakMode = lineBreakMode
        textContainer.maximumNumberOfLines = numberOfLines
        textContainer.size = frame.size
        
        layoutManager.addTextContainer(textContainer)
        layoutManager.delegate = self
        
        textStorage.addLayoutManager(layoutManager)
        
        userInteractionEnabled = true
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func updateLinks() {
        links = [String: NSRange]()
        
        attributedText.enumerateAttribute(TapLabel.LinkContentName,
            inRange: NSMakeRange(0, attributedText.length),
            options: NSAttributedStringEnumerationOptions(rawValue: 0))
            {
                value, range, stop in
                
                if let v = value as? String {
                    self.links[v] = range
                }
        }
    }
    
    private func updateRangesForUrls()
    {
       // var error: NSError?
        
        //NSDataDetector(types: NSTextCheckingType.Link.rawValue, error: &error)!
        
        
//        do {
//            try!
//        }
//        
//        
//        let detector = NSDataDetector(types: NSTextCheckingType.Link.rawValue)
//        let plainText = attributedText.string
//        let matches = detector.matchesInString(plainText,
//            options: NSMatchingOptions(0),
//            range: NSMakeRange(0, count(plainText))) as! [NSTextCheckingResult]
//        
//        rangesForUrls = matches.map { $0.range }
    }
    
    public override func textRectForBounds(bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect
    {
        let savedTextContainerSize = textContainer.size
        let savedTextContainerNumberOfLines = textContainer.maximumNumberOfLines
        
        textContainer.size = bounds.size
        textContainer.maximumNumberOfLines = numberOfLines
        
        let glyphRange = layoutManager.glyphRangeForTextContainer(textContainer)
        var textBounds = layoutManager.boundingRectForGlyphRange(glyphRange, inTextContainer:textContainer)
        
        textBounds.origin = bounds.origin
        textBounds.size.width = ceil(textBounds.size.width)
        textBounds.size.height = ceil(textBounds.size.height)
        
        textContainer.size = savedTextContainerSize
        textContainer.maximumNumberOfLines = savedTextContainerNumberOfLines
        
        return textBounds;
    }
    
    public override func drawTextInRect(rect: CGRect)
    {
        let glyphRange = layoutManager.glyphRangeForTextContainer(textContainer)
        let textOffset = calcTextOffsetForGlyphRange(glyphRange)
        
        layoutManager.drawBackgroundForGlyphRange(glyphRange, atPoint:textOffset)
        layoutManager.drawGlyphsForGlyphRange(glyphRange, atPoint:textOffset)
    }
    
    private func calcTextOffsetForGlyphRange(glyphRange: NSRange) -> CGPoint
    {
        var textOffset = CGPointZero
        
        let textBounds = layoutManager.boundingRectForGlyphRange(glyphRange, inTextContainer:textContainer)
        let paddingHeight = (self.bounds.size.height - textBounds.size.height) / 2
        if (paddingHeight > 0) {
            textOffset.y = paddingHeight;
        }
        
        return textOffset;
    }
    
    private func linkAtPoint(point: CGPoint) -> (String, NSRange)? {
        var point2 = point
        if textStorage.length == 0 {
            return nil
        }
        
        let glyphRange = layoutManager.glyphRangeForTextContainer(textContainer)
        let textOffset = calcTextOffsetForGlyphRange(glyphRange)
        
        point2.x = point2.x - textOffset.x
        point2.y = point2.y - textOffset.y
        
        let touchedChar = layoutManager.glyphIndexForPoint(point2, inTextContainer:textContainer)
        
        var lineRange = NSRange()
        let lineRect = layoutManager.lineFragmentUsedRectForGlyphAtIndex(touchedChar, effectiveRange:&lineRange)
        
        if !CGRectContainsPoint(lineRect, point2) {
            return nil
        }
        
        // Find the word that was touched and call the detection block
        for (link, range) in links {
            if range.location <= touchedChar && touchedChar < range.location + range.length {
                return (link, range)
            }
        }
        
        return nil
    }
    
    //MARK: - Interactions
    
    public override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        isTouchMoved = false
        
        let touchLocation = touches.first!.locationInView(self)
        
        if let (link, range) = linkAtPoint(touchLocation) {
            selected = (link, range)
        } else {
            super.touchesBegan(touches, withEvent: event)
        }
        
    }
    
    public override func touchesMoved(touches: Set<UITouch>, withEvent event: UIEvent?) {
        super.touchesMoved(touches, withEvent: event)
        isTouchMoved = true
        selected = nil
    }
    
    public override func touchesEnded(touches: Set<UITouch>, withEvent event: UIEvent?) {
        super.touchesEnded(touches, withEvent: event)
        
        if !isTouchMoved {
            if (selected != nil) {
                delegate?.tapLabel(self, didSelectLink: selected!.0)
            }
        }
        
        selected = nil
    }
    
    
    public override func touchesCancelled(touches: Set<UITouch>?, withEvent event: UIEvent?) {
        super.touchesCancelled(touches, withEvent: event)
        selected = nil
    }
    
    
    //MARK: - NSLayoutManagerDelegate
    
    @objc public func layoutManager(
        layoutManager: NSLayoutManager,
        shouldBreakLineByWordBeforeCharacterAtIndex charIndex: Int) -> Bool
    {
        for range in rangesForUrls {
            if range.location < charIndex && charIndex < range.location + range.length {
                return false
            }
        }
        return true
    }
    
}