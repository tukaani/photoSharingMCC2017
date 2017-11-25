"""
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import firebase_admin
from firebase_admin import auth, credentials
admin_credentials = credentials.Certificate("./key.json")

firebase_admin.initialize_app(admin_credentials, options={
    "databaseURL": "https://fir-functions-test-c85f9.firebaseio.com"
})


def authenticate_user(id_token):
    """ Autheticate user from the client token"""
    decoded_token = auth.verify_id_token(id_token)
    return decoded_token['uid'] != None


def get_user_by_email(email_id):
    """ Retrieve user info by email """
    user = auth.get_user_by_email(email=email_id)
    return user
