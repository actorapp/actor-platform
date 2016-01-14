//
//  NYTPhotosViewController.h
//  NYTPhotoViewer
//
//  Created by Brian Capps on 2/10/15.
//  Copyright (c) 2015 NYTimes. All rights reserved.
//

@import UIKit;

@class NYTPhotosOverlayView;

@protocol NYTPhoto;
@protocol NYTPhotosViewControllerDelegate;

NS_ASSUME_NONNULL_BEGIN

// All notifications will have the `NYTPhotosViewController` instance set as the object.
extern NSString * const NYTPhotosViewControllerDidNavigateToPhotoNotification;
extern NSString * const NYTPhotosViewControllerWillDismissNotification;
extern NSString * const NYTPhotosViewControllerDidDismissNotification;

@interface NYTPhotosViewController : UIViewController

/**
 *  The pan gesture recognizer used for panning to dismiss the photo. Disable to stop the pan-to-dismiss behavior.
 */
@property (nonatomic, readonly) UIPanGestureRecognizer *panGestureRecognizer;

/**
 *  The tap gesture recognizer used to hide the overlay, including the caption, left and right bar button items, and title, all at once. Disable to always show the overlay.
 */
@property (nonatomic, readonly) UITapGestureRecognizer *singleTapGestureRecognizer;

/**
 *  The internal page view controller used for swiping horizontally, photo to photo. Created during `viewDidLoad`.
 */
@property (nonatomic, readonly, nullable) UIPageViewController *pageViewController;

/**
 *  The object conforming to `NYTPhoto` that is currently being displayed by the `pageViewController`.
 */
@property (nonatomic, readonly, nullable) id <NYTPhoto> currentlyDisplayedPhoto;

/**
 *  The overlay view displayed over photos. Created during `viewDidLoad`.
 */
@property (nonatomic, readonly, nullable) NYTPhotosOverlayView *overlayView;

/**
 *  The left bar button item overlaying the photo.
 */
@property (nonatomic, nullable) UIBarButtonItem *leftBarButtonItem;

/**
 *  The left bar button items overlaying the photo.
 */
@property (nonatomic, copy, nullable) NSArray <UIBarButtonItem *> *leftBarButtonItems;

/**
 *  The right bar button item overlaying the photo.
 */
@property (nonatomic, nullable) UIBarButtonItem *rightBarButtonItem;

/**
 *  The right bar button items overlaying the photo.
 */
@property (nonatomic, copy, nullable) NSArray <UIBarButtonItem *> *rightBarButtonItems;

/**
 *  The object that acts as the delegate of the `NYTPhotosViewController`.
 */
@property (nonatomic, weak, nullable) id <NYTPhotosViewControllerDelegate> delegate;

/**
 *  A convenience initializer that calls `initWithPhotos:initialPhoto:`, passing the first photo as the `initialPhoto` argument.
 *
 *  @param photos An array of objects conforming to the `NYTPhoto` protocol.
 *
 *  @return A fully initialized object.
 */
- (instancetype)initWithPhotos:(NSArray <id <NYTPhoto>> * _Nullable)photos;

/**
 *  The designated initializer that stores the array of objects conforming to the `NYTPhoto` protocol for display, along with specifying an initial photo for display.
 *
 *  @param photos An array of objects conforming to the `NYTPhoto` protocol.
 *  @param initialPhoto The photo to display initially. Must be contained within the `photos` array. If `nil` or not within the `photos` array, the first photo within the `photos` array will be displayed.
 *
 *  @return A fully initialized object.
 */
- (instancetype)initWithPhotos:(NSArray <id <NYTPhoto>> * _Nullable)photos initialPhoto:(id <NYTPhoto> _Nullable)initialPhoto NS_DESIGNATED_INITIALIZER;

/**
 *  Displays the specified photo. Can be called before the view controller is displayed. Calling with a photo not contained within the data source has no effect.
 *
 *  @param photo    The photo to make the currently displayed photo.
 *  @param animated Whether to animate the transition to the new photo.
 */
- (void)displayPhoto:(id <NYTPhoto> _Nullable)photo animated:(BOOL)animated;

/**
 *  Update the image displayed for the given photo object.
 *
 *  @param photo The photo for which to display the new image.
 */
