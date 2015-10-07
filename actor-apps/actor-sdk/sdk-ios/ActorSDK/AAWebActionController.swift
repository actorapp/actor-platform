//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

public class AAWebActionController: AAViewController, UIWebViewDelegate {
    
    public var webView = UIWebView()
    
    public let regex: Regex
    public let desc: ACWebActionDescriptor
    
    public init(desc: ACWebActionDescriptor) {
        self.desc = desc
        self.regex = Regex(desc.getRegexp())
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        webView.delegate = self
        
        view.addSubview(webView)
        
        webView.loadRequest(NSURLRequest(URL: NSURL(string: desc.getUri())!))
    }
    
    public override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        webView.frame = view.bounds
    }
    
    public func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
        if let url = request.URL {
            let rawUrl = url.absoluteString
            
            // Match end url
            if regex.test(rawUrl) {
                self.executeSafe(Actor.completeWebActionWithHash(desc.getActionHash(), withUrl: rawUrl)) { (val) -> Void in
                    self.dismiss()
                }
                return false
            }
        }
        return true
    }
}