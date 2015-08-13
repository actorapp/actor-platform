Pod::Spec.new do |s|
  s.name         = "core"
  s.version      = '0.9.7'
  s.summary      = "J2ObjC's JRE emulation library, emulates a subset of the Java runtime library."
  s.authors      = "Steve Kite"

  s.ios.deployment_target = '7.0'
  s.frameworks = 'Security'
  s.osx.deployment_target = '10.7'
  s.osx.frameworks = 'ExceptionHandling'
  s.requires_arc = true
  
  s.header_mappings_dir = 'j2objc/j2objc-0.9.7/include'
  s.preserve_paths = '{j2objc,core,runtime}'
  s.libraries = 'jre_emul', 'z'

  s.source_files = 'core', 'runtime', 'core/gen/objc/**/*.{h,m}', 'core/main/objc/**/*.{h,m}', 'runtime/main/objc/**/*.{h,m}', 'runtime/gen/objc/**/*.{h,m}'

  s.prepare_command = <<-CMD
    // rm -rf "j2objc"
    // rm -rf "core"
    // rm -rf "runtime"
    // ln -s ../../build-tools/dist/j2objc-0.9.7 ./j2objc
    // ln -s ../../core-cocoa/src/ ./core
    // ln -s ../../runtime-cocoa/src/ ./runtime
  CMD

  s.xcconfig = { 'LIBRARY_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/lib"', \
      'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/../Core/j2objc/include" "$(PODS_ROOT)/../Core/core/gen/objc" "$(PODS_ROOT)/../Core/core/main/objc" "$(PODS_ROOT)/../Core/runtime/gen/objc" "$(PODS_ROOT)/../Core/runtime/main/objc"' }  
end