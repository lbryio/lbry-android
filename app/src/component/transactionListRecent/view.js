// @flow
import React from 'react';
//import BusyIndicator from 'component/common/busy-indicator';
import { Text, View } from 'react-native';
import Button from '../button';
import TransactionList from '../transactionList';
import type { Transaction } from '../transactionList/view';
import walletStyle from '../../styles/wallet';

type Props = {
  fetchTransactions: () => void,
  fetchingTransactions: boolean,
  hasTransactions: boolean,
  transactions: Array<Transaction>,
};

class TransactionListRecent extends React.PureComponent<Props> {
  componentDidMount() {
    this.props.fetchTransactions();
  }

  render() {
    const { fetchingTransactions, hasTransactions, transactions } = this.props;

    return (
      <View style={walletStyle.transactionsCard}>
        <Text style={walletStyle.transactionsTitle}>Recent Transactions</Text>
        {fetchingTransactions && (
          <Text style={walletStyle.infoText}>Fetching transactions...</Text>  
        )}
        {!fetchingTransactions && (
          <TransactionList
            transactions={transactions}
            emptyMessage={"Looks like you don't have any recent transactions."}
          />
        )}
      </View>
    );
  }
}

export default TransactionListRecent;
