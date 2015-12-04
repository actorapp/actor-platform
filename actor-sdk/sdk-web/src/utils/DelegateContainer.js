/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

let _delegate = null;

export default {
  set: (delegate) => _delegate = delegate,
  get: () => _delegate
}
