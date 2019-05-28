import React from 'react';
import { Text, View } from 'react-native';
import Button from 'component/button';
import Link from 'component/link';
import walletStyle from 'styles/wallet';

class WalletSyncDriver extends React.PureComponent<Props> {
  onEnableSyncPressed = () => {
    const { navigation } = this.props;
    navigation.navigate({ routeName: 'Verification', key: 'verification', params: { syncFlow: true } });
  }

  render() {
    const { deviceWalletSynced } = this.props;

    return (
      <View style={walletStyle.syncDriverCard}>
        <View style={walletStyle.syncDriverRow}>
          <Text style={walletStyle.syncDriverTitle}>Wallet sync is {deviceWalletSynced ? 'on' : 'off'}.</Text>
          {!deviceWalletSynced &&
          <Link text="Sync FAQ" href="https://lbry.com/faq/how-to-backup-wallet#sync" style={walletStyle.syncDriverText} />}
        </View>
        {!deviceWalletSynced &&
        <View style={walletStyle.actionRow}>
          <Button style={walletStyle.enrollButton} theme={"light"} text={"Enable"} onPress={this.onEnableSyncPressed} />
          <Link text="Manual backup" href="https://lbry.com/faq/how-to-backup-wallet#android" style={walletStyle.syncDriverText} />
        </View>}
      </View>
    );
  }
}

export default WalletSyncDriver;
