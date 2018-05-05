// @flow
import React from 'react';
import { Text, View } from 'react-native';
import TransactionListItem from './internal/transaction-list-item';
import transactionListStyle from '../../styles/transactionList';

export type Transaction = {
  amount: number,
  claim_id: string,
  claim_name: string,
  fee: number,
  nout: number,
  txid: string,
  type: string,
  date: Date,
};

class TransactionList extends React.PureComponent {
  constructor(props) {
    super(props);

    this.state = {
      filter: 'all',
    };

    (this: any).handleFilterChanged = this.handleFilterChanged.bind(this);
    (this: any).filterTransaction = this.filterTransaction.bind(this);
  }

  handleFilterChanged(event: React.SyntheticInputEvent<*>) {
    this.setState({
      filter: event.target.value,
    });
  }

  filterTransaction(transaction: Transaction) {
    const { filter } = this.state;

    return filter === 'all' || filter === transaction.type;
  }
  
  render() {
    const { emptyMessage, rewards, transactions, navigation } = this.props;
    const { filter } = this.state;
    const transactionList = transactions.filter(this.filterTransaction);
    
    return (
      <View>
        {!transactionList.length && (
          <Text style={transactionListStyle.noTransactions}>{emptyMessage || 'No transactions to list.'}</Text>
        )}
      
        {!!transactionList.length && (
          <View>
            {transactionList.map(t => (
              <TransactionListItem
                key={`${t.txid}:${t.nout}`}
                transaction={t}
                navigation={navigation}
                reward={rewards && rewards[t.txid]}
              />
            ))}
          </View>
        )}
      </View>
    );
  }
}

export default TransactionList;
