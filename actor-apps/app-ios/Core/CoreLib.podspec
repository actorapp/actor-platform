Pod::Spec.new do |s|
  s.name         = "CoreLib"
  s.version      = '0.9.7'
  s.summary      = "J2ObjC's JRE emulation library, emulates a subset of the Java runtime library."
  s.authors      = "Steve Kite"

  s.ios.deployment_target = '7.0'
  s.frameworks = 'Security'
  s.osx.deployment_target = '10.7'
  s.osx.frameworks = 'ExceptionHandling'
  s.requires_arc = true
  
  # s.header_mappings_dir = 'core/gen/'
  # s.preserve_paths = 'core/gen/**/*', 'runtime/gen/**/*'

  s.public_header_files = 'core/main/**/*.h', 'runtime/main/**/*.h'
  s.private_header_files = 'core/gen/**/*.h', 'runtime/gen/**/*.h'

  s.libraries = 'jre_emul', 'z'

  s.source_files = 'core/gen/**/*', 'runtime/gen/**/*'

  s.prepare_command = <<-CMD
    # rm -rf "j2objc"
    # rm -rf "core"
    # rm -rf "runtime"
    # ln -s ../../build-tools/dist/j2objc-0.9.7 ./j2objc
    # ln -s ../../core-cocoa/src/ ./core
    # ln -s ../../runtime-cocoa/src/ ./runtime
  CMD

  s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/lib"', 'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/include" "$(PODS_ROOT)/../Core/core/gen/" "$(PODS_ROOT)/../Core/core/main/" "$(PODS_ROOT)/../Core/runtime/gen/" "$(PODS_ROOT)/../Core/runtime/main/"' }  

  # s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/lib"', 'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/include" "$(PODS_ROOT)/../Core/core/main/objc/"' }  

end