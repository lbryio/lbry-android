import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  Linking,
  NativeModules,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { NavigationActions } from 'react-navigation';
import Colors from '../../styles/colors';
import WelcomePage from './internal/welcome-page';
import firstRunStyle from '../../styles/firstRun';

class FirstRunScreen extends React.PureComponent {
  static pages = ['welcome'];

  constructor() {
    super();
    this.state = {
      currentPage: null,
      launchUrl: null,
      isFirstRun: false
    }
  }

  componentDidMount() {
    Linking.getInitialURL().then((url) => {
      if (url) {
        this.setState({ launchUrl: url });
      }
    });

    if (NativeModules.FirstRun) {
      NativeModules.FirstRun.isFirstRun().then(firstRun => {
        this.setState({ isFirstRun: firstRun });
        if (firstRun) {
          this.setState({ currentPage: FirstRunScreen.pages[0] });
        } else {
          // Not the first run. Navigate to the splash screen right away
          this.launchSplashScreen();
        }
      });
    } else {
      // The first run module was not detected. Go straight to the splash screen.
      this.launchSplashScreen();
    }
  }

  launchSplashScreen() {
    const { navigation } = this.props;
    const resetAction = NavigationActions.reset({
      index: 0,
      actions: [
        NavigationActions.navigate({ routeName: 'Splash', params: { launchUri: this.state.launchUri } })
      ]
    });
    navigation.dispatch(resetAction);
  }

  handleContinuePressed = () => {
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    if (pageIndex === (FirstRunScreen.pages.length - 1)) {
      // Final page. Let the app know that first run experience is completed.
      if (NativeModules.FirstRun) {
        NativeModules.FirstRun.firstRunCompleted();
      }

      // Navigate to the splash screen
      this.launchSplashScreen();
    } else {
      // TODO: Page transition animation?
       this.state.currentPage = FirstRunScreen.pages[pageIndex + 1];
    }
  }

  render() {
    let page = null;
    if (this.state.currentPage === 'welcome') {
      // show welcome page
      page = (<WelcomePage />);
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}
        {this.state.currentPage &&
        <TouchableOpacity style={firstRunStyle.button} onPress={this.handleContinuePressed}>
          <Text style={firstRunStyle.buttonText}>Continue</Text>
        </TouchableOpacity>}
      </View>
    )
  }
}

export default FirstRunScreen;
