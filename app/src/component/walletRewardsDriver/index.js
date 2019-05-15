import { connect } from 'react-redux';
import WalletRewardsDriver from './view';

const select = state => ({});

export default connect(select, null)(WalletRewardsDriver);
