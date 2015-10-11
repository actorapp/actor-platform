mkdir build/CocoaPod
mkdir build/CocoaPod/Sources

find ActorSDK/ -name "*.swift" -type file -exec cp {} build/CocoaPod/Sources \;
find ActorSDK/ -name "*.h" -type file -exec cp {} build/CocoaPod/Sources \;
find ActorSDK/ -name "*.m" -type file -exec cp {} build/CocoaPod/Sources \;