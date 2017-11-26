"""
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import datetime
import uuid
import os
import constants
from urllib import parse
import pyrebase


firebase_config = {
    "apiKey": os.environ.get('FIREBASE_API_KEY', None),
    "authDomain": os.environ.get('AUTH_DOMAIN', None),
    "databaseURL": os.environ.get('DATABASE_URL', None),
    "projectId": os.environ.get('PROJECT_ID', None),
    "storageBucket": os.environ.get('STORAGE_BUCKET', None),
    "serviceAccount": "./key.json"
}

firebase = pyrebase.initialize_app(firebase_config)
database = firebase.database()
auth = firebase.auth()
storage = firebase.storage()


def create(group_name, validity, author):
    """Create a group and directory to store group images"""
    token = str(uuid.uuid4())
    data = {"name": group_name,
            "author": author,
            "start_time": str(datetime.datetime.now()),
            "end_time": str(datetime.datetime.now() + datetime.timedelta(hours=validity)),
            "token": token,
            "members": [author]}
    group_id = database.child("group").push(data)

    # Firebase will not allows to create an empty directory
    storage.child(group_id['name'] + "/.keep").put(".keep")
    return group_id['name'], token


def update(group_id, user_id):
    """Add an user to the group"""
    token = str(uuid.uuid4())
    data = {"token": token}

    # Update one time token
    database.child("group").child(group_id).update(data)

    # Update members
    members = database.child("group").child(
        group_id).child("members").get().val()

    if user_id in members:
        raise Exception("user has already joined the group")

    members.append(user_id)
    database.child("group").child(
        group_id).child("members").set(members)
    return token


def delete(group_id, user_id):
    """Delete a group (if user is an author of the group or remove the user from members list"""
    author_id = database.child("group").child(
        group_id).child("author").get().val()

    if user_id == author_id:
        database.child("group").child(
            group_id).remove()
        # FIXME: storage.list_files() can be optimized by having a metadata table which stores image urls for the group
        for file in storage.list_files():
            path, file = os.path.split(parse.unquote(file.path))
            if group_id in path:
                storage.delete(group_id + "/" + file)
    else:
        members = database.child("group").child(
            group_id).child("members").get().val()
        members.remove(user_id)
        database.child("group").child(
            group_id).child("members").set(members)
