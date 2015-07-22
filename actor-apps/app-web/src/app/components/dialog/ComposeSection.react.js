import _ from 'lodash';

import React from 'react';
//import ReactMixin from 'react-mixin';
//import { PureRenderMixin } from 'react/addons';
import ActorClient from 'utils/ActorClient';
import { Styles, FlatButton } from 'material-ui';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

import MessageActionCreators from 'actions/MessageActionCreators';
import TypingActionCreators from 'actions/TypingActionCreators';
import DraftActionCreators from 'actions/DraftActionCreators';

import DraftStore from 'stores/DraftStore';

import AvatarItem from 'components/common/AvatarItem.react';

const ThemeManager = new Styles.ThemeManager();

const getStateFromStores = () => {
  return {
    text: DraftStore.getDraft(),
    profile: ActorClient.getUser(ActorClient.getUid())
  };
};

//@ReactMixin.decorate(PureRenderMixin)
class ComposeSection extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired
  };

  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ThemeManager.setTheme(ActorTheme);
    DraftStore.addLoadDraftListener(this.onDraftLoad);
  }

  componentWillUnmount() {
    DraftStore.removeLoadDraftListener(this.onDraftLoad);
  }

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  onDraftLoad = () => {
    this.setState(getStateFromStores());
  }

  onChange = event => {
    TypingActionCreators.onTyping(this.props.peer);
    this.setState({text: event.target.value});
  }

  onKeyDown = event => {
    if (event.keyCode === KeyCodes.ENTER && !event.shiftKey) {
      event.preventDefault();
      this.sendTextMessage();
    } else if (event.keyCode === 50 && event.shiftKey) {
      console.warn('Mention should show now.');
    }
  }

  onKeyUp = () => {
    DraftActionCreators.saveDraft(this.state.text);
  }

  sendTextMessage = () => {
    const text = this.state.text;
    if (text) {
      MessageActionCreators.sendTextMessage(this.props.peer, text);
    }
    this.setState({text: ''});
    DraftActionCreators.saveDraft('', true);
  }

  onSendFileClick = () => {
    const fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  }

  onSendPhotoClick = () => {
    const photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = 'image/*';
    photoInput.click();
  }

  onFileInputChange = () => {
    const files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.peer, files[0]);
  }

  onPhotoInputChange = () => {
    const photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.peer, photos[0]);
  }

  onPaste = event => {
    let preventDefault = false;

    _.forEach(event.clipboardData.items, (item) => {
      if (item.type.indexOf('image') !== -1) {
        preventDefault = true;
        MessageActionCreators.sendClipboardPhotoMessage(this.props.peer, item.getAsFile());
      }
    }, this);

    if (preventDefault) {
      event.preventDefault();
    }
  }

  render() {
    const text = this.state.text;
    const profile = this.state.profile;

    return (
      <section className="compose" onPaste={this.onPaste}>

        <AvatarItem image={profile.avatar}
                    placeholder={profile.placeholder}
                    title={profile.name}/>


          <textarea className="compose__message"
                    onChange={this.onChange}
                    onKeyDown={this.onKeyDown}
                    onKeyUp={this.onKeyUp}
                    value={text}>
          </textarea>

        <footer className="compose__footer row">
          <button className="button" onClick={this.onSendFileClick}>
            <i className="material-icons">attachment</i> Send file
          </button>
          <button className="button" onClick={this.onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> Send photo
          </button>

          <span className="col-xs"></span>

          <FlatButton label="Send" onClick={this.sendTextMessage} secondary={true}/>
        </footer>

        <div className="compose__hidden">
          <input id="composeFileInput"
                 onChange={this.onFileInputChange}
                 type="file"/>
          <input id="composePhotoInput"
                 onChange={this.onPhotoInputChange}
                 type="file"/>
        </div>
      </section>
    );
  }
}

export default ComposeSection;
