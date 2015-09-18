echo "Building OSX"
electron-packager src Actor --platform=darwin --arch=x64 --app-bundle-id=im.actor.app --version=0.33.0 --out=build --icon=icon/osx.icns

echo "Building Windows"
electron-packager src Actor --platform=win32 --arch=all --app-bundle-id=im.actor.app --version=0.33.0 --out=build --icon=icon/windows.icns

echo "Building Linux"
electron-packager src Actor --platform=linux --arch=all --app-bundle-id=im.actor.app --version=0.33.0 --out=build
