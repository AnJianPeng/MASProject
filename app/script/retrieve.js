const AWS = require('aws-sdk');
//*/ get reference to S3 client
var s3 = new AWS.S3();
exports.handler = (event, context, callback) => {
    var filePath = "images/" + event.queryStringParameters.key + ".jpg";
    var params = {
    "Bucket": "cs8803mas-mini",
    "Key": filePath
    };
    s3.getObject(params, function(err, data){
        const contentType = "image/jpeg";
        const image = data.Body;
        if(err) {
           callback(err, null);
        } else {
           let response = {
        "statusCode": 200,
        "headers": {
            "Content-Type": contentType
        },
        "body": image.toString("base64"),
        "isBase64Encoded": true
    };
           callback(null, response);
    }
    });

};