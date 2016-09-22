//
//  Copyright (c) 2014-2016 Actor LLC. <https://actor.im>
//

import Foundation

open class AACodePreviewController: AAViewController {
    
    var webView = UIWebView()
    let code: String
    
    public init(code: String) {
        self.code = code
        super.init()
    }

    public required init(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationItem.title = "Source Code"

        let data = "<html>\n<header>\n<link rel=\"stylesheet\" href=\"highlight-default.min.css\">\n<script src=\"highlight.min.js\"></script>\n<script>hljs.initHighlightingOnLoad();</script>\n</header>\n<body>\n<pre><code>" +
            code
                .replace("&", dest: "&amp;")
                .replace("<", dest: "&lt;")
                .replace(">", dest: "&gt;")
                .replace("\"", dest: "&quot;")
                .replace("\n", dest: "<br/>") +
            "</code></pre>\n" +
            "</body>\n" +
            "</html>"

        let bundle = Bundle.framework
        let path = bundle.path(forResource: "highlight.min", ofType: "js")!
        
        webView.loadHTMLString(data, baseURL: URL(fileURLWithPath: path))
        
        view.addSubview(webView)
    }
    
    open override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        webView.frame = view.bounds
    }
}
