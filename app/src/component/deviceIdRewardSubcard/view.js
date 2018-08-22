// @flow
import React from 'react';
import {
  ActivityIndicator,
  AsyncStorage,
  NativeModules,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Button from '../button';
import Colors from '../../styles/colors';
import Constants from '../../constants';
import Link from '../link';
import rewardStyle from '../../styles/reward';

class DeviceIdRewardSubcard extends React.PureComponent {
  onAllowAccessPressed = () => {
    if (!NativeModules.UtilityModule) {
      return notify({
        message: 'The device ID could not be obtained due to a missing module.',
        displayType: ['toast']
      });
    }

    NativeModules.UtilityModule.requestPhoneStatePermission();
  }

  render() {
    return (
      <View style={rewardStyle.subcard}>
        <Text style={rewardStyle.subtitle}>Pending action: Device ID</Text>
        <Text style={[rewardStyle.bottomMarginMedium, rewardStyle.subcardText]}>
          The app requires the phone state permission in order to identify your device for reward eligibility.
        </Text>
        <Button style={rewardStyle.actionButton}
                text={"Allow Access"}
                onPress={this.onAllowAccessPressed} />
      </View>
    );
  }
};

export default DeviceIdRewardSubcard;
