'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const gcs = require('@google-cloud/storage')();
const Vision = require('@google-cloud/vision');
const vision = new Vision();

exports.detectfaces = functions.storage.object().onChange(event => {
    const object = event.data;
    if (object.name.indexOf('.keep') === -1) {
        console.log(object.name)
        const image = {
            source: { imageUri: `gs://${object.bucket}/${object.name}` }
        };

        // Check the image content using the Cloud Vision API.
        return vision.faceDetection(image).then(faceDetectionResponse => {
            const faceDetection = faceDetectionResponse[0].faceAnnotations;
            let path = image.source.imageUri.split("/");
            if (faceDetection.length > 0) {
                admin.database().ref('Photos').push({
                    group: path[3],
                    filename: path[4],
                    source: image.source.imageUri,
                    people: true,
                });
            } else {
                admin.database().ref('Photos').push({
                    group: path[3],
                    filename: path[4],
                    source: image.source.imageUri,
                    people: false,
                });
            }
        });
    }

});
