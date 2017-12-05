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

    # check whether the user is part of any active group
    user_group = database.child("users").child(author).child(
        "group").get().val()
    if user_group is not None:
        end_time = database.child('groups').child(
            user_group).child('end_time').get().val()
        end = datetime.datetime.strptime(end_time, "%Y-%m-%d %H:%M:%S.%f")
        if end > datetime.datetime.now():
            raise Exception(
                "user is part of another active group and not allowed to create a new group")
    group_id = database.child('groups').push(data)
    # Add group information to the user collection
    database.child("users").child(author).child("group").set(group_id['name'])
    # Firebase will not allows to create an empty directory
    storage.child("images/" + group_id['name'] + "/.keep").put(".keep")
    return group_id['name'], token


def update(group_id, user_id, user_token):
    """Add an user to the group"""
    token = str(uuid.uuid4())
    data = {"token": token}

    active_token = database.child("groups").child(
        group_id).child("token").get().val()
    if user_token != active_token:
        raise Exception("Invalid one time token")

    # check whether the user is part of any active group
    user_group = database.child("users").child(user_id).child(
        "group").get().val()
    if user_group is not None:
        end_time = database.child('groups').child(
            user_group).child('end_time').get().val()
        end = datetime.datetime.strptime(end_time, "%Y-%m-%d %H:%M:%S.%f")
        if end > datetime.datetime.now():
            raise Exception(
                "user is part of another active group and not allowed to join this group")
        else:
            database.child("users").child(
                user_id).child("group").update(group_id)
    database.child("users").child(user_id).child("group").set(group_id)

    # Update one time token
    database.child("groups").child(group_id).update(data)

    # Update members
    members = database.child("groups").child(
        group_id).child("members").get().val()

    if user_id in members:
        raise Exception("user has already joined the group")

    members.append(user_id)
    database.child("groups").child(
        group_id).child("members").set(members)
    return token


def delete(group_id, user_id):
    """Delete a group (if user is an author of the group or remove the user from members list"""
    author_id = database.child("groups").child(
        group_id).child("creator").get().val()
    database.child("users").child(user_id).child("group").remove()
    if user_id == author_id:
        database.child("groups").child(
            group_id).remove()
        for f in storage.list_files():
            path, file = os.path.split(parse.unquote(f.path))
            print(parse.unquote(f.path))
            if group_id in path:
                p = parse.unquote(f.path).split("/")
                index = p.index("images")
                storage.delete("/".join(p[index:]))
    else:
        members = database.child("groups").child(
            group_id).child("members").get().val()
        members.remove(user_id)
        database.child("groups").child(
            group_id).child("members").set(members)


def housekeeper_cron():
    """Housekeeper cron Job Google app engine triggers for every 60 seconds"""
    try:
        group_ids = database.child("groups").get()
        for grp in group_ids.each():
            batch_delete(group=grp.key())
    except Exception as ex:
        logging.info(ex)


def batch_delete(group):
    """ Delete InActive Groups"""
    try:
        end_time = database.child('groups').child(
            group).child('end_time').get().val()
        end = datetime.datetime.strptime(end_time, "%Y-%m-%d %H:%M:%S.%f")
        if end > datetime.datetime.now():
            print(group + " Group is valid..")
        else:
            print(group + " Ĝroup validity Expired ... Housekeeping(daemon) begins...")
            members = database.child("groups").child(
            group).child("members").get().val()
            for member in members:
                 database.child("users").child(member).child("group").remove()
            for f in storage.list_files():
                path, file = os.path.split(parse.unquote(f.path))
                if group in path:
                    p = parse.unquote(f.path).split("/")
                    index = p.index("images")
                    storage.delete("/".join(p[index:]))
            database.child("groups").child(group).remove()
            print("Removed Inactive groups...")
    except Exception as ex:
        logging.info(ex)


def authenticate_group_member(email_id, password):
    """Authenticate Enduser by email"""
    user = auth.sign_in_with_email_and_password(
        email=email_id, password=password)
    if 'idToken' in user:
        if user['idToken'] is None:
            raise Exception("invalid user idToken")
    else:
        raise Exception("Current user is not part of any group")
    return user['idToken']


def get_group_id(user_id):
    """ Retrieve group_id from user_id"""
    return database.child("users").child(user_id).child("group").get().val()


def retrieve_user_photos(group_id):
    """ Retrive group photos"""
    files = []
    for file in storage.list_files():
        path, file = os.path.split(parse.unquote(file.path))
        if group_id in path:
            files.append(file)
    return files


def get_download_url(group_id, user_token):
    """ Get Firebase Download URL"""
    # shit load of crapy operations performed here. think about restructure the schema.
    urls = []
    for f in storage.list_files():
        path, file_name = os.path.split(parse.unquote(f.path))
        if group_id in path:
            if file_name != "":
                p = parse.unquote(f.path).split("/")
                index = p.index("images")
                print("/".join(p[index:]))
                storage.child("/".join(p[index:])).download("temp/" + file_name)
                data = storage.child(
                    "/".join(p[index:])).put("temp/" + file_name, token=user_token)
                download_url = storage.child(
                    "/".join(p[index:])).get_url(token=data['downloadTokens'])
                if file_name != ".keep":
                    url = {}
                    url['url'] = download_url
                    url['name'] = file_name
                    urls.append(url)
    return urls


def delete_specific_file(group_id, file_name):
    """Delete a specific file from storage"""
    for f in storage.list_files():
        path, file = os.path.split(parse.unquote(f.path))
        if group_id in path:
            if file == file_name:
                p = parse.unquote(f.path).split("/")
                index = p.index("images")
                storage.delete("/".join(p[index:]))


def stream_group_message(message):
    """
    Monitor the group photo sharing
    """
    try:
        if 'data' in message:
            for group in message['data']:
                end_time = database.child('groups').child(
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
                    database.child("groups").child(group).remove()
    except Exception as ex:
        pass
# database.child("Photos").stream(stream_group_message)
