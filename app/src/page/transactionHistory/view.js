import React from 'react';
import { View, ScrollView, Text } from 'react-native';
import TransactionList from 'component/transactionList';
import UriBar from 'component/uriBar';
import walletStyle from 'styles/wallet';

class TransactionHistoryPage extends React.PureComponent {
  componentWillMount() {
    this.props.pushDrawerStack();
  }

  componentDidMount() {
    this.props.fetchTransactions();
  }

  render() {
    const { fetchingTransactions, transactions, navigation } = this.props;

    return (
      <View>
        <UriBar navigation={navigation} />
        <ScrollView style={walletStyle.transactionHistoryScroll}>
          <View style={walletStyle.historyList}>
            {fetchingTransactions && !transactions.length && (
              <Text style={walletStyle.infoText}>Loading transactions...</Text>
            )}
            {!fetchingTransactions && transactions.length === 0 && (
              <Text style={walletStyle.infoText}>No transactions to list.</Text>
            )}
            {!fetchingTransactions && transactions && (transactions.length > 0) && (
              <TransactionList navigation={navigation} transactions={transactions} />
            )}
          </View>
        </ScrollView>
      </View>
    );
  }
}

export default TransactionHistoryPage;
