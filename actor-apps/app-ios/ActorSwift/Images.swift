//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation
import NYTPhotoViewer

class AAPhoto: NSObject, NYTPhoto {
    
    var image: UIImage?
    var placeholderImage: UIImage?
    let attributedCaptionTitle: NSAttributedString?
    let attributedCaptionSummary: NSAttributedString?
    let attributedCaptionCredit: NSAttributedString?
    
    init(image: UIImage?) {
        self.image = image
        self.placeholderImage = nil
        self.attributedCaptionTitle = nil
        self.attributedCaptionSummary = nil
        self.attributedCaptionCredit = nil
    }
    
    init(image: UIImage?, placeholderImage: UIImage?, attributedCaptionTitle: NSAttributedString?, attributedCaptionSummary: NSAttributedString?, attributedCaptionCredit: NSAttributedString?) {
        self.image = image
        self.placeholderImage = placeholderImage
        self.attributedCaptionTitle = attributedCaptionTitle
        self.attributedCaptionSummary = attributedCaptionSummary
        self.attributedCaptionCredit = attributedCaptionCredit
    }
}



