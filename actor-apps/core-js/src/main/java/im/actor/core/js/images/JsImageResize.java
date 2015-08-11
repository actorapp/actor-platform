/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.images;

import im.actor.runtime.js.fs.JsBlob;

public class JsImageResize {
    public static native void resize(JsBlob file, JsResizeListener resizeListener)/*-{
        var img = document.createElement("img");
        img.style.cssText = "image-orientation: from-image;"

        var reader = new FileReader();
        reader.onload = function(e) {
            img.src = e.target.result
            var canvas = document.createElement("canvas");

            var MAX_WIDTH = 90;
            var MAX_HEIGHT = 90;
            var width = img.width;
            var height = img.height;

            if (width > height) {
                if (width > MAX_WIDTH) {
                    height *= MAX_WIDTH / width;
                    width = MAX_WIDTH;
                }
            } else {
                if (height > MAX_HEIGHT) {
                    width *= MAX_HEIGHT / height;
                    height = MAX_HEIGHT;
                }
            }
            canvas.width = width;
            canvas.height = height;

            var ctx = canvas.getContext("2d");

            ctx.fillStyle = 'white';
            ctx.fillRect(0, 0, width, height);

            ctx.drawImage(img, 0, 0, width, height);

            var compressedImage = canvas.toDataURL("image/jpeg", 0.55);

            resizeListener.@im.actor.core.js.images.JsResizeListener::onResized(*)(compressedImage,
                  width, height, img.width, img.height);
        }
        reader.readAsDataURL(file);
    }-*/;
}
