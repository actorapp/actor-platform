/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

let _router = null;

export default {
  set: (router) => _router = router,
  get: () => _router
}
