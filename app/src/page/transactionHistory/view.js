import React from 'react';
import { View, ScrollView, Text } from 'react-native';
import Constants from 'constants';
import TransactionList from 'component/transactionList';
import UriBar from 'component/uriBar';
import walletStyle from 'styles/wallet';

class TransactionHistoryPage extends React.PureComponent {
  didFocusListener;

  componentWillMount() {
    const { navigation } = this.props;
    this.didFocusListener = navigation.addListener('didFocus', this.onComponentFocused);
  }

  componentWillUnmount() {
    if (this.didFocusListener) {
      this.didFocusListener.remove();
    }
  }

  onComponentFocused = () => {
    const { fetchTransactions, pushDrawerStack, setPlayerVisible } = this.props;
    pushDrawerStack();
    setPlayerVisible();
    fetchTransactions();
  };

  componentDidMount() {
    this.onComponentFocused();
  }

  componentWillReceiveProps(nextProps) {
    const { currentRoute } = nextProps;
    const { currentRoute: prevRoute } = this.props;
    if (Constants.DRAWER_ROUTE_TRANSACTION_HISTORY === currentRoute && currentRoute !== prevRoute) {
      this.onComponentFocused();
    }
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
            {!fetchingTransactions && transactions && transactions.length > 0 && (
              <TransactionList navigation={navigation} transactions={transactions} />
            )}
          </View>
        </ScrollView>
      </View>
    );
  }
}

export default TransactionHistoryPage;
