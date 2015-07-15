# !/bin/bash
set -e
IPA_NAME=$1
TMP_IPA="build/Applications/${IPA_NAME}_Temp"

# Create dest dir
if [ -d "${TMP_IPA}" ];
then
    rm -rf "${TMP_IPA}"
fi  
mkdir ${TMP_IPA}

# Unzip IPA
unzip -q "${IPA_NAME}" -d "${TMP_IPA}"

# Copy SwitfSupport
mkdir -p "${TEMP_IPA_BUILT}/SwiftSupport"
for SWIFT_LIB in $(ls -1 ${TMP_IPA}/Payload/Frameworks); do 
    cp "${TMP_IPA}/Payload/Frameworks/${SWIFT_LIB}" "${TMP_IPA}/SwiftSupport"
done

# Repack IPA
rm -rf "${IPA_NAME}"
echo "+ zip --symlinks --verbose --recurse-paths ${IPA} ."
cd "${TMP_IPA}"
zip --symlinks --verbose --recurse-paths "${IPA_NAME}" .