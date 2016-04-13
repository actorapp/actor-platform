/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

class ActionCreators {
  constructor() {
    this.bindings = new Map();
  }

  setBindings(namespace, bindings) {
    if (this.bindings.has(namespace)) {
      console.error('You are trying to set bindings "%s#%s" before it was removed', this.constructor.name, namespace);
    } else {
      this.bindings.set(namespace, bindings)
    }
  }

  removeBindings(namespace) {
    const bindings = this.bindings.get(namespace);
    if (bindings) {
      for (let i = 0; i < bindings.length; i++) {
        bindings[i].unbind();
        bindings[i] = null;
      }

      this.bindings.delete(namespace);
    } else {
      console.warn('You are trying to remove bindings "%s#%s" before it was set', this.constructor.name, namespace);
    }
  }
}

export default ActionCreators;
