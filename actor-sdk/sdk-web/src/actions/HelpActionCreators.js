/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import ActorClient from '../utils/ActorClient';
import { helpPhone } from '../constants/ActorAppConstants';
import SharedContainer from '../utils/SharedContainer';
import ContactActionCreators from './ContactActionCreators';
import DialogActionCreators from './DialogActionCreators';

export default {
  open() {
    const SharedActor = SharedContainer.get();
    const phone = SharedActor.helpPhone ? SharedActor.helpPhone : helpPhone;

    const handleFind = users => {
      if (users.length > 0) {
        const user = users[0].id;
        const uid = user;
        const userPeer = ActorClient.getUserPeer(uid);

        if (user.isContact) {
          DialogActionCreators.selectDialogPeer(userPeer);
        } else {
          ContactActionCreators.addContact(uid);
          DialogActionCreators.selectDialogPeer(userPeer);
        }
      } else {
        console.warn('Support user not found.')
      }
    };

    ActorClient.findUsers(phone)
      .then(handleFind, handleFind)
      .catch(error => {
        throw new Error(error)
      });
  }
};
