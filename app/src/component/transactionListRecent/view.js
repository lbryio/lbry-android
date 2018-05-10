// @flow
import React from 'react';
//import BusyIndicator from 'component/common/busy-indicator';
import { Text, View } from 'react-native';
import Button from '../button';
import Link from '../link';
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
    const { fetchingTransactions, hasTransactions, transactions, navigation } = this.props;

    return (
      <View style={walletStyle.transactionsCard}>
        <View style={[walletStyle.row, walletStyle.transactionsHeader]}>
          <Text style={walletStyle.transactionsTitle}>Recent Transactions</Text>
          <Link style={walletStyle.link}
                navigation={navigation}
                text={'View All'}
                href={'#TransactionHistory'} />
        </View>
        {fetchingTransactions && (
          <Text style={walletStyle.infoText}>Fetching transactions...</Text>  
        )}
        {!fetchingTransactions && (
          <TransactionList
            navigation={navigation}
            transactions={transactions}
            emptyMessage={"Looks like you don't have any recent transactions."}
          />
        )}
      </View>
    );
  }
}

export default TransactionListRecent;
