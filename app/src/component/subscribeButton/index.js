import { connect } from 'react-redux';
import {
  doChannelSubscribe,
  doChannelUnsubscribe,
  selectSubscriptions,
  makeSelectIsSubscribed,
} from 'lbryinc';
import { doToast } from 'lbry-redux';
import SubscribeButton from './view';

const select = (state, props) => ({
  subscriptions: selectSubscriptions(state),
  isSubscribed: makeSelectIsSubscribed(props.uri, true)(state),
});

export default connect(
  select,
  {
    doChannelSubscribe,
    doChannelUnsubscribe,
    doToast,
  }
)(SubscribeButton);
