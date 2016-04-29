var exec = require("cordova/exec");

var RecordVideo = {
    record: function(url) {
        exec(null, null, "RecordVideo", "record", []);
    }
};

module.exports = RecordVideo;