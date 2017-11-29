"""
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import os
import constants
import firebase_admin
from firebase_admin import auth, credentials
admin_credentials = credentials.Certificate("./key.json")

firebase_admin.initialize_app(admin_credentials, options={
    "databaseURL": os.environ.get('DATABASE_URL', None)
})


def authenticate_user(id_token):
    """ Autheticate user from the client token"""
    decoded_token = auth.verify_id_token(id_token)
    return decoded_token['uid'] != None


def get_user_by_email(email_id):
    """ Retrieve user info by email """
    user = auth.get_user_by_email(email=email_id)
    return user
