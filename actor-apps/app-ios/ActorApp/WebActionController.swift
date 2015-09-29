//
//  Copyright (c) 2014-2015 Actor LLC. <https://actor.im>
//

import Foundation

class WebActionController: AAViewController, UIWebViewDelegate {
    
    var webView = UIWebView()
    
    let regex: Regex
    let desc: ACWebActionDescriptor
    
    init(desc: ACWebActionDescriptor) {
        self.desc = desc
        self.regex = Regex(desc.getRegexp())
        super.init()
    }

    required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView.delegate = self
        
        view.addSubview(webView)
        
        webView.loadRequest(NSURLRequest(URL: NSURL(string: desc.getUri())!))
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        webView.frame = view.bounds
    }
    
    func webView(webView: UIWebView, shouldStartLoadWithRequest request: NSURLRequest, navigationType: UIWebViewNavigationType) -> Bool {
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