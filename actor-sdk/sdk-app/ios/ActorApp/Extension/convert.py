import sys
import os
#
## print "Starting converting headers"
#
## Building umbrella paths
#
#def loadIncludes(fileName):
#    res = set()
#    umbrella = open(fileName, 'r').read()
#    for line in umbrella.splitlines():
#        if line.startswith("#include") and '\"' in line:
#            start = line.index('\"')
#            end = line.index('\"', start + 1)
#            includedFile = line[start + 1:end]
#            if includedFile != 'objc/runtime.h':
#                localIncludePath = os.path.join("Sources/", includedFile)
#                if os.path.exists(localIncludePath):
#                    res.add(includedFile.lower())
#        if line.startswith("#import <ActorSDK/"):
#            start = line.index('<') + 9
#            end = line.index('>', start + 1)
#            includedFile = line[start + 1:end]
#            res.add(includedFile.lower())
#    return res
#
## print "Building required includes"
#
#def processInclude(fileName, paths, level):
#    # print ('|' * level) + "Processing " + fileName
#    res = paths.copy()
#    includes = loadIncludes(fileName)
#    for inc in includes:
#        if inc not in res:
#            res.add(inc)
#            res = processInclude('Sources/' + inc, res, level + 1)
#    return res
#
#paths = loadIncludes(sys.argv[1])
#for p in paths.copy():
#    paths = paths.union(processInclude('Sources/' + p, paths.copy(), 0))
#
#if not os.path.exists('Public'):
#    os.makedirs('Public')

for root, directories, filenames in os.walk('Sources/'):
    for filename in filenames:

        # Matching only header files
        if not filename.lower().endswith(".h"):
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
            
            allLines = f.read()
            destLines = ""
            
            for line in allLines.splitlines():
                if (line.startswith("#include") or line.startswith("#import")) and '\"' in line:
                    start = line.index('\"')
                    end = line.index('\"', start + 1)
                    includedFile = line[start + 1:end]
                    if includedFile != 'objc/runtime.h':
                        localIncludePath = os.path.join("Sources/", includedFile)
                        if os.path.exists(localIncludePath):
                            line = line[0:start] + "\"" + includedFile + "\"" + line[end+1:]
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


