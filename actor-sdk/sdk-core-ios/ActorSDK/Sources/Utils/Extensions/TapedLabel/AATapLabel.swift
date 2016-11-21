//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class TapLabel: UILabel, NSLayoutManagerDelegate {
    
    open static let LinkContentName = "TapLabelLinkContentName"
    open static let SelectedForegroudColorName = "TapLabelSelectedForegroudColorName"
    
    open weak var delegate: AATapLabelDelegate?
    
    fileprivate let layoutManager = NSLayoutManager()
    fileprivate let textContainer = NSTextContainer()
    fileprivate let textStorage = NSTextStorage()
    fileprivate var rangesForUrls = [NSRange]()
    fileprivate var links = [String: NSRange]()
    fileprivate var isTouchMoved = false
    fileprivate var defaultSelectedForegroundColor: UIColor?
    
    fileprivate var selected: (String, NSRange)? {
        didSet {
            if let (_, range) = selected
            {
                if let currentColor = textStorage.attribute(NSForegroundColorAttributeName,
                    at: range.location,
                    effectiveRange: nil) as? UIColor
                {
                    defaultSelectedForegroundColor = currentColor
                }
                
                if let color = textStorage.attribute(TapLabel.SelectedForegroudColorName,
                    at: range.location,
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
    
    open override var lineBreakMode: NSLineBreakMode {
        didSet {
            textContainer.lineBreakMode = lineBreakMode
        }
    }
    
    open override var numberOfLines: Int {
        didSet {
            textContainer.maximumNumberOfLines = numberOfLines
        }
    }
    
    open override var attributedText: NSAttributedString! {
        didSet {
            textStorage.setAttributedString(attributedText)
            updateLinks()
            updateRangesForUrls()
        }
    }
    
    open override var frame: CGRect {
        didSet {
            textContainer.size = frame.size
        }
    }
    
    open override var bounds: CGRect {
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
        
        isUserInteractionEnabled = true
    }
    
    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func updateLinks() {
        links = [String: NSRange]()
        
        attributedText.enumerateAttribute(TapLabel.LinkContentName,
            in: NSMakeRange(0, attributedText.length),
            options: NSAttributedString.EnumerationOptions(rawValue: 0))
            {
                value, range, stop in
                
                if let v = value as? String {
                    self.links[v] = range
                }
        }
    }
    
    fileprivate func updateRangesForUrls()
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
    
    open override func textRect(forBounds bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect
    {
        let savedTextContainerSize = textContainer.size
        let savedTextContainerNumberOfLines = textContainer.maximumNumberOfLines
        
        textContainer.size = bounds.size
        textContainer.maximumNumberOfLines = numberOfLines
        
        let glyphRange = layoutManager.glyphRange(for: textContainer)
        var textBounds = layoutManager.boundingRect(forGlyphRange: glyphRange, in:textContainer)
        
        textBounds.origin = bounds.origin
        textBounds.size.width = ceil(textBounds.size.width)
        textBounds.size.height = ceil(textBounds.size.height)
        
        textContainer.size = savedTextContainerSize
        textContainer.maximumNumberOfLines = savedTextContainerNumberOfLines
        
        return textBounds;
    }
    
    open override func drawText(in rect: CGRect)
    {
        let glyphRange = layoutManager.glyphRange(for: textContainer)
        let textOffset = calcTextOffsetForGlyphRange(glyphRange)
        
        layoutManager.drawBackground(forGlyphRange: glyphRange, at:textOffset)
        layoutManager.drawGlyphs(forGlyphRange: glyphRange, at:textOffset)
    }
    
    fileprivate func calcTextOffsetForGlyphRange(_ glyphRange: NSRange) -> CGPoint
    {
        var textOffset = CGPoint.zero
        
        let textBounds = layoutManager.boundingRect(forGlyphRange: glyphRange, in:textContainer)
        let paddingHeight = (self.bounds.size.height - textBounds.size.height) / 2
        if (paddingHeight > 0) {
            textOffset.y = paddingHeight;
        }
        
        return textOffset;
    }
    
    fileprivate func linkAtPoint(_ point: CGPoint) -> (String, NSRange)? {
        var point2 = point
        if textStorage.length == 0 {
            return nil
        }
        
        let glyphRange = layoutManager.glyphRange(for: textContainer)
        let textOffset = calcTextOffsetForGlyphRange(glyphRange)
        
        point2.x = point2.x - textOffset.x
        point2.y = point2.y - textOffset.y
        
        let touchedChar = layoutManager.glyphIndex(for: point2, in:textContainer)
        
        var lineRange = NSRange()
        let lineRect = layoutManager.lineFragmentUsedRect(forGlyphAt: touchedChar, effectiveRange:&lineRange)
        
        if !lineRect.contains(point2) {
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
    
    open override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        isTouchMoved = false
        
        let touchLocation = touches.first!.location(in: self)
        
        if let (link, range) = linkAtPoint(touchLocation) {
            selected = (link, range)
        } else {
            super.touchesBegan(touches, with: event)
        }
        
    }
    
    open override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesMoved(touches, with: event)
        isTouchMoved = true
        selected = nil
    }
    
    open override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        
        if !isTouchMoved {
            if (selected != nil) {
                delegate?.tapLabel(self, didSelectLink: selected!.0)
            }
        }
        
        selected = nil
    }
    
    
    open override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesCancelled(touches, with: event)
        selected = nil
    }
    
    
    //MARK: - NSLayoutManagerDelegate
    
    @objc open func layoutManager(
        _ layoutManager: NSLayoutManager,
        shouldBreakLineByWordBeforeCharacterAt charIndex: Int) -> Bool
    {
        for range in rangesForUrls {
            if range.location < charIndex && charIndex < range.location + range.length {
                return false
            }
        }
        return true
    }
    
}
