Pod::Spec.new do |s|
  s.name         		= "ActorSDK-iOS"
  s.version      		= "0.0.26"
  s.summary      		= "Actor SDK for intergration Actor Messaging to your apps"
  s.homepage        = "https://actor.im/"
  s.license         = { :type => 'MIT', :file => 'LICENSE' }
  s.author       		= { "Actor LLC" => "steve@actor.im" }
  s.source          = { :http => "https://github.com/actorapp/ActorSDK-iOS/releases/download/v#{s.version}/ActorSDK.zip https://github.com/actorapp/ActorSDK-iOS.git"}

  s.platform     		= :ios, "8.0"
  s.requires_arc 		= true

  # Core
  s.dependency 'RegexKitLite'
  s.dependency 'zipzap'
  s.dependency 'J2ObjC-Framework'
  s.dependency 'ReachabilitySwift'

  # UI
  s.dependency 'VBFPopFlatButton'
  s.dependency 'MBProgressHUD'
  s.dependency 'SZTextView'
  s.dependency 'RSKImageCropper'
  s.dependency 'JDStatusBarNotification'
  s.dependency 'YYImage'
  s.dependency 'YYImage/WebP'
  s.dependency 'YYCategories'
  s.dependency 'YYWebImage'
  s.dependency 'DZNWebViewController'

  s.dependency 'TTTAttributedLabel'
  s.dependency 'M13ProgressSuite'

  s.ios.preserve_paths = '**/*'
  s.ios.vendored_frameworks = 'ActorSDK.framework'

  # s.xcconfig = { 
  #    "SWIFT_INCLUDE_PATHS" => "$(PROJECT_DIR)/ActorSDK-iOS/Frameworks/",
  #    "FRAMEWORK_SEARCH_PATHS" => "$(PROJECT_DIR)/ActorSDK-iOS/Frameworks/"
  # }
end
