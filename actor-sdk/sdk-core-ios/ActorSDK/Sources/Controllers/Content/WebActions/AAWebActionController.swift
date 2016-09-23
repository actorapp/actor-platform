//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AAWebActionController: AAViewController, UIWebViewDelegate {
    
    fileprivate var webView = UIWebView()
    
    fileprivate let regex: AARegex
    fileprivate let desc: ACWebActionDescriptor
    
    public init(desc: ACWebActionDescriptor) {
        self.desc = desc
        self.regex = AARegex(desc.getRegexp())
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        webView.delegate = self
        
        view.addSubview(webView)
        
        webView.loadRequest(URLRequest(url: URL(string: desc.getUri())!))
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        webView.frame = view.bounds
    }
    
    open func webView(_ webView: UIWebView, shouldStartLoadWith request: URLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        if let url = request.url {
            let rawUrl = url.absoluteString
            
            // Match end url
            if regex.test(rawUrl) {
                self.executeSafe(Actor.completeWebAction(withHash: desc.getActionHash(), withUrl: rawUrl)) { (val) -> Void in
                    self.dismissController()
                }
                return false
            }
        }
        return true
    }
}
