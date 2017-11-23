'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const gcs = require('@google-cloud/storage')();
const Vision = require('@google-cloud/vision');
const vision = new Vision();

exports.helloWorld = functions.https.onRequest((request, response) => {
    response.send("Hello from Firebase!");
});

exports.detectFaces = functions.storage.object().onChange(event => {
    const object = event.data;
    if (object.resourceState === 'not_exists') {
        return console.log('This is a deletion event.');
    } else if (!object.name) {
        return console.log('This is a deploy event.');
    }

    const image = {
        source: { imageUri: `gs://${object.bucket}/${object.name}` }
    };

    // Check the image content using the Cloud Vision API.
    return vision.faceDetection(image).then(faceDetectionResponse => {
        const faceDetection = faceDetectionResponse[0].faceAnnotations;
        if (faceDetection.length > 0) {
            console.log("Contains People")
            admin.database().ref('Image_Info').push({
                name: image.source.imageUri,
                people: 1,
            });
        } else {
            admin.database().ref('Image_Info').push({
                name: image.source.imageUri,
                people: 0,
            });
        }
    });
});