- (void)updateImageForPhoto:(id <NYTPhoto> _Nullable)photo;

@end

/**
 *  A protocol of entirely optional methods called for configuration and lifecycle events by an `NYTPhotosViewController` instance.
 */
@protocol NYTPhotosViewControllerDelegate <NSObject>

@optional

/**
 *  Called when a new photo is displayed through a swipe gesture.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                The photo object that was just displayed.
 *  @param photoIndex           The index of the photo that was just displayed.
 */
- (void)photosViewController:(NYTPhotosViewController *)photosViewController didNavigateToPhoto:(id <NYTPhoto>)photo atIndex:(NSUInteger)photoIndex;

/**
 *  Called immediately before the photos view controller is about to start dismissal. This will be the beginning of the interactive panning to dismiss, if it is enabled and performed.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 */
- (void)photosViewControllerWillDismiss:(NYTPhotosViewController *)photosViewController;

/**
 *  Called immediately after the photos view controller has dismissed.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 */
- (void)photosViewControllerDidDismiss:(NYTPhotosViewController *)photosViewController;

/**
 *  Returns a view to display over a photo, full width, locked to the bottom, representing the caption for the photo. Can be any `UIView` object, but is expected to respond to `intrinsicContentSize` appropriately to calculate height.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                The photo object over which to display the caption view.
 *
 *  @return A view to display as the caption for the photo. Return `nil` to show a default view generated from the caption properties on the photo object.
 */
- (UIView * _Nullable)photosViewController:(NYTPhotosViewController *)photosViewController captionViewForPhoto:(id <NYTPhoto>)photo;

/**
 *  Returns a view to display while a photo is loading. Can be any `UIView` object, but is expected to respond to `sizeToFit` appropriately. This view will be sized and centered in the blank area, and hidden when the photo image is loaded.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                The photo object over which to display the activity view.
 *
 *  @return A view to display while the photo is loading. Return `nil` to show a default white `UIActivityIndicatorView`.
 */
- (UIView * _Nullable)photosViewController:(NYTPhotosViewController *)photosViewController loadingViewForPhoto:(id <NYTPhoto>)photo;

/**
 *  Returns the view from which to animate for a given object conforming to the `NYTPhoto` protocol.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                The photo for which the animation will occur.
 *
 *  @return The view to animate out of or into for the given photo.
 */
- (UIView * _Nullable)photosViewController:(NYTPhotosViewController *)photosViewController referenceViewForPhoto:(id <NYTPhoto>)photo;

/**
*  Returns the maximum zoom scale for a given object conforming to the `NYTPhoto` protocol.
*
*  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
*  @param photo                The photo for which the maximum zoom scale is requested.
*
*  @return The maximum zoom scale for the given photo.
*/
- (CGFloat)photosViewController:(NYTPhotosViewController *)photosViewController maximumZoomScaleForPhoto:(id <NYTPhoto>)photo;

/**
 *  Called when a photo is long pressed.
 *
 *  @param photosViewController       The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                      The photo being displayed that was long pressed.
 *  @param longPressGestureRecognizer The gesture recognizer that detected the long press.
 *
 *  @return `YES` if the long press was handled by the client, `NO` if the default `UIMenuController` with a Copy action is desired.
 */
- (BOOL)photosViewController:(NYTPhotosViewController *)photosViewController handleLongPressForPhoto:(id <NYTPhoto>)photo withGestureRecognizer:(UILongPressGestureRecognizer *)longPressGestureRecognizer;

/**
 *  Called when the action button is tapped.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param photo                The photo being displayed when the action button was tapped.
 *
 *  @return `YES` if the action button tap was handled by the client, `NO` if the default `UIActivityViewController` is desired.
 */
- (BOOL)photosViewController:(NYTPhotosViewController *)photosViewController handleActionButtonTappedForPhoto:(id <NYTPhoto>)photo;

/**
 *  Called after the default `UIActivityViewController` is presented and successfully completes an action with a specified activity type.
 *
 *  @param photosViewController The `NYTPhotosViewController` instance that sent the delegate message.
 *  @param activityType         The activity type that was successfully shared.
 */
- (void)photosViewController:(NYTPhotosViewController *)photosViewController actionCompletedWithActivityType:(NSString * _Nullable)activityType;

@end

NS_ASSUME_NONNULL_END
