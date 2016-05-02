var exec = require("cordova/exec");

var RecordVideo = {
    record: function(url, success, error) {
        exec(success, error, "RecordVideo", "record", []);
    }
};

module.exports = RecordVideo;