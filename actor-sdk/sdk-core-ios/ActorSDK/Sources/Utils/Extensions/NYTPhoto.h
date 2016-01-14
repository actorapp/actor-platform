//
//  NYTPhoto.h
//  NYTPhotoViewer
//
//  Created by Brian Capps on 2/10/15.
//  Copyright (c) 2015 NYTimes. All rights reserved.
//

@import UIKit;

NS_ASSUME_NONNULL_BEGIN

/**
 *  The model for photos displayed in an `NYTPhotosViewController`.
 */
@protocol NYTPhoto <NSObject>

/**
 *  The image to display.
 */
@property (nonatomic, readonly, nullable) UIImage *image;

/**
 * The image data to display. This will be preferred over the `image` property.
 * In case this is empty `image` will be used. The main advantage of using this is animated gif support.
 */
@property (nonatomic, readonly, nullable) NSData *imageData;

/**
 *  A placeholder image for display while the image is loading.
 */
@property (nonatomic, readonly, nullable) UIImage *placeholderImage;

/**
 *  An attributed string for display as the title of the caption.
 */
@property (nonatomic, readonly, nullable) NSAttributedString *attributedCaptionTitle;

/**
 *  An attributed string for display as the summary of the caption.
 */
@property (nonatomic, readonly, nullable) NSAttributedString *attributedCaptionSummary;

/**
 *  An attributed string for display as the credit of the caption.
 */
@property (nonatomic, readonly, nullable) NSAttributedString *attributedCaptionCredit;

@end

NS_ASSUME_NONNULL_END
