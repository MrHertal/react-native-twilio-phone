import TwilioVoice

@objc(TwilioPhone)
class TwilioPhone: RCTEventEmitter {
    var hasListeners = false
    var audioDevice = DefaultAudioDevice()
    
    var activeCallInvites: [String: CallInvite]! = [:]
    var activeCalls: [String: Call]! = [:]
    var activeCall: Call?
    
    override func supportedEvents() -> [String]! {
        return [
            "CallInvite",
            "CancelledCallInvite",
            "CallRinging",
            "CallConnectFailure",
            "CallConnected",
            "CallReconnecting",
            "CallReconnected",
            "CallDisconnected",
            "CallDisconnectedError",
            "RegistrationSuccess",
            "RegistrationFailure",
            "UnregistrationSuccess",
            "UnregistrationFailure"
        ]
    }
    
    override func startObserving() {
        hasListeners = true
    }
    
    override func stopObserving() {
        hasListeners = false
    }
    
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc(register:withDeviceToken:)
    func register(accessToken: String, deviceToken: String) {
        NSLog("[TwilioPhone] Registering")
        
        let tokenData = hexToData(str: deviceToken)
        
        TwilioVoiceSDK.register(accessToken: accessToken, deviceToken: tokenData) { error in
            if let error = error {
                NSLog("[TwilioPhone] An error occurred while registering: \(error.localizedDescription)")
                
                if self.hasListeners {
                    self.sendEvent(withName: "RegistrationFailure", body: ["errorMessage": error.localizedDescription])
                }
            } else {
                NSLog("[TwilioPhone] Successfully registered for VoIP push notifications")
                
                if self.hasListeners {
                    self.sendEvent(withName: "RegistrationSuccess", body: nil)
                }
            }
        }
    }
    
    @objc(handleMessage:)
    func handleMessage(payload: [String: String]) {
        NSLog("[TwilioPhone] Handling message")
        
        TwilioVoiceSDK.handleNotification(payload, delegate: self, delegateQueue: nil)
    }
    
    @objc(acceptCallInvite:)
    func acceptCallInvite(callSid: String) {
        NSLog("[TwilioPhone] Accepting call invite")
        
        guard let callInvite = activeCallInvites[callSid] else {
            NSLog("[TwilioPhone] No call invite to be accepted")
            return
        }
        
        let call = callInvite.accept(with: self)
        
        activeCalls[callSid] = call
        activeCallInvites.removeValue(forKey: callSid)
    }
    
    @objc(rejectCallInvite:)
    func rejectCallInvite(callSid: String) {
        NSLog("[TwilioPhone] Rejecting call invite")
        
        guard let callInvite = activeCallInvites[callSid] else {
            NSLog("[TwilioPhone] No call invite to be rejected")
            return
        }
        
        callInvite.reject()
        
        activeCallInvites.removeValue(forKey: callSid)
    }
    
    @objc(disconnectCall:)
    func disconnectCall(callSid: String) {
        NSLog("[TwilioPhone] Disconnecting call")
        
        guard let call = activeCalls[callSid] else {
            NSLog("[TwilioPhone] No call to be disconnected")
            return
        }
        
        call.disconnect()
    }
    
    @objc(endCall:)
    func endCall(callSid: String) {
        NSLog("[TwilioPhone] Ending call")
        
        if let callInvite = activeCallInvites[callSid] {
            callInvite.reject()
            activeCallInvites.removeValue(forKey: callSid)
        } else if let call = activeCalls[callSid] {
            call.disconnect()
        } else {
            NSLog("[TwilioPhone] Unknown sid to perform end-call action with")
        }
    }
    
    @objc(toggleMuteCall:withMute:)
    func toggleMuteCall(callSid: String, mute: Bool) {
        NSLog("[TwilioPhone] Toggling mute call")
        
        guard let activeCall = activeCalls[callSid] else {
            return
        }
        
        activeCall.isMuted = mute
    }
    
    @objc(toggleHoldCall:withHold:)
    func toggleHoldCall(callSid: String, hold: Bool) {
        NSLog("[TwilioPhone] Toggling hold call")
        
        guard let activeCall = activeCalls[callSid] else {
            return
        }
        
        activeCall.isOnHold = hold
    }
    
    @objc(toggleSpeaker:)
    func toggleAudioRoute(speakerOn: Bool) {
        NSLog("[TwilioPhone] Toggling speaker")
        
        audioDevice.block = {
            DefaultAudioDevice.DefaultAVAudioSessionConfigurationBlock()
            do {
                if speakerOn {
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(.speaker)
                } else {
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(.none)
                }
            } catch {
                NSLog("[TwilioPhone] Failed to toggle speaker: \(error.localizedDescription)")
            }
        }
        
        audioDevice.block()
    }
    
    @objc(sendDigits:withDigits:)
    func sendDigits(callSid: String, digits: String) {
        NSLog("[TwilioPhone] Sending digits")
        
        guard let activeCall = activeCalls[callSid] else {
            return
        }
        
        activeCall.sendDigits(digits)
    }
    
    @objc(startCall:withParams:)
    func startCall(accessToken: String, params: [String: String]) {
        NSLog("[TwilioPhone] Starting call")
        
        let connectOptions = ConnectOptions(accessToken: accessToken) { builder in
            builder.params = params
        }
        
        let call = TwilioVoiceSDK.connect(options: connectOptions, delegate: self)
        
        activeCall = call
    }
    
