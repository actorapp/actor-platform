Pod::Spec.new do |s|
  s.name         		= "ActorSDK-iOS"
  s.version      		= "0.0.26"
  s.summary      		= "Actor SDK for intergration Actor Messaging to your apps"
  s.homepage        = "https://actor.im/"
  s.license         = { :type => 'MIT', :file => 'LICENSE' }
  s.author       		= { "Actor LLC" => "steve@actor.im" }
  s.source          = { :git => "https://github.com/actorapp/ActorSDK-iOS.git", :tag => "v#{s.version}" }

  s.platform     		= :ios, "8.0"
  s.requires_arc 		= true

  s.prepare_command = <<-CMD
      Scripts/download.sh
  CMD

  # Core
  s.dependency 'RegexKitLite'
  s.dependency 'CocoaAsyncSocket'
  s.dependency 'zipzap'
  s.dependency 'J2ObjC-Framework'

  # UI
  s.dependency 'VBFPopFlatButton'
  s.dependency 'MBProgressHUD'
  s.dependency 'PSTAlertController'
  s.dependency 'SZTextView'
  s.dependency 'RSKImageCropper'
  s.dependency 'JDStatusBarNotification'
  s.dependency 'YYImage', '0.9.3'
  s.dependency 'YYCategories'
  s.dependency 'DZNWebViewController'

  s.dependency 'TTTAttributedLabel'
  s.dependency 'M13ProgressSuite'

  s.preserve_paths = 'Frameworks'
  s.vendored_framework = 'Frameworks/ActorSDK.framework'

  s.xcconfig = { 
     "SWIFT_INCLUDE_PATHS" => "$(PROJECT_DIR)/ActorSDK-iOS/Frameworks/",
     "FRAMEWORK_SEARCH_PATHS" => "$(PROJECT_DIR)/ActorSDK-iOS/Frameworks/"
  }
end
