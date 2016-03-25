//
//  ElegantPresentation.swift
//  TwitterPresentationController
//
//  Created by Kyle Bashour on 2/21/16.
//  Copyright © 2016 Kyle Bashour. All rights reserved.
//

import UIKit

typealias CoordinatedAnimation = UIViewControllerTransitionCoordinatorContext? -> Void

class ElegantPresentationController: UIPresentationController {
    
    
    // MARK: - Properties
    
    /// Dims the presenting view controller, if option is set
    private lazy var dimmingView: UIView = {
        
        let view = UIView()
        
        view.backgroundColor = UIColor.blackColor().colorWithAlphaComponent(0.5)
        view.alpha = 0
        view.userInteractionEnabled = false
        
        return view
    }()
    
    /// For dismissing on tap if option is set
    private lazy var recognizer: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: Selector("dismiss:"))
    
    /// An options struct containing the customization options set
    private let options: PresentationOptions
    
    
    // MARK: - Lifecycle
    
    /**
     Initializes and returns a presentation controller for transitioning between the specified view controllers
     
     - parameter presentedViewController:  The view controller being presented modally.
     - parameter presentingViewController: The view controller whose content represents the starting point of the transition.
     - parameter options:                  An options struct for customizing the appearance and behavior of the presentation.
     
     - returns: An initialized presentation controller object.
     */
    init(presentedViewController: UIViewController, presentingViewController: UIViewController, options: PresentationOptions) {
        self.options = options
        super.init(presentedViewController: presentedViewController, presentingViewController: presentingViewController)
    }
    
    
    // MARK: - Presenting and dismissing
    
    override func presentationTransitionWillBegin() {
        
        // If the option is set, then add the gesture recognizer for dismissal to the container
        if options.dimmingViewTapDismisses {
            containerView!.addGestureRecognizer(recognizer)
        }
        
        // Prepare and position the dimming view
        dimmingView.alpha = 0
        dimmingView.frame = containerView!.bounds
        containerView?.insertSubview(dimmingView, atIndex: 0)
        
        // Animate these properties with the transtion coordinator if possible
        let animations: CoordinatedAnimation = { [unowned self] _ in
            self.dimmingView.alpha = self.options.dimmingViewAlpha
            self.presentingViewController.view.transform = self.options.presentingTransform
        }
        
        transtionWithCoordinator(animations)
    }
    
    override func dismissalTransitionWillBegin() {
        
        // Animate these properties with the transtion coordinator if possible
        let animations: CoordinatedAnimation = { [unowned self] _ in
            self.dimmingView.alpha = 0
            self.presentingViewController.view.transform = CGAffineTransformIdentity
        }
        
        transtionWithCoordinator(animations)
    }
    
    
    // MARK: - Adaptation
    
    override func viewWillTransitionToSize(size: CGSize, withTransitionCoordinator coordinator: UIViewControllerTransitionCoordinator) {
        
        /*
         There's a bug when rotating that makes the presented view controller permanently
         larger or smaller if this isn't here. I'm probably doing something wrong :P
         
         It jumps because it's not in the animation block, but isn't noticiable unless in
         slow-mo. Placing it in the animation block does not fix the issue, so here it is.
         */
        presentingViewController.view.transform = CGAffineTransformIdentity
        
        // Animate these with the coordinator
        let animations: CoordinatedAnimation = { [unowned self] _ in
            self.dimmingView.frame = self.containerView!.bounds
            self.presentingViewController.view.transform = self.options.presentingTransform
            self.presentedView()?.frame = self.frameOfPresentedViewInContainerView()
        }
        
        coordinator.animateAlongsideTransition(animations, completion: nil)
    }
    
    override func sizeForChildContentContainer(container: UIContentContainer, withParentContainerSize parentSize: CGSize) -> CGSize {
        
        // Percent height doesn't make sense as a negative value or greater than zero, so we'll enforce it
        let percentHeight = min(abs(options.presentedPercentHeight), 1)
        
        // Return the appropiate height based on which option is set
        if options.usePercentHeight {
            return CGSize(width: parentSize.width, height: parentSize.height * CGFloat(percentHeight))
        }
        else if options.presentedHeight > 0 {
            return CGSize(width: parentSize.width, height: options.presentedHeight)
        }
        
        return parentSize
    }
    
    override func frameOfPresentedViewInContainerView() -> CGRect {
        
        // Grab the parent and child sizes
        let parentSize = containerView!.bounds.size
        let childSize = sizeForChildContentContainer(presentedViewController, withParentContainerSize: parentSize)
        
        // Create and return an appropiate frame
        return CGRect(x: 0, y: parentSize.height - childSize.height, width: childSize.width, height: childSize.height)
    }
    
    
    // MARK: - Helper functions
    
    // For the tap-to-dismiss
    func dismiss(sender: UITapGestureRecognizer) {
        presentedViewController.dismissViewControllerAnimated(true, completion: nil)
    }
    
    /*
     I noticed myself doing this a lot (more so in earlier versions) so I made a quick function.
     Simply takes a closure with animations in them and attempts to animate with the coordinator.
     */
    private func transtionWithCoordinator(animations: CoordinatedAnimation) {
        if let coordinator = presentingViewController.transitionCoordinator() {
            coordinator.animateAlongsideTransition(animations, completion: nil)
        }
        else {
            animations(nil)
        }
    }
}