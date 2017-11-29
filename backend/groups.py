"""
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import datetime
import uuid
import os
import constants
from urllib import parse
import pyrebase
import json
from PIL import Image
import time
import logging

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
            "creator": author,
            "start_time": str(datetime.datetime.now()),
            "end_time": str(datetime.datetime.now() + datetime.timedelta(minutes=validity)),
            "token": token,
            "members": [author]}
    group_id = database.child('Photos').push(data)

    # Firebase will not allows to create an empty directory
    storage.child("images/" + group_id['name'] + "/.keep").put(".keep")
    return group_id['name'], token


def update(group_id, user_id):
    """Add an user to the group"""
    token = str(uuid.uuid4())
    data = {"token": token}

    # Update one time token
    database.child("Photos").child(group_id).update(data)

    # Update members
    members = database.child("Photos").child(
        group_id).child("members").get().val()

    if user_id in members:
        raise Exception("user has already joined the group")

    members.append(user_id)
    database.child("Photos").child(
        group_id).child("members").set(members)
    return token


def delete(group_id, user_id):
    """Delete a group (if user is an author of the group or remove the user from members list"""
    author_id = database.child("Photos").child(
        group_id).child("creator").get().val()

    if user_id == author_id:
        database.child("Photos").child(
            group_id).remove()
        # FIXME: storage.list_files() can be optimized by having a metadata table which stores image urls for the group
        for f in storage.list_files():
            path, file = os.path.split(parse.unquote(f.path))
            if group_id in path:
                storage.delete("images/" + group_id + "/" + file)
    else:
        members = database.child("Photos").child(
            group_id).child("members").get().val()
        members.remove(user_id)
        database.child("Photos").child(
            group_id).child("members").set(members)


def notify_housekeeper_daemon(message):
    """ Notify housekeeper daemon"""
    try:
        logging.info(
            "First group is created.. closing the housekeeper stream.." + message)
        housekeeper.close()
    except Exception as ex:
        logging.info("Starts housekeeper daemon")
        housekeeper_daemon()


def housekeeper_daemon():
    """Housekeeper Daemon runs for every 60 seconds"""
    try:
        while True:
            group_ids = database.child("Photos").get()
            for grp in group_ids.each():
                batch_delete(group=grp.key())
            time.sleep(60) #TODO:configure this value sensibily
    except Exception as ex:
        pass


def batch_delete(group):
    """ Delete InActive Groups"""
    end_time = database.child('Photos').child(
        group).child('end_time').get().val()
    end = datetime.datetime.strptime(end_time, "%Y-%m-%d %H:%M:%S.%f")
    if end > datetime.datetime.now():
        print("Group is valid..")
    else:
        print("Ĝroup validity Expired ... Housekeeping(daemon) begins...")
        for file in storage.list_files():
            path, file = os.path.split(parse.unquote(file.path))
            if group in path:
                storage.delete("images/" + group + "/" + file)
        database.child("Photos").child(group).remove()
        print("Removed Inactive groups...")


def stream_group_message(message):
    """
    Monitor the group photo sharing
    """
    try:
        if 'data' in message:
            for group in message['data']:
                end_time = database.child('Photos').child(
                    group).child('end_time').get().val()
                end = datetime.datetime.strptime(
                    end_time, "%Y-%m-%d %H:%M:%S.%f")

                if end > datetime.datetime.now():
                    print("Group is Valid..")
                else:
                    print("Expired ... Housekeeping(stream) begins...")
                    for file in storage.list_files():
                        path, file = os.path.split(parse.unquote(file.path))
                        if group in path:
                            storage.delete("images/" + group + "/" + file)
                    database.child("Photos").child(group).remove()
    except Exception as ex:
        pass
#database.child("Photos").stream(stream_group_message)

housekeeper = database.child("Photos").stream(notify_housekeeper_daemon)
