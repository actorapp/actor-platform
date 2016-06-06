/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */
import '../vendor/canvasBlurRect';
import Lightbox from 'jsonlylightbox';

const lightbox = new Lightbox();

const dataURItoBlob = (dataURI) => {
  const byteString = atob(dataURI.split(',')[1]);
  const mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
  const buffer = new ArrayBuffer(byteString.length);

  let view = new Uint8Array(buffer);
  for (let i in byteString) {
    view[i] = byteString.charCodeAt(i);
  }

  return new Blob([view], { type: mimeString });
};

export {
  lightbox,
  dataURItoBlob
};

export function loadImage(source) {
  return new Promise((resolve, reject) => {
    const image = document.createElement('img')
    image.onerror = reject;
    image.onload = () => {
      image.onerror = null;
      image.onload = null;
      resolve(image);
    };

    image.setAttribute('crossOrigin', 'anonymous');
    image.src = source;
  });
}

export function renderImageToCanvas(source, canvas) {
  return loadImage(source).then((image) => {
    const width = canvas.width = image.width;
    const height = canvas.height = image.height;

    const ctx = canvas.getContext('2d');
    ctx.drawImage(image, 0, 0, width, height);
    ctx._blurRect(0, 0, width, height, 4, 1);
  });
}

export function getDimentions(width, height, maxWidth = 300, maxHeight = 400) {
  if (width > height) {
    if (width > maxWidth) {
      return {
        width: maxWidth,
        height: height * (maxWidth / width)
      };
    }
  } else if (height > maxHeight) {
    return {
      width: width * (maxHeight / height),
      height: maxHeight
    };
  }

  return { width, height };
}
