import { AppRegistry } from 'react-native';
import { RNTwilioPhone } from 'react-native-twilio-phone';
import { name as appName } from './app.json';
import { App } from './src/App';

const callKeepOptions = {
  ios: {
    appName: 'TwilioPhone Example',
    supportsVideo: false,
  },
  android: {
    alertTitle: 'Permissions required',
    alertDescription: 'This application needs to access your phone accounts',
    cancelButton: 'Cancel',
    okButton: 'OK',
    additionalPermissions: [],
    // Required to get audio in background when using Android 11
    foregroundService: {
      channelId: 'com.example.reactnativetwiliophone',
      channelName: 'Foreground service for my app',
      notificationTitle: 'My app is running on background',
    },
  },
};

RNTwilioPhone.handleBackgroundState(callKeepOptions);

AppRegistry.registerComponent(appName, () => App);
