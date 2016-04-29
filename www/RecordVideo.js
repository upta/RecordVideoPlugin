var exec = require("cordova/exec");

var RecordVideo = {
    play: function(url) {
        exec(null, null, "RecordVideo", "record", []);
    }
};

module.exports = RecordVideo;