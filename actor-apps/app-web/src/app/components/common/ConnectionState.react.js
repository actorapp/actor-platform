import React from 'react';
import ConnectionStateStore from 'stores/ConnectionStateStore';
import classnames from 'classnames';

const getStateFromStore = () => {
  return {
    connectionState: ConnectionStateStore.getState()
  };
};

class ConnectionState extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStore();

    ConnectionStateStore.addChangeListener(this.onStateChange);
  }

  componentWillUnmount() {
    ConnectionStateStore.removeChangeListener(this.onStateChange);
  }

  onStateChange = () => {
    this.setState(getStateFromStore);
  };

  render() {
    const { connectionState } = this.state;
    let connectionStateText;

    const className = classnames('connection-state', {
      'connection-state--online': connectionState === 'online',
      'connection-state--connection': connectionState === 'connecting'
    });

    switch (connectionState) {
      case 'online':
        connectionStateText = 'You\'re back online!';
        break;
      case 'connecting':
        connectionStateText = 'Houston, we have a problem! Connection to Actor server is lost. Trying to reconnect now...';
        break;
    }

    return (
      <div className={className}>
        {connectionStateText}
      </div>
    );
  }
}

export default ConnectionState;
