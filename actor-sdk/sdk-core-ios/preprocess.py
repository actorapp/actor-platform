import sys
import os

if not os.path.exists('Public'):
    os.makedirs('Public')

if not os.path.exists('External'):
    os.makedirs('External')

for root, directories, filenames in os.walk('Sources/'):
    for filename in filenames:

        # Matching only header and m files
        if (not filename.lower().endswith(".h") and not filename.lower().endswith(".m")):
            continue

        # File logic path like im/actor/core/Messenger.h
        path = os.path.join(root[8:], filename)
        # Full directory of file like im/actor/core
        dirName = os.path.split(os.path.join(root, filename))[0][8:] + '/'
        # Source file full path like Sources/actor/core/Messenger.h
        srcFile = os.path.join(root, filename)
        # Converted File path like Sources2/actor/core/Messenger.h
        destFile = os.path.join('Public', path)
        externalFile = os.path.join('External', path)
        
        # Auto Create directory
        if not os.path.exists(os.path.dirname(destFile)):
            os.makedirs(os.path.dirname(destFile))
                
        if not os.path.exists(os.path.dirname(externalFile)):
            os.makedirs(os.path.dirname(externalFile))
        
        with open(srcFile, 'r') as f:
            
            allLines = f.read()
            destLines = ""
            
            with open(externalFile, 'w') as d:
                d.write(allLines)
            
            for line in allLines.splitlines():
                if (line.startswith("#include") or line.startswith("#import")) and '\"' in line:
                    start = line.index('\"')
                    end = line.index('\"', start + 1)
                    includedFile = line[start + 1:end]
                    if includedFile != 'objc/runtime.h':
                        localIncludePath = os.path.join("Sources/", includedFile)
                        if os.path.exists(localIncludePath):
                            line = line
                        # line = line[0:start] + "<ActorSDK/" + includedFile + ">" + line[end+1:]
                        # line = line[0:start] + "<ActorSDK/" + includedFile + ">" + line[end+1:]
                        else:
                            line = line[0:start] + "<j2objc/" + includedFile + ">" + line[end+1:]
        
                destLines = destLines + line + "\n"

            isUpdated = True

            if os.path.exists(destFile):
                with open(destFile, 'rw') as d:
                    if d.read() == destLines:
                        # print "Not Updated"
                        isUpdated = False

            if isUpdated:
                with open(destFile, 'w') as d:
                    d.write(destLines)
