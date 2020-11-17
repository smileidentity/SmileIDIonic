import Foundation
import Smile_Identity_SDK
@objc(SmileIDIonic) public class SmileIDIonic : CDVPlugin,IDCardViewControllerDelegate,SelfieViewControllerDelegate,SIDNetworkRequestDelegate {
    
    
    
    static let TAG_DATA = "cordova.plugin.smileid.smile.EXTRA_TAG"
    static let RESULT = "SMILE_ID_RESULT";
    static let ACTION = "SMILE_ID_ACTION";
    var callbackId:String?
    
    func onIdCardSuccess(tag: String) {
        let args: [String: String] = [
            SmileIDIonic.TAG_DATA: tag,
            SmileIDIonic.RESULT: "success",
            SmileIDIonic.ACTION: "IDCAPTURE"
        ]
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: args)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    func onIdCardError(error: SIDError) {
        let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.message)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    func onIdCardCancelled(tag: String) {
    
        let args: [String: String] = [
            SmileIDIonic.TAG_DATA: tag,
            SmileIDIonic.RESULT: "cancelled",
            SmileIDIonic.ACTION: "IDCAPTURE"
        ]
        
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: args)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    func onSelfieSuccess(tag: String) {
//        let tagResults = [SmileIDIonic.TAG_DATA: tag] as [AnyHashable : Any]
//        let results = [SmileIDIonic.RESULT: "success"] as [AnyHashable : Any]
//        let action = [SmileIDIonic.ACTION: "SELFIE"] as [AnyHashable : Any]
//        let toReturn = [tagResults, results, action]
        let args: [String: String] = [
            SmileIDIonic.TAG_DATA: tag,
            SmileIDIonic.RESULT: "success",
            SmileIDIonic.ACTION: "SELFIE"
        ]
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: args)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    func onSelfieError(error: SIDError) {
        let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.message)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    func onSelfieCancelled(tag: String) {
        let args: [String: String] = [
            SmileIDIonic.TAG_DATA: tag,
            SmileIDIonic.RESULT: "cancelled",
            SmileIDIonic.ACTION: "SELFIE"
        ]
        
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: args)
        self.commandDelegate.send(result, callbackId: self.callbackId)
    }
    
    public func onStartJobStatus() {
        
    }
    
    public func onEndJobStatus() {
        
    }
    
    public func onUpdateJobProgress(progress: Int) {
        
    }
    
    public func onUpdateJobStatus(msg: String) {
        
    }
    
    public func onAuthenticated(sidResponse: SIDResponse) {
        if( sidResponse.isSuccess() ){
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: sidResponse.getStatusResponse()?.getRawJsonString())
            self.commandDelegate.send(result, callbackId: self.callbackId)
        }
        else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "onAuthenticated error")
            self.commandDelegate.send(result, callbackId: self.callbackId)
        }
    }
    
    public func onEnrolled(sidResponse: SIDResponse) {
        if( sidResponse.isSuccess() ){
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: sidResponse.getStatusResponse()?.getRawJsonString())
            self.commandDelegate.send(result, callbackId: self.callbackId)
        }
        else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "onEnrolled error")
            self.commandDelegate.send(result, callbackId: self.callbackId)
        }
    }
    
    public func onComplete() {
        
    }
    
    public func onError(sidError: SIDError) {
        
    }
    
    public func onIdValidated(idValidationResponse: IDValidationResponse) {
        
    }
    
    @objc(captureSelfie:)
    func captureSelfie(command : CDVInvokedUrlCommand) //this method will be called web app
    {
        self.callbackId = command.callbackId
        let viewControllerInXib = SIDSelfieViewController(nibName: "SIDSelfieViewController", bundle: nil)
        viewControllerInXib.selfieViewControllerDelegate = self
        viewControllerInXib.currentTag =  command.arguments[0] as? String ?? "TAG_MISSING"
        self.viewController.present(viewControllerInXib, animated: false, completion: nil)
        //        viewController.navigationController?.pushViewController(viewControllerInXib, animated:false)
    }
    
    @objc(captureID:)
    func captureID(command : CDVInvokedUrlCommand) //this method will be called web app
    {
        self.callbackId = command.callbackId
        let viewControllerInXib = IDCardViewController(nibName: "IDCardViewController", bundle: nil)
        viewControllerInXib.currentTag =  command.arguments[0] as? String ?? "TAG_MISSING"
        viewControllerInXib.iDCardViewControllerDelegate = self
        //        viewController.navigationController?.pushViewController(viewControllerInXib, animated:false)
        self.viewController.present(viewControllerInXib, animated: false, completion: nil)
    }
    
    @objc(upload:)
    func upload(command : CDVInvokedUrlCommand) //this method will be called web app
    {
        do {
            self.callbackId = command.callbackId
            let jobInfo =  command.arguments[0] as! Dictionary<String, Any>
             let partnerParamsArg =  command.arguments[1] as! Dictionary<String, Any>
             let userIdInfoArgs =  command.arguments[2] as! Dictionary<String, Any>
             let sidNetInfoArgs =  command.arguments[3] as! Dictionary<String, Any>
            var jobType = -1
            var tag :String = ""
            var useIDCard = false
            
            if let val = jobInfo["tag"]{
                tag = (val as? String)!
            } else {
                let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "jobInfo: tag is required")
                self.commandDelegate.send(result, callbackId: self.callbackId)
                return
            }
            
            if let val = jobInfo["jobType"] {
                jobType = (val as? Int)!
            } else {
                let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "jobInfo: jobType is required")
                self.commandDelegate.send(result, callbackId: self.callbackId)
                return
            }
            
            if let val = jobInfo["useIdCard"] {
                useIDCard = (val as? Bool)!
            }
            
            
            let partnerParams = getPartnerParams(partnerParams:partnerParamsArg)
            let userIdInfo = getUserIdInfo(userIdInfo:userIdInfoArgs)
             let sidNetData = getSIDNetData(sidNetDataInfo:sidNetInfoArgs)
            
            let sidNetworkRequest = SIDNetworkRequest()
            sidNetworkRequest.setDelegate(delegate: self)
            sidNetworkRequest.initialize()
            
            let sidConfig = SIDConfig()
            sidConfig.setSidNetworkRequest( sidNetworkRequest : sidNetworkRequest )
            if sidNetData != nil {
                sidConfig.setSidNetData( sidNetData : sidNetData! )
            }else{
                let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Enroll failed sidNetData is not configured correctly")
                self.commandDelegate.send(result, callbackId: self.callbackId)
            }
            
            sidConfig.setRetryOnFailurePolicy( retryOnFailurePolicy: getRetryOnFailurePolicy() )
            sidConfig.setIsEnrollMode( isEnrollMode: jobType == 2 ? false:true )
            sidConfig.setUseIdCard( useIdCard: useIDCard )
            sidConfig.setPartnerParams(partnerParams: partnerParams)
            sidConfig.setUserIdInfo(userIdInfo: userIdInfo)
            
            
            let sidNetworkUtils = SIDNetworkUtils()
            if( sidNetworkUtils.isConnected() ){
                sidConfig.build(userTag:tag )
                try sidConfig.getSidNetworkRequest().submit( sidConfig: sidConfig )
            }
        }
        catch {
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Enroll failed")
            self.commandDelegate.send(result, callbackId: self.callbackId)
        }
        
    }
    
    func getPartnerParams( partnerParams : Dictionary<String, Any> ) ->PartnerParams {
        let result = PartnerParams()
        if let val = partnerParams["userId"] {
            let userId = (val as? String)!
            result.setUserId(userId: userId)
        }
        
        if let val = partnerParams["jobId"] {
            let jobId = (val as? String)!
            result.setJobId(jobId: jobId)
        }
        
        for (key,value) in partnerParams {
            if(key != "userId" && key != "jobId"){
                result.setAdditionalValue(key: key, val: value)
            }
        }
        return result
    }
    
    func getUserIdInfo(userIdInfo : Dictionary<String, Any>) -> SIDUserIdInfo{
        let sidId = SIDUserIdInfo()
        
        if let val = userIdInfo["country"] {
            let country = (val as? String)!
            sidId.setCountry(country: country)
        }
        
        if let val = userIdInfo["idType"] {
            let idType = (val as? String)!
            sidId.setIdType(idType: idType)
        }
        
        if let val = userIdInfo["idNumber"] {
            let idNumber = (val as? String)!
            sidId.setIdNumber(idNumber: idNumber)
        }
        
        if let val = userIdInfo["email"] {
            let email = (val as? String)!
            sidId.setEmail(email: email)
        }
        
        if let val = userIdInfo["firstName"] {
            let firstName = (val as? String)!
            sidId.setFirstName(firstName: firstName)
        }
        
        if let val = userIdInfo["lastName"] {
            let lastName = (val as? String)!
            sidId.setLastName(lastName: lastName)
        }
        
        if let val = userIdInfo["middleName"] {
            let middleName = (val as? String)!
            sidId.setMiddleName(middleName: middleName)
        }
        
        let elements = ["country", "countryCode", "idType", "idNumber", "email","firstName","lastName","middleName"]
        for (key,value) in userIdInfo {
            if elements.contains(key) {
                sidId.additionalValue(name: key, value: value as! String)
            }
        }
        return sidId
    }
    
    func getSIDNetData(sidNetDataInfo : Dictionary<String, Any>) -> SIDNetData? {
        let sidNetData = SIDNetData()
        if let val = sidNetDataInfo["authUrl"] {
            let authUrl = (val as? String)!
            sidNetData.setAuthUrl(authUrl: authUrl)
        }else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "netData: authUrl is required")
            self.commandDelegate.send(result, callbackId: self.callbackId)
            return nil
        }
        
        if let val = sidNetDataInfo["partnerUrl"] {
            let partnerUrl = (val as? String)!
            sidNetData.setPartnerUrl(partnerUrl: partnerUrl)
        }else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "netData: partnerUrl is required")
            self.commandDelegate.send(result, callbackId: self.callbackId)
            return nil
        }
        
        if let val = sidNetDataInfo["partnerPort"] {
            let partnerPort = (val as? String)!
            sidNetData.setPartnerPort(partnerPort: partnerPort)
        }else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "netData: partnerPort is required")
            self.commandDelegate.send(result, callbackId: self.callbackId)
            return nil
        }
        
        if let val = sidNetDataInfo["lambdaUrl"] {
            let lambdaUrl = (val as? String)!
            sidNetData.setLambdaUrl(lambdaUrl: lambdaUrl)
        }else{
            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "netData: lambdaUrl is required")
            self.commandDelegate.send(result, callbackId: self.callbackId)
            return nil
        }
        
//        if let val = sidNetDataInfo["siPort"] {
//            let siPort = (val as? String)!
//            sidNetData.SET(partnerPort: siPort)
//        }else{
//            let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "netData: siPort is required")
//            self.commandDelegate.send(result, callbackId: self.callbackId)
//            return
//        }


        return sidNetData
    }
    
    func getRetryOnFailurePolicy() -> RetryOnFailurePolicy {
        let options = RetryOnFailurePolicy();
        options.setMaxRetryTimeoutSec(maxRetryTimeoutSec:15)
        return options;
    }
}
