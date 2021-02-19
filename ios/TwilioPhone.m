#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(TwilioPhone, NSObject)

RCT_EXTERN_METHOD(register:(NSString *)accessToken withDeviceToken:(NSString *)deviceToken)

RCT_EXTERN_METHOD(handleMessage:(NSDictionary *)payload)

RCT_EXTERN_METHOD(acceptCallInvite:(NSString *)callSid)

RCT_EXTERN_METHOD(rejectCallInvite:(NSString *)callSid)

RCT_EXTERN_METHOD(disconnectCall:(NSString *)callSid)

RCT_EXTERN_METHOD(endCall:(NSString *)callSid)

RCT_EXTERN_METHOD(toggleMuteCall:(NSString *)callSid withMute:(BOOL *)mute)

RCT_EXTERN_METHOD(toggleHoldCall:(NSString *)callSid withHold:(BOOL *)hold)

RCT_EXTERN_METHOD(toggleSpeaker:(BOOL *)speakerOn)

RCT_EXTERN_METHOD(sendDigits:(NSString *)callSid withDigits:(NSString *)digits)

RCT_EXTERN_METHOD(startCall:(NSString *)accessToken withParams:(NSDictionary *)params)

RCT_EXTERN_METHOD(unregister:(NSString *)accessToken withDeviceToken:(NSString *)deviceToken)

RCT_EXTERN_METHOD(activateAudio)

RCT_EXTERN_METHOD(deactivateAudio)

RCT_EXTERN_METHOD(checkPermissions:(RCTResponseSenderBlock)callback)

@end
