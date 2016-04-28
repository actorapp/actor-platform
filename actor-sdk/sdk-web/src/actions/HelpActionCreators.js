/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import ActorClient from '../utils/ActorClient';
import { helpPhone } from '../constants/ActorAppConstants';
import SharedContainer from '../utils/SharedContainer';
import DialogActionCreators from './DialogActionCreators';

export default {
  handleFind(users) {
    if (users.lenght === 0) {
      throw new Error('Support user not found');
    }

    const helpUser = users[0];
    const uid = helpUser.id;

    if (!helpUser.isContact) {
      ActorClient.addContact(uid)
    }

    return uid;
  },

  open() {
    const SharedActor = SharedContainer.get();
    const phone = SharedActor.helpPhone ? SharedActor.helpPhone : helpPhone;

    ActorClient.findUsers(phone)
      .then(this.handleFind)
      .then(DialogActionCreators.selectDialogPeerUser)
      .catch(error => {throw new Error(error)});
  }
};
