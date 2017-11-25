#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
import datetime
import uuid
import os
import pyrebase

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
database = firebase.database()
auth = firebase.auth()
storage = firebase.storage()


def create(group_name, validity):
    """Create a directory to store group images"""
    token = str(uuid.uuid4())
    data = {"name": group_name,
            "start_time": str(datetime.datetime.now()),
            "end_time": str(datetime.datetime.now() + datetime.timedelta(hours=validity)),
            "token": token}
    group_id = database.child("group").push(data)
    #storage.child(group_name + "/.keep").put(".keep")
    return group_id['name'], token


def update(group_id):
    """Create a directory to store group images"""
    token = str(uuid.uuid4())
    data = {"token": token}
    database.child("group").child(group_id).update(data)
    return token
