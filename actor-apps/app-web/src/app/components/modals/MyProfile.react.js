import _ from 'lodash';

import React from 'react';

import MyProfileActions from '../../actions/MyProfileActions';
import MyProfileStore from '../../stores/MyProfileStore';

import AvatarItem from '../common/AvatarItem.react';

import Modal from 'react-modal';
import classNames from 'classnames';

const getStateFromStores = () => {
  return {
    profile: MyProfileStore.getProfile(),
    name: MyProfileStore.getName(),
    isOpen: MyProfileStore.isModalOpen(),
    isNameEditable: false
  };
};

class MyProfile extends React.Component {
  componentWillMount() {
    this.unsubscribe = MyProfileStore.listen(this.onChange);
  }

  componentWillUnmount() {
    this.unsubscribe();
  }

  componentDidUpdate() {
    if (this.state.isNameEditable) {
      let nameInput = React.findDOMNode(this.refs.myName);

      nameInput.addEventListener('focus', (event) => {
        event.target.select();
      });

      setTimeout(() => {
        nameInput.focus(function() {
          console.warn('asdasd');
        });
      });
    }
  }

  constructor() {
    super();

    this.onClose = this.onClose.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onNameEdit = this.onNameEdit.bind(this);
    this.onNameSave = this.onNameSave.bind(this);
    this.onNameChange = this.onNameChange.bind(this);

    this.state = getStateFromStores();
  }

  onClose() {
    MyProfileActions.modalClose();
  }

  onChange() {
    this.setState(getStateFromStores());
  }

  onNameEdit() {
    this.setState({isNameEditable: true});
  }

  onNameChange(event) {
    this.setState({name: event.target.value});
  }

  onNameSave() {
    this.setState({isNameEditable: false});
    MyProfileActions.setName(this.state.name);
  }

  render() {
    let isOpen = this.state.isOpen;
    let profile = this.state.profile;
    let isNameEditable = this.state.isNameEditable;

    if (profile !== null && isOpen === true) {
      let name = this.state.name;

      let phones = _.map(profile.phones, (phone, i) => {
        //return (
        //  <div className="phone" key={i}>+{phone.number}</div>
        //);
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

      let myNameClassName = classNames('name-block', 'row', {
        'name-block--editable': isNameEditable
      });

      //return (
      //  <Modal className="modal-new modal-new--profile"
      //         closeTimeoutMS={150}
      //         isOpen={isOpen}>
      //    <header className="modal-new__header">
      //      <a className="modal-new__header__close material-icons" onClick={this.onClose}>clear</a>
      //      <h4 className="modal-new__header__title">Profile</h4>
      //    </header>
      //    <div className="modal-new__body row">
      //      <AvatarItem image={profile.bigAvatar}
      //                  placeholder={profile.placeholder}
      //                  title={profile.name}/>
      //      <div className="col-xs">
      //        <div className="name">{name}</div>
      //        <div className="phone">{phones}</div>
      //        <ul className="modal-new__body__list">
      //          <li>
      //            <a>
      //              Send message
      //            </a>
      //          </li>
      //          <li>
      //            <a className="color--red">
      //              Block user
      //            </a>
      //          </li>
      //        </ul>
      //      </div>
      //    </div>
      //  </Modal>
      //);

       return (
         <Modal className="modal modal--profile profile"
                closeTimeoutMS={150}
                isOpen={isOpen}>

           <a className="modal__header__close material-icons" onClick={this.onClose}>clear</a>

           <div className="modal__body">

             <div className="myprofile__image">
               <AvatarItem image={profile.bigAvatar}
                           placeholder={profile.placeholder}
                           size="huge"
                           title={profile.name}/>
             </div>

             <h3 className={myNameClassName}>
               <a className="name-block__edit material-icons" onClick={this.onNameEdit}>mode_edit</a>
               <a className="name-block__save material-icons" onClick={this.onNameSave}>check_circle</a>
               <span className="name-block__name col-xs">
                 <span>{name}</span>
                 <input onChange={this.onNameChange} ref="myName" type="text" value={name} />
               </span>
             </h3>

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
}

export default MyProfile;
