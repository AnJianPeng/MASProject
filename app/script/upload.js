const AWS = require('aws-sdk');
var uuid = require('uuid');
var url_prefix = "https://b9aknc0duk.execute-api.ap-northeast-1.amazonaws.com/beta/image";
//*/ get reference to S3 client
var s3 = new AWS.S3();
exports.handler = (event, context, callback) => {
     let encodedImage =JSON.parse(event.body).image;
     let decodedImage = Buffer.from(encodedImage, 'base64');
     let id = uuid.v1();
     var filePath = "images/" + id + ".jpg";
     var params = {
       "Body": decodedImage,
       "Bucket": "cs8803mas-mini",
       "Key": filePath
    };
    s3.upload(params, function(err, data){
        let body = {
           "Location": url_prefix + "?key=" + id
        };
        if(err) {
            callback(err, null);
        } else {
            let response = {
        "statusCode": 200,
        "headers": {
            "my_header": "my_value"
        },
        "body": JSON.stringify(body),
        "isBase64Encoded": false
    };
           callback(null, response);
    }
    });

};