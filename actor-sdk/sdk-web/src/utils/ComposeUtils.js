/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { KeyCodes } from '../constants/ActorAppConstants';

export function parseBotCommand(text) {
  const matches = /^\/(.)?(?: (.+))?/.exec(text);
  if (!matches) {
    return null;
  }

  return {
    name: matches[1],
    args: matches[2]
  };
}

function _parseMentionQuery(runText, query, position) {
  if (runText.length === 0) {
    return null;
  } else {
    const lastChar = runText.charAt(runText.length - 1);
    if (lastChar === '@') {
      const charBeforeAt = runText.charAt(runText.length - 2);
      if (charBeforeAt.trim() === '') {
        const text = (query || '');
        const atStart = text.length + 1 === position;

        return {
          text: text,
          atStart: atStart
        };
      } else {
        return null;
      }
    } else if (lastChar.trim() === '') {
      return null;
    } else {
      return _parseMentionQuery(runText.substring(0, runText.length - 1), lastChar + (query || ''), position);
    }
  }
}

export function parseMentionQuery(text, position) {
  return _parseMentionQuery(text.substring(0, position), null, position);
}

function reduceClipboardData(clipboardData) {
  const files = [];
  for (let i = 0; i < clipboardData.items.length; i++) {
    let item = clipboardData.items[i];
    if (item.type.indexOf('image') !== -1) {
      files.push(item.getAsFile());
    }
  }

  return files;
}

function imageToFile(img, callback) {
  img.onload = () => {
    let canvas = document.createElement('canvas');
    canvas.width = img.naturalWidth;
    canvas.height = img.naturalHeight;

    canvas.getContext('2d').drawImage(img, 0, 0);

    canvas.toBlob((blob) => {
      callback(blob);
      canvas = null;
      img.onload = null;
      img = null;
    });
  };
}

export function getClipboardImages(event, callback) {
  if (event.clipboardData.items) {
    callback(reduceClipboardData(event.clipboardData));
    return;
  }

  let input = event.target;
  let previousContent = input.innerText;
  setTimeout(() => {
    const childNode = input.childNodes[0];
    input.innerText = previousContent;

    if (childNode && childNode.tagName === 'IMG') {
      imageToFile(childNode, (blob) => {
        callback([blob]);
      });
    }
  }, 1);
}

export function isEditEvent(event) {
  return event.keyCode === KeyCodes.ARROW_UP && !event.target.innerText;
}

const IE = typeof document.selection !== 'undefined' && document.selection.type !== 'Control';
const W3 = typeof window.getSelection !== 'undefined';

export function getCaretPosition(area) {
  if (W3) {
    const range = window.getSelection().getRangeAt(0);
    const preCaretRange = range.cloneRange();
    preCaretRange.selectNodeContents(area);
    preCaretRange.setEnd(range.endContainer, range.endOffset);
    return preCaretRange.toString().length;
  }

  if (IE) {
    const textRange = document.selection.createRange();
    const preCaretTextRange = document.body.createTextRange();
    preCaretTextRange.moveToElementText(area);
    preCaretTextRange.setEndPoint('EndToEnd', textRange);
    return preCaretTextRange.text.length;
  }

  return 0;
}

const CR = typeof window.getSelection !== 'undefined' && typeof document.createRange !== 'undefined';
const CTR = typeof document.body.createTextRange !== 'undefined';

export function setCaretToEnd(area) {
  area.focus();
  if (CR) {
    const range = document.createRange();
    range.selectNodeContents(area);
    range.collapse(false);
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
  } else if (CTR) {
    const textRange = document.body.createTextRange();
    textRange.moveToElementText(area);
    textRange.collapse(false);
    textRange.select();
  }
}
