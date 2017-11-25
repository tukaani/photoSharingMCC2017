""""#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import os
import uuid
from pyrebase import pyrebase


API_KEY = os.environ.get('API_KEY')
AUTH_DOMAIN = os.environ.get('AUTH_DOMAIN')
DATABASE_URL = os.environ.get('DATABASE_URL')
STORAGE_BUCKET = os.environ.get('STORAGE_BUCKET')

firebase_config = {
    "apiKey": "AIzaSyAGqgZpYbVbn2SwH1T-RjXQ3zJ0rttOY5I",
    "authDomain": "fir-functions-test-c85f9.firebaseapp.com",
    "databaseURL": "https://fir-functions-test-c85f9.firebaseio.com",
    "projectId": "fir-functions-test-c85f9",
    "storageBucket": "fir-functions-test-c85f9.appspot.com",
    "serviceAccount": os.path.dirname(os.path.abspath(__file__)) + "/key.json"
}


firebase = pyrebase.initialize_app(firebase_config)

db = firebase.database()
data = {"name": "Mortimer 'Morty' Smith"}
db.child("users").push(data)

print(uuid.uuid4())
