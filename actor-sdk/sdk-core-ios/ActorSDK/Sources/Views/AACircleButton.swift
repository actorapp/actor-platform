//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AACircleButton: UIView {
    
    fileprivate let buttonSize: CGFloat
    
    open let button = UIButton()
    fileprivate let titleView = UILabel()
    fileprivate let borderView = UIImageView()
    
    open var image: UIImage? {
        didSet(v) {
            updateStyles()
        }
    }
    
    open var title: String? {
        get {
            return titleView.text
        }
        set(v) {
            titleView.text = v
        }
    }

    open var filled: Bool = false {
        didSet(v) {
            updateStyles()
        }
    }
    
    open var enabled: Bool = true {
        didSet(v) {
            button.isEnabled = v
            button.isUserInteractionEnabled = v
            updateStyles()
        }
    }
    
    open var borderColor: UIColor = UIColor.white.alpha(0.87) {
        didSet(v) {
            updateStyles()
        }
    }
    
    public init(size: CGFloat) {
        self.buttonSize = size
        super.init(frame: CGRect(x: 0, y: 0, width: size, height: size))
        
        borderView.bounds = CGRect(x: 0, y: 0, width: size, height: size)
        borderView.frame = borderView.bounds
        
        titleView.font = UIFont.thinSystemFontOfSize(17)
        titleView.textAlignment = .center
        titleView.bounds = CGRect(x: 0, y: 0, width: 86, height: 44)
        titleView.adjustsFontSizeToFitWidth = true
        
        button.bounds = CGRect(x: 0, y: 0, width: size, height: size)
        
        updateStyles()
        
        addSubview(borderView)
        addSubview(button)
        addSubview(titleView)
    }

    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func updateStyles() {
        let mainColor = enabled ? UIColor.white : UIColor.white.alpha(0.3)
        let selectedColor = enabled ? UIColor.black : UIColor.black.alpha(0.3)
        titleView.textColor = mainColor
        if (filled) {
            borderView.image = Imaging.roundedImage(mainColor, radius: buttonSize / 2)
            button.setImage(image?.tintImage(selectedColor), for: UIControlState())
        } else {
            borderView.image = Imaging.circleImage(mainColor, radius: buttonSize / 2)
            button.setImage(image?.tintImage(mainColor), for: UIControlState())
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        button.topIn(self.bounds)
        borderView.topIn(self.bounds)
        titleView.under(button.frame, offset: 5)
    }
}
