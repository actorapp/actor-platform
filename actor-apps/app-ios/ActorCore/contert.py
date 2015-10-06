import sys
import os

added = set()
with open('JActor.h', 'w') as umbrella:
    for root, directories, filenames in os.walk('Sources/'):
        for filename in filenames:

            # Matching only h and m files
            if not filename.endswith(".h") and not filename.endswith(".m"):
                 continue

            # File logic path like im/actor/core/Messenger.h
            path = os.path.join(root[8:], filename)
            # Full directory of file like im/actor/core
            dirName = os.path.split(os.path.join(root, filename))[0][8:] + '/'
            # Source file full path like Sources/actor/core/Messenger.h
            srcFile = os.path.join(root, filename)
            # Converted File path like Sources2/actor/core/Messenger.h
            destFile = os.path.join('Sources2', path)

            if not path.startswith('com/google/protobuf'):

                # Auto Create directory
                if not os.path.exists(os.path.dirname(destFile)):
                    os.makedirs(os.path.dirname(destFile))

                with open(srcFile, 'r') as f:

                    # if path not in added:
                    #     added.add(path)
                    #     umbrella.write("#include <j2objc/" + path + ">\n")

                    with open(destFile, 'w') as d:
                        for line in f:
                            if (line.startswith("#include") or line.startswith("#import")) and '\"' in line:

                                start = line.index('\"')
                                end = line.index('\"', start + 1)
                                includedFile = line[start + 1:end]

                                if includedFile != 'objc/runtime.h':

                                    # Hack for local include
                                    # if "/" not in includedFile:
                                    #     if os.path.exists(os.path.join(root, includedFile)):
                                    #         if dirName != '/':
                                    #             includedFile = dirName + includedFile
                                    localIncludePath = os.path.join("Sources/", includedFile)
                                    print localIncludePath
                                    if os.path.exists(localIncludePath):
                                        line = line[0:start] + "\"" + includedFile + "\"" + line[end+1:]
                                    else:
                                        line = line[0:start] + "<j2objc/" + includedFile + ">" + line[end+1:]

                            d.write(line)
