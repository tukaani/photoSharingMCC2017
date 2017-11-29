""""#
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import os
import uuid
import constants
from pyrebase import pyrebase

firebase_config = {
    "apiKey": os.environ.get('FIREBASE_API_KEY', None),
    "authDomain": os.environ.get('AUTH_DOMAIN', None),
    "databaseURL": os.environ.get('DATABASE_URL', None),
    "projectId": os.environ.get('PROJECT_ID', None),
    "storageBucket": os.environ.get('STORAGE_BUCKET', None),
    "serviceAccount": "./key.json"
}


firebase = pyrebase.initialize_app(firebase_config)

db = firebase.database()
data = {"name": "Mortimer 'Morty' Smith"}
db.child("users").push(data)

print(uuid.uuid4())
