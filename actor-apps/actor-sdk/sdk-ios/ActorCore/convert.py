import sys
import os

added = set()

if not os.path.exists('Public'):
    os.makedirs('Public')

for root, directories, filenames in os.walk('Sources/'):
    for filename in filenames:

        # Matching only header files
        if not filename.endswith(".h"):
            continue

        # File logic path like im/actor/core/Messenger.h
        path = os.path.join(root[8:], filename)
        # Full directory of file like im/actor/core
        dirName = os.path.split(os.path.join(root, filename))[0][8:] + '/'
        # Source file full path like Sources/actor/core/Messenger.h
        srcFile = os.path.join(root, filename)
        # Converted File path like Sources2/actor/core/Messenger.h
        destFile = os.path.join('Public', path)

        # Auto Create directory
        if not os.path.exists(os.path.dirname(destFile)):
            os.makedirs(os.path.dirname(destFile))
                
        with open(srcFile, 'r') as f:

            with open(destFile, 'w') as d:
                for line in f:
                    if (line.startswith("#include") or line.startswith("#import")) and '\"' in line:

                        start = line.index('\"')
                        end = line.index('\"', start + 1)
                        includedFile = line[start + 1:end]

                        if includedFile != 'objc/runtime.h':
                            localIncludePath = os.path.join("Sources/", includedFile)
                            if os.path.exists(localIncludePath):
                                line = line[0:start] + "<ActorCore/" + includedFile + ">" + line[end+1:]
                            else:
                                line = line[0:start] + "<j2objc/" + includedFile + ">" + line[end+1:]

                    d.write(line)
