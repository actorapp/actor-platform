# !/bin/bash
set -e
IPA_NAME=$1
APP_NAME=$2
TMP_IPA="build/IPA_Temp"
DEVELOPER_DIR=`xcode-select --print-path`

# Create dest dir
if [ -d "${TMP_IPA}" ];
then
    rm -rf "${TMP_IPA}"
fi
mkdir ${TMP_IPA}

# Unzip IPA
unzip -q "build/${IPA_NAME}" -d "${TMP_IPA}"

# Copy SwitfSupport
mkdir -p "${TMP_IPA}/SwiftSupport"
for SWIFT_LIB in $(ls -1 ${TMP_IPA}/Payload/${APP_NAME}.app/Frameworks); do
    cp "${DEVELOPER_DIR}/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphoneos/${SWIFT_LIB}" "${TMP_IPA}/SwiftSupport"
done

# Repack IPA
rm -rf "build/${IPA_NAME}"
echo "+ zip --symlinks --verbose --recurse-paths ${IPA} ."
cd "${TMP_IPA}"
zip --symlinks --verbose --recurse-paths "${IPA_NAME}" .
cd ../../

# Move to original path
mv "${TMP_IPA}/${IPA_NAME}" "build/${IPA_NAME}"
