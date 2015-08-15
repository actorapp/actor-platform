Pod::Spec.new do |s|
  s.name         = "ActorCore"
  s.version      = '1.0.0'
  s.summary      = "Core Library of Actor Platform"
  s.authors      = "Steve Kite"

  s.ios.deployment_target = '7.0'
  s.frameworks = 'Security'
  s.osx.deployment_target = '10.7'
  s.osx.frameworks = 'ExceptionHandling'
  s.requires_arc = true
  
  # s.public_header_files = '{core,runtime}-cocoa/src/main/objc/*.h', 'build-tools/dist/j2objc-0.9.7/include/**/*.h'
  # s.header_mappings_dir = 'build-tools/dist/j2objc-0.9.7/include'
  # s.private_header_files = 'core-cocoa/src/gen/objc/**/*.h', 'runtime-cocoa/src/gen/objc/**/*.h'

  s.library = 'jre_emul', 'z'

  s.source_files = '{core,runtime}-cocoa/src/{main,gen}/objc/**/*'
  s.resource = 'core/src/main/resources/*'

  s.pod_target_xcconfig = { 'OTHER_LDFLAGS' => '-lObjC' }

  # s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/lib"', 'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/include" "$(PODS_ROOT)/../../core-cocoa/src/gen/objc/" "$(PODS_ROOT)/../../core-cocoa/src/main/objc/" "$(PODS_ROOT)/../../runtime-cocoa/src/main/objc/" "$(PODS_ROOT)/../../runtime-cocoa/src/gen/objc/"' }  

end