    @objc(unregister:withDeviceToken:)
    func unregister(accessToken: String, deviceToken: String) {
        NSLog("[TwilioPhone] Unregistering")
        
        let tokenData = hexToData(str: deviceToken)
        
        TwilioVoiceSDK.unregister(accessToken: accessToken, deviceToken: tokenData) { error in
            if let error = error {
                NSLog("[TwilioPhone] An error occurred while unregistering: \(error.localizedDescription)")
                
                if self.hasListeners {
                    self.sendEvent(withName: "UnregistrationFailure", body: ["errorMessage": error.localizedDescription])
                }
            } else {
                NSLog("[TwilioPhone] Successfully unregistered from VoIP push notifications")
                
                if self.hasListeners {
                    self.sendEvent(withName: "UnregistrationSuccess", body: nil)
                }
            }
        }
    }
    
    @objc
    func activateAudio() {
        NSLog("[TwilioPhone] Activating audio")
        
        audioDevice.isEnabled = true
    }
    
    @objc
    func deactivateAudio() {
        NSLog("[TwilioPhone] Deactivating audio")
        
        audioDevice.isEnabled = false
    }
    
    @objc(checkPermissions:)
    func checkPermissions(callback: RCTResponseSenderBlock) {
        NSLog("[TwilioPhone] Checking permissions")
        
        var permissions: [String: String] = [:]
        
        let permissionStatus = AVAudioSession.sharedInstance().recordPermission
        
        switch permissionStatus {
        case .granted:
            permissions["RECORD"] = "GRANTED"
        case .denied:
            permissions["RECORD"] = "DENIED"
        case .undetermined:
            permissions["RECORD"] = "UNDETERMINED"
            
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                NSLog("[TwilioPhone] Record permission granted: \(granted)")
            }
        default:
            permissions["RECORD"] = "UNKNOWN"
        }
        
        callback([permissions])
    }
    
    func hexToData(str: String) -> Data {
        let len = str.count / 2
        var data = Data(capacity: len)
        let ptr = str.cString(using: String.Encoding.utf8)!
        
        for i in 0..<len {
            var num: UInt8 = 0
            var multi: UInt8 = 16
            for j in 0..<2 {
                let c = UInt8(ptr[i*2 + j])
                var offset: UInt8 = 0
                
                switch c {
                case 48...57: // '0'-'9'
                    offset = 48
                case 65...70: // 'A'-'F'
                    offset = 65 - 10 // 10 since 'A' is 10, not 0
                case 97...102: // 'a'-'f'
                    offset = 97 - 10 // 10 since 'a' is 10, not 0
                default:
                    assert(false)
                }
                
                num += (c - offset)*multi
                multi = 1
            }
            data.append(num)
        }
        return data
    }
}

// MARK: - TVOCallDelegate

extension TwilioPhone: CallDelegate {
    func callDidStartRinging(call: Call) {
        NSLog("[TwilioPhone] Call did start ringing")
        
        activeCalls[call.sid] = call
        
        if call == activeCall {
            activeCall = nil
        }
        
        if hasListeners {
            sendEvent(withName: "CallRinging", body: ["callSid": call.sid])
        }
    }
    
    func callDidFailToConnect(call: Call, error: Error) {
        NSLog("[TwilioPhone] Call failed to connect: \(error.localizedDescription)")
        
        if hasListeners {
            sendEvent(withName: "CallConnectFailure", body: [
                "callSid": call.sid,
                "errorMessage": error.localizedDescription
            ])
        }
    }
    
    func callDidConnect(call: Call) {
        NSLog("[TwilioPhone] Call did connect")
        
        if hasListeners {
            sendEvent(withName: "CallConnected", body: ["callSid": call.sid])
        }
    }
    
    func callIsReconnecting(call: Call, error: Error) {
        NSLog("[TwilioPhone] Call is reconnecting with error: \(error.localizedDescription)")
        
        if hasListeners {
            sendEvent(withName: "CallReconnecting", body: [
                "callSid": call.sid,
                "errorMessage": error.localizedDescription
            ])
        }
    }
    
    func callDidReconnect(call: Call) {
        NSLog("[TwilioPhone] Call did reconnect")
        
        if hasListeners {
            sendEvent(withName: "CallReconnected", body: ["callSid": call.sid])
        }
    }
    
    func callDidDisconnect(call: Call, error: Error?) {
        if let error = error {
            NSLog("[TwilioPhone] Call disconnected with error: \(error.localizedDescription)")
            
            if hasListeners {
                sendEvent(withName: "CallDisconnectedError", body: [
                    "callSid": call.sid,
                    "errorMessage": error.localizedDescription
                ])
            }
        } else {
            NSLog("[TwilioPhone] Call disconnected")
            
            if hasListeners {
                sendEvent(withName: "CallDisconnected", body: ["callSid": call.sid])
            }
        }
        
        activeCalls.removeValue(forKey: call.sid)
    }
}

// MARK: - TVONotificationDelegate

extension TwilioPhone: NotificationDelegate {
    func callInviteReceived(callInvite: CallInvite) {
        NSLog("[TwilioPhone] Call invite received")
        
        activeCallInvites[callInvite.callSid] = callInvite
        
        let callerInfo: TVOCallerInfo = callInvite.callerInfo
        if let verified: NSNumber = callerInfo.verified {
            if verified.boolValue {
                NSLog("[TwilioPhone] Call invite received from verified caller number")
            }
        }
        
        if hasListeners {
            sendEvent(withName: "CallInvite", body: [
                "callSid": callInvite.callSid,
                "from": callInvite.from
            ])
        }
    }
    
    func cancelledCallInviteReceived(cancelledCallInvite: CancelledCallInvite, error: Error) {
        NSLog("[TwilioPhone] Cancelled call invite received")
        
        activeCallInvites.removeValue(forKey: cancelledCallInvite.callSid)
        
        if hasListeners {
            sendEvent(withName: "CancelledCallInvite", body: ["callSid": cancelledCallInvite.callSid])
        }
    }
}
