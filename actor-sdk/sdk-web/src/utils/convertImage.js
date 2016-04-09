/* global WebPDecoder */
import { memoize } from 'lodash';

function loadImage(src) {
  return new Promise(function (resolve, reject) {
    var xhr = new XMLHttpRequest();
    if (xhr.overrideMimeType) {
      xhr.overrideMimeType('text/plain; charset=x-user-defined');
    } else {
      xhr.setRequestHeader('Accept-Charset', 'x-user-defined');
    }

    xhr.addEventListener('error', reject);
    xhr.addEventListener('load', function () {
      var binary = xhr.responseText.split('').map(function (e) {
        return String.fromCharCode(e.charCodeAt(0) & 0xff);
      }).join('');
      resolve(binary);
    });

    xhr.open('GET', src);
    xhr.send();
  });
}

function binaryToArray(binary) {
  var result = new Array();
  for (var i = 0; i < binary.length; i++) {
    result.push(binary.charCodeAt(i));
  }

  return result;
}

function convertWebPText(data) {
  var buff = binaryToArray(data);

  var decoder = new WebPDecoder();
  var config = decoder.WebPDecoderConfig;
  var output_buffer = config.j;
  var bitstream = config.input;

  if (!decoder.WebPInitDecoderConfig(config)) {
    throw new Error('Library version mismatch!');
  }

  var status = decoder.WebPGetFeatures(buff, buff.length, bitstream);
  if (status !== 0) {
    throw new Error('Unable to decode webp image!', status);
  }

  output_buffer.J = 4;
  status = decoder.WebPDecode(buff, buff.length, config);

  if (status != 0) {
    throw new Error('WebP decoding failed.', status);
  }

  return {
    bitmap: output_buffer.c.RGBA.ma,
    height: output_buffer.height,
    width: output_buffer.width
  };
}

var canvas = document.createElement('canvas');
var context = canvas.getContext('2d');

function convertWebPToPNG(src) {
  return loadImage(src).then(function (data) {
    const webp = convertWebPText(data);
    canvas.height = webp.height;
    canvas.width = webp.width;

    var output = context.createImageData(canvas.width, canvas.height);
    var outputData = output.data;

    for (var h = 0; h < webp.height; h++) {
      for (var w = 0; w < webp.width; w++) {
        outputData[0 + w * 4 + webp.width * 4 * h] = webp.bitmap[1 + w * 4 + webp.width * 4 * h];
        outputData[1 + w * 4 + webp.width * 4 * h] = webp.bitmap[2 + w * 4 + webp.width * 4 * h];
        outputData[2 + w * 4 + webp.width * 4 * h] = webp.bitmap[3 + w * 4 + webp.width * 4 * h];
        outputData[3 + w * 4 + webp.width * 4 * h] = webp.bitmap[0 + w * 4 + webp.width * 4 * h];
      }
    }

    context.putImageData(output, 0, 0);
    return canvas.toDataURL();
  })
}

const isWebPSupported = new Promise((resolve) => {
  var image = new Image();
  image.onload = () => resolve(image.width === 2 && image.height === 1);
  image.onerror = () => resolve(false);
  image.src = 'data:image/webp;base64,UklGRjIAAABXRUJQVlA4ICYAAACyAgCdASoCAAEALmk0mk0iIiIiIgBoSygABc6zbAAA/v56QAAAAA==';
}).then((isSupported) => {
  if (isSupported) {
    return true;
  }

  return new Promise((resolve) => {
    require.ensure(['../vendor/libwebp-0.2.0.min'], (require) => {
      require('../vendor/libwebp-0.2.0.min');
      resolve(false);
    });
  });
})

function convertImage(src) {
  return isWebPSupported.then((isSupported) => {
    if (isSupported || !/\.webp\?/.test(src)) {
      return src;
    }

    return convertWebPToPNG(src);
  });
}

export default memoize(convertImage);
