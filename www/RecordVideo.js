var exec = require("cordova/exec");

var RecordVideo = {
    record: function(success, error) {
        exec(success, error, "RecordVideo", "record", []);
    }
};

module.exports = RecordVideo;