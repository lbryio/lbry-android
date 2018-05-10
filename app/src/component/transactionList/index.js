import { connect } from 'react-redux';
//import { selectClaimedRewardsByTransactionId } from 'redux/selectors/rewards';
import { selectAllMyClaimsByOutpoint } from 'lbry-redux';
import TransactionList from './view';

const select = state => ({
  //rewards: selectClaimedRewardsByTransactionId(state),
  myClaims: selectAllMyClaimsByOutpoint(state),
});

export default connect(select, null)(TransactionList);
