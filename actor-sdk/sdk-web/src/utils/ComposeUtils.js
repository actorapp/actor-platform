/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

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
