// https://github.com/keithwhor/canvasBlurRect/blob/master/LICENSE
// The MIT License (MIT)
//
// Copyright (c) 2015 Keith Horwood
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

!function(window) {

  'use strict';

  var blurCanvas = document.createElement('canvas');
  blurCanvas.width = screen.width;
  blurCanvas.height = screen.height;
  var blurCtx = blurCanvas.getContext('2d');

  function saturate(src, w, h, sv) {

    var len = w * h;
    var pos, i, j, offset;

    var lumR = (1 - sv) * 0.3086;
    var lumG = (1 - sv) * 0.6094;
    var lumB = (1 - sv) * 0.0820;

    var r, g, b;

    var shiftW = w << 2;

    for(j = 0; j < h; j++) {

      offset = j * shiftW;

      for(i = 0; i < w; i++) {

        pos = offset + (i << 2);

        r = src[pos];
        g = src[pos + 1];
        b = src[pos + 2];

        src[pos] = ((lumR + sv) * r) +
          (lumG * g) +
          (lumB * b);

        src[pos + 1] = (lumR * r) +
          ((lumG + sv) * g) +
          (lumB * b);

        src[pos + 2] = (lumR * r) +
          (lumG * g) +
          ((lumB + sv) * b);

      }

    }

  };

  function boxBlur(src, w, h, r, sv) {
    var tmp = new Uint8Array(w * h * 4);
    blurRight(src, tmp, w, h, r);
    blurDown(tmp, src, w, h, r);
    blurLeft(src, tmp, w, h, r);
    blurUp(tmp, src, w, h, r);
    (sv !== undefined && sv !== 1) && saturate(src, w, h, sv);
  };

  function blurRight(src, dest, w, h, r) {

    var i, j, offset, pos, posR;

    var shiftR = r << 2;
    var shiftW = w << 2;

    var weightR, weightG, weightB, weightA;

    for(j = 0; j < h; j++) {

      weightR = 0;
      weightG = 0;
      weightB = 0;
      weightA = 0;

      offset = j * shiftW;

      for(i = 0; i < r; i++) {

        pos = offset + (i << 2);

        weightR += src[pos];
        weightG += src[pos + 1];
        weightB += src[pos + 2];
        weightA += src[pos + 3];

        dest[pos] = (weightR / (i + 1)) | 0;
        dest[pos + 1] = (weightG / (i + 1)) | 0;
        dest[pos + 2] = (weightB / (i + 1)) | 0;
        dest[pos + 3] = (weightA / (i + 1)) | 0;

      }

      for(; i < w; i++) {

        pos = offset + (i << 2);
        posR = pos - shiftR;

        dest[pos] = (weightR / r) | 0;
        dest[pos + 1] = (weightG / r) | 0;
        dest[pos + 2] = (weightB / r) | 0;
        dest[pos + 3] = (weightA / r) | 0;

        weightR += src[pos] - src[posR];
        weightG += src[pos + 1] - src[posR + 1];
        weightB += src[pos + 2] - src[posR + 2];
        weightA += src[pos + 3] - src[posR + 3];

      }

    }

  };

  function blurLeft(src, dest, w, h, r) {

    var i, j, offset, pos, posR;

    var shiftR = r << 2;
    var shiftW = w << 2;

    var weightR, weightG, weightB, weightA;

    for(j = 0; j < h; j++) {

      weightR = 0;
      weightG = 0;
      weightB = 0;
      weightA = 0;

      offset = j * shiftW;

      for(i = w - 1; i >= w - r; i--) {

        pos = offset + (i << 2);

        weightR += src[pos];
        weightG += src[pos + 1];
        weightB += src[pos + 2];
        weightA += src[pos + 3];

        dest[pos] = (weightR / (w - i)) | 0;
        dest[pos + 1] = (weightG / (w - i)) | 0;
        dest[pos + 2] = (weightB / (w - i)) | 0;
        dest[pos + 3] = (weightA / (w - i)) | 0;

      }

      for(; i >= 0; i--) {

        pos = offset + (i << 2);
        posR = pos + shiftR;

        dest[pos] = (weightR / r) | 0;
        dest[pos + 1] = (weightG / r) | 0;
        dest[pos + 2] = (weightB / r) | 0;
        dest[pos + 3] = (weightA / r) | 0;

        weightR += src[pos] - src[posR];
        weightG += src[pos + 1] - src[posR + 1];
        weightB += src[pos + 2] - src[posR + 2];
        weightA += src[pos + 3] - src[posR + 3];

      }

    }

  };

  function blurDown(src, dest, w, h, r) {

    var i, j, offset, pos, posR;

    var shiftR = r << 2;
    var shiftW = w << 2;

    var offsetR = shiftW * r;

    var weightR, weightG, weightB, weightA;

    for(i = 0; i < w; i++) {

      weightR = 0;
      weightG = 0;
      weightB = 0;
      weightA = 0;

      offset = i << 2;

      for(j = 0; j < r; j++) {

        pos = offset + (j * shiftW);

        weightR += src[pos];
        weightG += src[pos + 1];
        weightB += src[pos + 2];
        weightA += src[pos + 3];

        dest[pos] = (weightR / (j + 1)) | 0;
        dest[pos + 1] = (weightG / (j + 1)) | 0;
        dest[pos + 2] = (weightB / (j + 1)) | 0;
        dest[pos + 3] = (weightA / (j + 1)) | 0;

      }

      for(; j < h; j++) {

        pos = offset + (j * shiftW);
        posR = pos - offsetR;

        dest[pos] = (weightR / r) | 0;
        dest[pos + 1] = (weightG / r) | 0;
        dest[pos + 2] = (weightB / r) | 0;
        dest[pos + 3] = (weightA / r) | 0;

        weightR += src[pos] - src[posR];
        weightG += src[pos + 1] - src[posR + 1];
        weightB += src[pos + 2] - src[posR + 2];
        weightA += src[pos + 3] - src[posR + 3];

      }

    }

  };

  function blurUp(src, dest, w, h, r) {

    var i, j, offset, pos, posR;

    var shiftR = r << 2;
    var shiftW = w << 2;

    var offsetR = shiftW * r;

    var weightR, weightG, weightB, weightA;

    for(i = 0; i < w; i++) {

      weightR = 0;
      weightG = 0;
      weightB = 0;
      weightA = 0;

      offset = i << 2;

      for(j = h - 1; j >= h - r; j--) {

        pos = offset + (j * shiftW);

        weightR += src[pos];
        weightG += src[pos + 1];
        weightB += src[pos + 2];
        weightA += src[pos + 3];

        dest[pos] = (weightR / (h - j)) | 0;
        dest[pos + 1] = (weightG / (h - j)) | 0;
        dest[pos + 2] = (weightB / (h - j)) | 0;
        dest[pos + 3] = (weightA / (h - j)) | 0;

      }

      for(; j >= 0; j--) {

        pos = offset + (j * shiftW);
        posR = pos + offsetR;

        dest[pos] = (weightR / r) | 0;
        dest[pos + 1] = (weightG / r) | 0;
        dest[pos + 2] = (weightB / r) | 0;
        dest[pos + 3] = (weightA / r) | 0;

        weightR += src[pos] - src[posR];
        weightG += src[pos + 1] - src[posR + 1];
        weightB += src[pos + 2] - src[posR + 2];
        weightA += src[pos + 3] - src[posR + 3];

      }

    }

  };

  function blurRect(x, y, w, h, r, sv) {

    var ctx = this;
    var canvas = ctx.canvas;

    var srcW = w | 0;
    var srcH = h | 0;

    var srcX = x | 0;
    var srcY = y | 0;

    // r = (r | 0) * 32;
    // r = Math.min(Math.max(r, 32), 256);

    var resizeFactor = Math.max(0, ((Math.log(r) / Math.log(2)) - 3) | 0);
    var radius = r >>> resizeFactor;

    var resizeWidth = canvas.width >>> resizeFactor;
    var resizeHeight = canvas.height >>> resizeFactor;

    blurCtx.drawImage(canvas, 0, 0, resizeWidth, resizeHeight);
    var imageData = blurCtx.getImageData(0, 0, resizeWidth, resizeHeight);

    boxBlur(imageData.data, resizeWidth, resizeHeight, radius, sv);

    blurCtx.putImageData(imageData, 0, 0);

    blurCtx.drawImage(
      blurCanvas,
      0, 0,
      resizeWidth, resizeHeight,
      0, 0,
      canvas.width, canvas.height
    );

    ctx.drawImage(
      blurCanvas,
      srcX, srcY,
      srcW, srcH,
      srcX, srcY,
      srcW, srcH
    );

    return ctx;

  };

  window.CanvasRenderingContext2D.prototype._blurRect = blurRect;

}(window);
