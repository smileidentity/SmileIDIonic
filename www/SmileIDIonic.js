var exec = require('cordova/exec');

function SmileIDIonic() {

    this.captureType = {
        SELFIE_CAPTURE: "SELFIE_CAPTURE",
        ID_CAPTURE: "ID_CAPTURE"
    };
}

SmileIDIonic.prototype.captureSelfie = function (arg0, success, error) {
    exec(success, error, 'SmileIDIonic', 'captureSelfie', [arg0]);
};

SmileIDIonic.prototype.captureID = function (arg0, success, error) {
    exec(success, error, 'SmileIDIonic', 'captureID', [arg0]);
};

SmileIDIonic.prototype.upload = function (jobInfo, partnerParams, userIdInfo, sidNetInfo, success, error) {
    exec(success, error, 'SmileIDIonic', 'upload', [jobInfo, partnerParams, userIdInfo, sidNetInfo]);
};

var smileIDIonic = new SmileIDIonic();
module.exports = smileIDIonic;
