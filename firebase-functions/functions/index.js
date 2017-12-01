'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
const gcs = require('@google-cloud/storage')();
const Vision = require('@google-cloud/vision');
const vision = new Vision();
const fs = require('fs');
const path = require('path');
var Jimp = require("jimp");
const spawn = require('child-process-promise').spawn;


exports.detectFaces = functions.database.ref('/photos/{groupid}/{photoid}/files/full')
    .onWrite(event => {
  
    var dbPath = String(event.data.ref.parent);
    var baseUrl = 'gs://' + dbPath.substring(8).split('.')[0] + '.appspot.com/images/';
    var groupid = dbPath.split('/')[4];
    var photoid = dbPath.split('/')[5];
    var filename = event.data.val();
    var url = baseUrl + dbPath.split('/')[4] + '/' + filename;

        const image = {
            source: { imageUri: url }
        };

        // Check the image content using the Cloud Vision API.
        return vision.faceDetection(image).then(faceDetectionResponse => {
            const faceDetection = faceDetectionResponse[0].faceAnnotations;
            if (faceDetection.length > 0) {
              return event.data.ref.parent.parent.child('people').set(true);
            } else {
              return event.data.ref.parent.parent.child('people').set(false);
            }
        })
});




exports.createResolutions = functions.database.ref('/photos/{groupid}/{photoid}/files/full')
    .onWrite(event => {
      console.log("Creating resolutions..");

      const groupid = event.params.groupid
      const filename = event.data.val()
    
      //get your project storage bucket id
      const storageBucket = functions.config().firebase.storageBucket
      //path to the full image
      const imagePath = '/images/' + groupid +'/' + filename;
      //Path to the group folder
      const path = '/images/' + groupid +'/';

      const lowFilename = 'low_' + filename;
      const highFilename = 'high_' + filename;
      const resolution = 0;

      //open bucket
      const bucket = gcs.bucket(storageBucket)

      const tempOriginalFilename = '/tmp/tempFile'
      const tempPath = '/tmp/';
      const metadata = { contentType: 'image/jpeg' };

      //Download file from bucket.
      return bucket.file(imagePath).download({ destination: tempOriginalFilename})
      .catch((err) => {
        console.error('Failed to download file.', err);
        //return Promise.reject(err);
        }).then(() => {
        console.log("Transforming low resolution...");
        return spawn('convert', [tempOriginalFilename, '-resize', '640x480', tempPath + lowFilename]) 
        }).then(() => {

        console.log("Transforming high resolution...");
        return spawn('convert', [tempOriginalFilename, '-resize', '1280x960', tempPath + highFilename])
        }).then(() => {
          console.log('Images resized');
          return bucket.upload(tempPath + lowFilename, { destination: path + lowFilename, metadata:metadata })
            .catch((err) => {
              console.error('Failed to upload resized image.', err);
              return Promise.reject(err);
            });

        }).then(() => {
          return bucket.upload(tempPath + highFilename, { destination: path + highFilename, metadata:metadata })
            .catch((err) => {
              console.error('Failed to upload resized image.', err);
              return Promise.reject(err);
            });
        })
        .then(() => {
          return new Promise((resolve, reject) => {
            Jimp.read(tempOriginalFilename).then(function (image) {
              var w = image.bitmap.width; //  width of the image
              var h = image.bitmap.height; // height of the image
              console.log('Calculation resolution..');
              if(w <= 640) {
                return resolve(event.data.ref.parent.parent.child('resolution').set(1));
              } else if(w <= 1280) {
                return resolve(event.data.ref.parent.parent.child('resolution').set(2));
              } else {
                return resolve(event.data.ref.parent.parent.child('resolution').set(3))
              }
            }).catch(function (err) {
                console.error(err);
                reject(err)
            });
            })
        })
        // Set filenames for the low and high images to RT DB
        .then(() => {console.log("writing low filename");
          return event.data.ref.parent.child('low').set(lowFilename);})
        .then(() => {console.log("writing high filename");
          return event.data.ref.parent.child('high').set(highFilename);})
        // Destroy temp files
        .then(() => fs.unlinkSync(tempPath + lowFilename))
        .then(() => fs.unlinkSync(tempPath + highFilename))
        .then(() => fs.unlinkSync(tempOriginalFilename));
    
  });
 