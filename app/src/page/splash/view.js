import React from 'react';
import { Lbry } from 'lbry-redux';
import { View, Text, NativeModules } from 'react-native';
import PropTypes from 'prop-types';
import splashStyle from '../../styles/splash';

class SplashScreen extends React.PureComponent {
  static navigationOptions = {
    title: 'Splash'
  };
  
  componentWillMount() {
    this.setState({
      details: 'Starting daemon',
      message: 'Connecting',
      isRunning: false,
      isLagging: false,
    });
  }

  updateStatus() {
    Lbry.status().then(status => {
      this._updateStatusCallback(status);
    });
  }
  
  _updateStatusCallback(status) {
    const startupStatus = status.startup_status;
    if (startupStatus.code == 'started') {
      // Wait until we are able to resolve a name before declaring
      // that we are done.
      // TODO: This is a hack, and the logic should live in the daemon
      // to give us a better sense of when we are actually started
      this.setState({
        message: 'Testing Network',
        details: 'Waiting for name resolution',
        isLagging: false,
        isRunning: true,
      });

      Lbry.resolve({ uri: 'lbry://one' }).then(() => {
        // Leave the splash screen
        const { navigation } = this.props;
        navigation.navigate('Main');
      });
      return;
    }
    if (status.blockchain_status && status.blockchain_status.blocks_behind > 0) {
      const behind = status.blockchain_status.blocks_behind;
      const behindText = behind + ' block' + (behind == 1 ? '' : 's') + ' behind';
      this.setState({
        message: 'Blockchain Sync',
        details: behindText,
        isLagging: startupStatus.is_lagging,
      });
    } else {
      this.setState({
        message: 'Network Loading',
        details: startupStatus.message + (startupStatus.is_lagging ? '' : '...'),
        isLagging: startupStatus.is_lagging,
      });
    }
    setTimeout(() => {
      this.updateStatus();
    }, 500);
  }
  
  componentDidMount() {
    if (NativeModules.Mixpanel) {
      NativeModules.Mixpanel.track('App Launch', null);
    }
    
    Lbry
      .connect()
      .then(() => {
        this.updateStatus();
      })
      .catch((e) => {
        this.setState({
          isLagging: true,
          message: 'Connection Failure',
          details:
            'We could not establish a connection to the daemon. Your data connection may be preventing LBRY from connecting. Contact hello@lbry.io if you think this is a software bug.'
        });
      });
  }

  render() {
    const { message, details, isLagging, isRunning } = this.state;
    
    return (
      <View style={splashStyle.container}>
        <Text style={splashStyle.title}>Lbry.</Text>
        <Text style={splashStyle.message}>{message}</Text>
        <Text style={splashStyle.details}>{details}</Text>
      </View>
    );
  }
}

export default SplashScreen;
