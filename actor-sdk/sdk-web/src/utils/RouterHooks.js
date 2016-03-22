import LoginStore from '../stores/LoginStore';
import DialogActionCreators from '../actions/DialogActionCreators';
import PeerUtils from './PeerUtils';

const RouterHooks = {
  requireAuth(nextState, replaceState) {
    if (!LoginStore.isLoggedIn()) {
      replaceState({
        pathname: '/auth',
        state: {
          nextPathname: nextState.location.pathname
        }
      });
    }
  },
  onDialogEnter(nextState, replaceState) {
    const peer = PeerUtils.stringToPeer(nextState.params.id);
    if (!PeerUtils.hasPeer(peer)) {
      console.error('Invalig peer', nextState);
      replaceState('/im');
    } else {
      DialogActionCreators.selectDialogPeer(peer);
    }
  },
  onDialogLeave() {
    DialogActionCreators.selectDialogPeer(null);
  }
};

export default RouterHooks;
