//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

public class AACircleButton: UIView {
    
    private let buttonSize: CGFloat
    
    public let button = UIButton()
    private let titleView = UILabel()
    private let borderView = UIImageView()
    
    public var image: UIImage? {
        didSet(v) {
            updateStyles()
        }
    }
    
    public var title: String? {
        get {
            return titleView.text
        }
        set(v) {
            titleView.text = v
        }
    }

    public var filled: Bool = false {
        didSet(v) {
            updateStyles()
        }
    }
    
    public var enabled: Bool = true {
        didSet(v) {
            button.enabled = v
            button.userInteractionEnabled = v
            updateStyles()
        }
    }
    
    public var borderColor: UIColor = UIColor.whiteColor().alpha(0.87) {
        didSet(v) {
            updateStyles()
        }
    }
    
    public init(size: CGFloat) {
        self.buttonSize = size
        super.init(frame: CGRectMake(0, 0, size, size))
        
        borderView.bounds = CGRectMake(0, 0, size, size)
        borderView.frame = borderView.bounds
        
        titleView.font = UIFont.thinSystemFontOfSize(17)
        titleView.textAlignment = .Center
        titleView.bounds = CGRectMake(0, 0, 86, 44)
        titleView.adjustsFontSizeToFitWidth = true
        
        button.bounds = CGRectMake(0, 0, size, size)
        
        updateStyles()
        
        addSubview(borderView)
        addSubview(button)
        addSubview(titleView)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func updateStyles() {
        let mainColor = enabled ? UIColor.whiteColor() : UIColor.whiteColor().alpha(0.3)
        let selectedColor = enabled ? UIColor.blackColor() : UIColor.blackColor().alpha(0.3)
        titleView.textColor = mainColor
        if (filled) {
            borderView.image = Imaging.roundedImage(mainColor, radius: buttonSize / 2)
            button.setImage(image?.tintImage(selectedColor), forState: .Normal)
        } else {
            borderView.image = Imaging.circleImage(mainColor, radius: buttonSize / 2)
            button.setImage(image?.tintImage(mainColor), forState: .Normal)
        }
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        button.topIn(self.bounds)
        borderView.topIn(self.bounds)
        titleView.under(button.frame, offset: 5)
    }
}