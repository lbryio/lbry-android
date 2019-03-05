// @flow
import React from 'react';
import { ActivityIndicator, Text, TouchableOpacity, View } from 'react-native';
import { formatCredits } from 'lbry-redux'
import Address from '../address';
import Button from '../button';
import Colors from '../../styles/colors';
import floatingButtonStyle from '../../styles/floatingButton';

type Props = {
  balance: number,
};

class FloatingWalletBalance extends React.PureComponent<Props> {
  render() {
    const { balance, navigation } = this.props;

    return (
      <View style={[floatingButtonStyle.view, floatingButtonStyle.bottomRight]}>
        <TouchableOpacity style={floatingButtonStyle.container}
                          onPress={() => navigation && navigation.navigate({ routeName: 'WalletStack' })}>
          {isNaN(balance) && <ActivityIndicator size="small" color={Colors.White} />}
          <Text style={floatingButtonStyle.text}>
            {(balance || balance === 0) && (formatCredits(parseFloat(balance), 2) + ' LBC')}
          </Text>
        </TouchableOpacity>
        <TouchableOpacity style={floatingButtonStyle.pendingContainer}>
          <Text style={floatingButtonStyle.text}>
            get 40
          </Text>
        </TouchableOpacity>
      </View>
    );
  }
}

export default FloatingWalletBalance;
