import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import fuzzaldrin from 'fuzzaldrin';
import QuickSearchStore from '../stores/QuickSearchStore';

class SearchActionCreators {
  handleSearch(query) {
    const list = QuickSearchStore.getState();

    const filteredResults = list.filter((element) => {
      return fuzzaldrin.score(element.peerInfo.title, query) > 0 ||
             fuzzaldrin.score(element.peerInfo.userName, query) > 0;
    });

    let groups = [];
    let contacts = [];

    filteredResults.forEach((result) => {
      switch (result.peerInfo.peer.type) {
        case PeerTypes.GROUP:
          groups.push(result);
          break;
        case PeerTypes.USER:
          contacts.push(result);
          break;
        default:
          console.warn('Wrong peer type in results item');
      }
    })

    dispatch(ActionTypes.SEARCH, { query, results: { groups, contacts }});
  }
}

export default new SearchActionCreators();
