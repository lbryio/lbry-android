import React from 'react';
import { View, ScrollView, Text } from 'react-native';
import TransactionList from '../../component/transactionList';
import walletStyle from '../../styles/wallet';

class TransactionHistoryPage extends React.PureComponent {
  componentDidMount() {
    this.props.fetchTransactions();
  }

  render() {
    const { fetchingTransactions, transactions, navigation } = this.props;

    return (
      <ScrollView>
        <View style={walletStyle.historyList}>
          {fetchingTransactions && !transactions.length && (
            <Text style={walletStyle.infoText}>Loading transactions...</Text>
          )}
          {transactions && transactions.length && (
            <TransactionList navigation={navigation} transactions={transactions} />
          )}
        </View>
      </ScrollView>
    );
  }
}

export default TransactionHistoryPage;
