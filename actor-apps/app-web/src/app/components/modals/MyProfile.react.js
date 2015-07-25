//import _ from 'lodash';

import React from 'react';

import { KeyCodes } from 'constants/ActorAppConstants';

import MyProfileActions from 'actions/MyProfileActions';
import MyProfileStore from 'stores/MyProfileStore';

import AvatarItem from 'components/common/AvatarItem.react';

import Modal from 'react-modal';
//import classNames from 'classnames';
import { Styles, TextField } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';

const ThemeManager = new Styles.ThemeManager();


const getStateFromStores = () => {
  return {
    profile: MyProfileStore.getProfile(),
    name: MyProfileStore.getName(),
    isOpen: MyProfileStore.isModalOpen(),
    isNameEditable: false
  };
};

class MyProfile extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  componentWillUnmount() {
    this.unsubscribe();
    document.removeEventListener('keydown', this.onKeyDown, false);
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

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    this.unsubscribe = MyProfileStore.listen(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      textField: {
        textColor: 'rgba(0,0,0,.87)',
        focusColor: '#68a3e7',
        backgroundColor: 'transparent',
        borderColor: '#68a3e7',
        disabledTextColor: 'rgba(0,0,0,.4)'
      }
    });
  }

  onClose = () => {
    MyProfileActions.modalClose();
  }

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  }

  onChange = () => {
    this.setState(getStateFromStores());
  }

  onNameEdit = () => {
    this.setState({isNameEditable: true});
  }

  onNameChange = event => {
    this.setState({name: event.target.value});
  }

  onNameKeyDown = event => {
    if (event.keyCode === KeyCodes.ENTER) {
      event.preventDefault();
      this.onNameSave();
    }
  }

  onNameSave = () => {
    //this.setState({isNameEditable: false});
    MyProfileActions.setName(this.state.name);
  }

  render() {
    let isOpen = this.state.isOpen;
    let profile = this.state.profile;
    //let isNameEditable = this.state.isNameEditable;

    if (profile !== null && isOpen === true) {
      //let name = this.state.name;

      //let phones = _.map(profile.phones, (phone, i) => {
      //  return (
      //    <div className="phone" key={i}>+{phone.number}</div>
      //  );
        //return (
        //  <li className="profile__list__item row" key={i}>
        //    <i className="material-icons">call</i>
        //    <div className="col-xs">
        //      <span className="contact">+{phone.number}</span>
        //      <span className="title">{phone.title}</span>
        //    </div>
        //  </li>
        //);
      //});

      //let myNameClassName = classNames('name-block', 'row', {
      //  'name-block--editable': isNameEditable
      //});

      return (
        <Modal className="modal-new modal-new--profile"
               closeTimeoutMS={150}
               isOpen={isOpen}
               style={{width: 340}}>
          <header className="modal-new__header">
            <a className="modal-new__header__close material-icons" onClick={this.onClose}>clear</a>
            <h4 className="modal-new__header__title">Profile</h4>
          </header>
          <div className="modal-new__body row">
            <AvatarItem image={profile.bigAvatar}
                        placeholder={profile.placeholder}
                        size="big"
                        title={profile.name}/>
            <div className="col-xs">
              <div className="name">
                <TextField className="login__form__input"
                           floatingLabelText="Username"
                           fullWidth
                           onBlur={this.onNameSave}
                           onChange={this.onNameChange}
                           onKeyDown={this.onNameKeyDown}
                           type="text"
                           value={this.state.name}/>
              </div>
              <div className="phone">
                <TextField className="login__form__input"
                           disabled
                           floatingLabelText="Phone number"
                           fullWidth
                           type="tel"
                           value={this.state.profile.phones[0].number}/>
              </div>
              <ul className="modal-new__body__list hide">
                <li>
                  <a>
                    Send message
                  </a>
                </li>
                <li>
                  <a className="color--red">
                    Block user
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </Modal>
      );

       //return (
       //  <Modal className="modal modal--profile profile"
       //         closeTimeoutMS={150}
       //         isOpen={isOpen}>
       //
       //    <a className="modal__header__close material-icons" onClick={this.onClose}>clear</a>
       //
       //    <div className="modal__body">
       //
       //      <div className="myprofile__image">
       //        <AvatarItem image={profile.bigAvatar}
       //                    placeholder={profile.placeholder}
       //                    size="huge"
       //                    title={profile.name}/>
       //      </div>
       //
       //      <h3 className={myNameClassName}>
       //        <a className="name-block__edit material-icons" onClick={this.onNameEdit}>mode_edit</a>
       //        <a className="name-block__save material-icons" onClick={this.onNameSave}>check_circle</a>
       //        <span className="name-block__name col-xs">
       //          <span>{name}</span>
       //          <input onChange={this.onNameChange} ref="myName" type="text" value={name} />
       //        </span>
       //      </h3>
       //
       //      <ul className="profile__list profile__list--contacts">
       //        {phones}
       //      </ul>
       //    </div>
       //  </Modal>
       //);
    } else {
      return null;
    }
  }
}

export default MyProfile;
