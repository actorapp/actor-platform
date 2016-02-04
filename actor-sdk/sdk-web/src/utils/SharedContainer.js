/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

let _sharedActor = null;

export default {
  set: (shared) => _sharedActor = shared,
  get: () => _sharedActor
}
