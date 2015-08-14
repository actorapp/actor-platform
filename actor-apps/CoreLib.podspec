Pod::Spec.new do |s|
  s.name         = "CoreLib"
  s.version      = '1.0.0'
  s.summary      = "Core Library of Actor Platform"
  s.authors      = "Steve Kite"

  s.ios.deployment_target = '7.0'
  s.frameworks = 'Security'
  s.osx.deployment_target = '10.7'
  s.osx.frameworks = 'ExceptionHandling'
  s.requires_arc = true
  
  s.public_header_files = 'core-cocoa/src/main/objc/**/*.h', 'runtime-cocoa/src/main/objc/**/*.h'
  s.private_header_files = 'core-cocoa/src/gen/objc/**/*.h', 'runtime-cocoa/src/gen/objc/**/*.h'

  # s.libraries = 'jre_emul', 'z'
  s.library = 'z'

  s.source_files = 'core-cocoa/src/gen/objc/**/*', 'core-cocoa/src/main/objc/**/*', 'core-cocoa/src/main/swift/**/*', 'runtime-cocoa/src/gen/objc/**/*', 'runtime-cocoa/src/main/objc/**/*', 'runtime-cocoa/src/main/swift/**/*'
  s.resource = 'core/src/main/resources/*'

  s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/lib"', 'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/include" "$(PODS_ROOT)/../../core-cocoa/src/gen/objc/" "$(PODS_ROOT)/../../core-cocoa/src/main/objc/" "$(PODS_ROOT)/../../runtime-cocoa/src/main/objc/" "$(PODS_ROOT)/../../runtime-cocoa/src/gen/objc/"', 'OTHER_LDFLAGS' => '"-ObjC" "-force_load $(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/lib/libjre_emul.a"' }  

end