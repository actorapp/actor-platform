/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

export const dataURItoBlob = (dataURI) => {
  const byteString = atob(dataURI.split(',')[1]);
  const mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
  const buffer = new ArrayBuffer(byteString.length);

  let view = new Uint8Array(buffer);
  for (let i in byteString) {
    view[i] = byteString.charCodeAt(i);
  }

  return new Blob([view], {type: mimeString});
};
