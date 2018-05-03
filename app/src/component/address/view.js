// @flow
import * as React from 'react';
import { Clipboard, Text, View } from 'react-native';
import Button from '../button';
import walletStyle from '../../styles/wallet';

type Props = {
  address: string,
  doNotify: ({ message: string, displayType: Array<string> }) => void,
};

export default class Address extends React.PureComponent<Props> {
  render() {
    const { address, doNotify, style } = this.props;
    
    return (
      <View style={[walletStyle.row, style]}>
        <Text selectable={true} numberOfLines={1} style={walletStyle.address}>{address || ''}</Text>
        <Button icon={'clipboard'} style={walletStyle.button} onPress={() => {
          Clipboard.setString(address);
          doNotify({
            message: 'Address copied',
            displayType: ['toast'],
          });
        }} />
      </View>
    );
  }
}
