// @flow
import React from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import { formatCredits } from 'lbry-redux'
import Address from '../address';
import Button from '../button';
import floatingButtonStyle from '../../styles/floatingButton';

type Props = {
  balance: number,
};

class FloatingWalletBalance extends React.PureComponent<Props> {
  render() {
    const { balance, navigation } = this.props;

    return (
      <TouchableOpacity style={[floatingButtonStyle.container, floatingButtonStyle.bottomRight]}
                        onPress={() => {
                          if (navigation) {
                            navigation.navigate({ routeName: 'Wallet' });
                          }
                        }}>
        <Text style={floatingButtonStyle.text}>
          {(balance || balance === 0) && (formatCredits(balance, 2) + ' LBC')}
        </Text>
      </TouchableOpacity>
    );
  }
}

export default FloatingWalletBalance;
