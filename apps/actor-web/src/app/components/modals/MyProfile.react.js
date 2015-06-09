import _ from 'lodash';

import React from 'react';

import MyProfileActions from '../../actions/MyProfileActions';
import MyProfileStore from '../../stores/MyProfileStore';

import AvatarItem from '../common/AvatarItem.react';

import Modal from 'react-modal';

const getStateFromStores = function() {
  return {
    profile: MyProfileStore.getProfile(),
    isOpen: MyProfileStore.isModalOpen()
  };
};

export default class extends React.Component {
  constructor() {
    super();

    this._onClose = this._onClose.bind(this);
    this._onChange = this._onChange.bind(this);
    this.state = getStateFromStores();
  }

  componentWillMount() {
    this.unsubscribe = MyProfileStore.listen(this._onChange);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  render() {
    let profile = this.state.profile;
    let isOpen = this.state.isOpen;

    if (profile !== null) {

      let phones = _.map(profile.phones, function(phone, i) {
        return (
          <li className="profile__list__item row" key={i}>
            <i className="material-icons">call</i>
            <div className="col-xs">
              <span className="contact">+{phone.number}</span>
              <span className="title">{phone.title}</span>
            </div>
          </li>
        );
      });

      console.warn(profile);
      return (
        <Modal className="modal modal--profile profile"
               closeTimeoutMS={150}
               isOpen={isOpen}>

          <div className="modal__body">
            <a className="modal__header__close material-icons" onClick={this._onClose}>clear</a>

            <AvatarItem image={profile.bigAvatar}
                        placeholder={profile.placeholder}
                        size="huge"
                        title={profile.name}/>

            <h3 className="name">{profile.name}</h3>

            <ul className="profile__list profile__list--contacts">
              {phones}
            </ul>
          </div>
        </Modal>
      );
    } else {
      return null;
    }
  }

  _onClose() {
    MyProfileActions.modalClose();
  }

  _onChange() {
    this.setState(getStateFromStores());
  }
}
