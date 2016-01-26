/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from '../utils/ActorClient';
import { Support } from '../constants/ActorAppConstants';
import ContactActionCreators from './ContactActionCreators';
import DialogActionCreators from './DialogActionCreators';

export default {
  open() {
    ActorClient.findUsers(Support.phone)
      .then(users => {
        if (users.length > 0) {
          const user = users[0];
          const uid = user.id;
          const userPeer = ActorClient.getUserPeer(uid);

          if (user.isContact) {
            DialogActionCreators.selectDialogPeer(userPeer);
          } else {
            ContactActionCreators.addContact(uid);
            DialogActionCreators.selectDialogPeer(userPeer);
          }
        }
      }).catch(error => {
        throw new Error(error);
      });
  }
};
