import * as React from 'react';
import {
  ActivityIndicator,
  Button,
  Platform,
  StyleSheet,
  TextInput,
  View,
} from 'react-native';
import RNCallKeep from 'react-native-callkeep';
import {
  EventType,
  RNTwilioPhone,
  twilioPhoneEmitter,
} from 'react-native-twilio-phone';

const identity = 'alice';

const callKeepOptions = {
  ios: {
    appName: 'TwilioPhone Example',
    supportsVideo: false,
    maximumCallGroups: '1',
    maximumCallsPerCallGroup: '1',
  },
  android: {
    alertTitle: 'Permissions required',
    alertDescription: 'This application needs to access your phone accounts',
    cancelButton: 'Cancel',
    okButton: 'ok',
    additionalPermissions: [],
  },
};

async function fetchAccessToken() {
  const response = await fetch(
    'https://XXXXXX.ngrok.io/accessToken?identity=' +
      identity +
      '&os=' +
      Platform.OS
  );
  const accessToken = await response.text();

  return accessToken;
}

export function App() {
  const [to, setTo] = React.useState('');
  const [callInProgress, setCallInProgress] = React.useState(false);

  React.useEffect(() => {
    return RNTwilioPhone.initialize(callKeepOptions, fetchAccessToken, {
      requestPermissionsOnInit: false,
    });
  }, []);

  React.useEffect(() => {
    const subscriptions = [
      twilioPhoneEmitter.addListener(EventType.CallConnected, () => {
        setCallInProgress(true);
      }),
      twilioPhoneEmitter.addListener(EventType.CallDisconnected, () => {
        setCallInProgress(RNTwilioPhone.calls.length > 0);
      }),
      twilioPhoneEmitter.addListener(
        EventType.CallDisconnectedError,
        (data) => {
          console.log(data);
          setCallInProgress(RNTwilioPhone.calls.length > 0);
        }
      ),
    ];

    return () => {
      subscriptions.map((subscription) => {
        subscription.remove();
      });
    };
  }, []);

  function hangup() {
    RNCallKeep.endAllCalls();
  }

  async function call() {
    if (to === '') {
      return;
    }

    setCallInProgress(true);

    try {
      await RNTwilioPhone.startCall(to, 'My friend');
    } catch (e) {
      console.log(e);
      setCallInProgress(false);
    }
  }

  async function unregister() {
    try {
      await RNTwilioPhone.unregister();
    } catch (e) {
      console.log(e);
    }
  }

  let content;

  if (callInProgress) {
    content = (
      <View>
        <ActivityIndicator style={styles.loader} />
        <Button title="End call" onPress={hangup} />
      </View>
    );
  } else {
    content = (
      <View>
        <TextInput
          style={styles.to}
          onChangeText={(text) => setTo(text)}
          value={to}
          placeholder="Client or phone number"
          placeholderTextColor="gray"
        />
        <Button title="Start call" onPress={call} />
        <View style={styles.unregister}>
          <Button title="Unregister" onPress={unregister} />
        </View>
      </View>
    );
  }

  return <View style={styles.container}>{content}</View>;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  loader: {
    marginBottom: 40,
  },
  to: {
    height: 50,
    width: 200,
    fontSize: 16,
    borderColor: 'gray',
    borderBottomWidth: 1,
    marginBottom: 40,
    color: 'black',
    textAlign: 'center',
  },
  unregister: {
    marginTop: 40,
  },
});
