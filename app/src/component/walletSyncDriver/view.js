import React from 'react';
import { Alert, NativeModules, Switch, Text, View } from 'react-native';
import Button from 'component/button';
import Constants from 'constants';
import Link from 'component/link';
import walletStyle from 'styles/wallet';

class WalletSyncDriver extends React.PureComponent<Props> {
  handleSyncStatusChange = value => {
    const { navigation, notify, setClientSetting } = this.props;
    if (value) {
      // enabling
      navigation.navigate({ routeName: 'Verification', key: 'verification', params: { syncFlow: true } });
    } else {
      // turning off
      // set deviceWalletSynced to false (if confirmed)
      Alert.alert(
        'Disable wallet sync',
        'Are you sure you want to turn off wallet sync?',
        [
          { text: 'No' },
          {
            text: 'Yes',
            onPress: () => {
              setClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED, false);
              notify({ message: 'Wallet sync was successfully disabled.' });
            },
          },
        ],
        { cancelable: true }
      );
    }
  };

  render() {
    const { deviceWalletSynced, userEmail } = this.props;

    return (
      <View style={walletStyle.syncDriverCard}>
        <Text style={walletStyle.syncDriverTitle}>Wallet Sync</Text>
        <View style={walletStyle.switchRow}>
          <View style={walletStyle.tableCol}>
            <Text style={walletStyle.labelText}>Sync status</Text>
          </View>
          <View style={walletStyle.tableColRow}>
            <Text selectable={true} style={walletStyle.valueText}>
              {deviceWalletSynced ? 'On' : 'Off'}
            </Text>
            <Switch
              style={walletStyle.syncSwitch}
              value={deviceWalletSynced}
              onValueChange={this.handleSyncStatusChange}
            />
          </View>
        </View>
        {deviceWalletSynced && (
          <View style={walletStyle.tableRow}>
            <View style={walletStyle.tableCol}>
              <Text style={walletStyle.labelText}>Connected email</Text>
            </View>
            <View style={walletStyle.tableCol}>
              <Text selectable={true} style={walletStyle.valueText}>
                {userEmail ? userEmail : 'No connected email'}
              </Text>
            </View>
          </View>
        )}

        <View style={walletStyle.linkRow}>
          <View style={walletStyle.tableCol}>
            <Link
              text="Manual backup"
              href="https://lbry.com/faq/how-to-backup-wallet#android"
              style={walletStyle.syncDriverLink}
            />
          </View>
          <View style={walletStyle.rightTableCol}>
            {!deviceWalletSynced && (
              <Link
                text="Sync FAQ"
                href="https://lbry.com/faq/how-to-backup-wallet#sync"
                style={[walletStyle.syncDriverLink, walletStyle.rightLink]}
              />
            )}
          </View>
        </View>
      </View>
    );
  }
}

export default WalletSyncDriver;
