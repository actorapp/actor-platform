Pod::Spec.new do |s|
  s.name         = "j2objc"
  s.version      = '0.9.7'
  s.summary      = "J2ObjC's JRE emulation library, emulates a subset of the Java runtime library."


  s.ios.deployment_target = '5.0'
  s.frameworks = 'Security'
  s.osx.deployment_target = '10.7'
  s.osx.frameworks = 'ExceptionHandling'
  s.requires_arc = true

  s.header_mappings_dir = 'dist/j2objc-0.9.7/include'
  s.preserve_paths = 'dist'
  s.libraries = 'jre_emul', 'z'

  s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/lib"', \
      'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../../build-tools/dist/j2objc-0.9.7/include"' }
      
end