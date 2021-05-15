import { AppRegistry } from 'react-native';
import { RNTwilioPhone } from 'react-native-twilio-phone';
import { name as appName } from './app.json';
import { App } from './src/App';

RNTwilioPhone.handleBackgroundState();

AppRegistry.registerComponent(appName, () => App);
