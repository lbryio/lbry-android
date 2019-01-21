import { connect } from 'react-redux';
import {
  doChannelSubscriptionEnableNotifications,
  doChannelSubscriptionDisableNotifications,
  selectEnabledChannelNotifications,
  selectSubscriptions,
  makeSelectIsSubscribed,
} from 'lbryinc';
import { doToast } from 'lbry-redux';
import SubscribeNotificationButton from './view';

const select = (state, props) => ({
  enabledChannelNotifications: selectEnabledChannelNotifications(state),
  subscriptions: selectSubscriptions(state),
  isSubscribed: makeSelectIsSubscribed(props.uri, true)(state),
});

export default connect(
  select,
  {
    doChannelSubscriptionEnableNotifications,
    doChannelSubscriptionDisableNotifications,
    doToast,
  }
)(SubscribeNotificationButton);
