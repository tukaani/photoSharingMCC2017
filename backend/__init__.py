#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

import os
import pyrebase

API_KEY = os.environ.get('API_KEY')
AUTH_DOMAIN = os.environ.get('AUTH_DOMAIN')
DATABASE_URL = os.environ.get('DATABASE_URL')
STORAGE_BUCKET = os.environ.get('STORAGE_BUCKET')

firebase_config = {
    "apiKey": "AIzaSyCI5mlV-G0tu0u0wHQr5AdWHGWoXpC_bbo",
    "authDomain": "polar-scene-149514.firebaseapp.com",
    "databaseURL": "https://polar-scene-149514.firebaseio.com",
    "projectId": "polar-scene-149514",
    "storageBucket": "polar-scene-149514.appspot.com",
    "serviceAccount": os.path.dirname(os.path.abspath(__file__)) + "/polar-scene-149514-firebase.json"
}

firebase = pyrebase.initialize_app(firebase_config)

database = firebase.database()
auth = firebase.auth()
storage = firebase.storage()