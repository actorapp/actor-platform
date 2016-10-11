//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import UIKit


class AACustomPresentationAnimationController: NSObject, UIViewControllerAnimatedTransitioning {
    
    let isPresenting :Bool
    let duration :TimeInterval = 0.5

    init(isPresenting: Bool) {
        self.isPresenting = isPresenting

        super.init()
    }


    // ---- UIViewControllerAnimatedTransitioning methods

    func transitionDuration(using transitionContext: UIViewControllerContextTransitioning?) -> TimeInterval {
        return self.duration
    }

    func animateTransition(using transitionContext: UIViewControllerContextTransitioning)  {
        if isPresenting {
            animatePresentationWithTransitionContext(transitionContext)
        }
        else {
            animateDismissalWithTransitionContext(transitionContext)
        }
    }


    // ---- Helper methods

    func animatePresentationWithTransitionContext(_ transitionContext: UIViewControllerContextTransitioning) {
        let presentedController = transitionContext.viewController(forKey: UITransitionContextViewControllerKey.to)!
        let presentedControllerView = transitionContext.view(forKey: UITransitionContextViewKey.to)!
        let containerView = transitionContext.containerView

        // Position the presented view off the top of the container view
        presentedControllerView.frame = transitionContext.finalFrame(for: presentedController)
        //presentedControllerView.center.y -= containerView!.bounds.size.height

        containerView.addSubview(presentedControllerView)

        // Animate the presented view to it's final position
        UIView.animate(withDuration: self.duration, delay: 0.0, usingSpringWithDamping: 1.0, initialSpringVelocity: 0.0, options: .allowUserInteraction, animations: {
            //presentedControllerView.center.y += containerView!.bounds.size.height
        }, completion: {(completed: Bool) -> Void in
            transitionContext.completeTransition(completed)
        })
    }

    func animateDismissalWithTransitionContext(_ transitionContext: UIViewControllerContextTransitioning) {
        let presentedControllerView = transitionContext.view(forKey: UITransitionContextViewKey.from)!
        //let containerView = transitionContext.containerView()

        // Animate the presented view off the bottom of the view
        UIView.animate(withDuration: self.duration, delay: 0.0, usingSpringWithDamping: 1.0, initialSpringVelocity: 0.0, options: .allowUserInteraction, animations: {
            //presentedControllerView.center.y += containerView!.bounds.size.height
            presentedControllerView.alpha = 0.0
        }, completion: {(completed: Bool) -> Void in
                transitionContext.completeTransition(completed)
        })
    }
}
