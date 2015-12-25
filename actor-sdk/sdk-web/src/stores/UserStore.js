/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from '../utils/ActorClient';

const UserStore = {
  /**
   * Get user information
   *
   * @param uid {number} User id
   * @returns {object} User information
   */
  getUser(uid) {
    return ActorClient.getUser(uid);
  },

  /**
   * Get current user id
   *
   * @returns {number} User id
   */
  getMyId() {
    return ActorClient.getUid();
  },

  /**
   * Returns true if user is in contact
   *
   * @param uid {number} User id
   * @returns {boolean}
   */
  isContact(uid) {
    return this.getUser(uid).isContact
  }
};

export default UserStore;
