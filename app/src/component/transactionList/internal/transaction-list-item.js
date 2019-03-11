// @flow
import React from 'react';
import { Text, View, Linking } from 'react-native';
import { buildURI, formatCredits } from 'lbry-redux';
import { navigateToUri } from '../../../utils/helper';
import Link from '../../link';
import moment from 'moment';
import transactionListStyle from '../../../styles/transactionList';

class TransactionListItem extends React.PureComponent {
  capitalize(string: string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }

  render() {
    const { transaction, navigation } = this.props;
    const { amount, claim_id: claimId, claim_name: name, date, fee, txid, type } = transaction;

    return (
      <View style={transactionListStyle.listItem}>
        <View style={[transactionListStyle.row, transactionListStyle.topRow]}>
          <View style={transactionListStyle.col}>
            <Text style={transactionListStyle.text}>{this.capitalize(type)}</Text>
            {name && claimId && (
              <Link
                style={transactionListStyle.link}
                onPress={() => navigateToUri(navigation, buildURI({ claimName: name, claimId }))}
                text={name} />
            )}
          </View>
          <View style={transactionListStyle.col}>
            <Text style={[transactionListStyle.amount, transactionListStyle.text]}>{formatCredits(amount, 8)}</Text>
            { fee !== 0 && (<Text style={[transactionListStyle.amount, transactionListStyle.text]}>fee {formatCredits(fee, 8)}</Text>) }
          </View>
        </View>
        <View style={transactionListStyle.row}>
          <View style={transactionListStyle.col}>
            <Link style={transactionListStyle.smallLink}
                  text={txid.substring(0, 8)}
                  href={`https://explorer.lbry.com/tx/${txid}`}
                  error={'The transaction URL could not be opened'} />
          </View>
          <View style={transactionListStyle.col}>
            {date ? (
              <Text style={transactionListStyle.smallText}>{moment(date).format('MMM D')}</Text>
            ) : (
              <Text style={transactionListStyle.smallText}>Pending</Text>
            )}
          </View>
        </View>
      </View>
    );
  }
}

export default TransactionListItem;
