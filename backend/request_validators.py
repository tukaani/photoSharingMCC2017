"""
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#
"""
import users


def validate_authorization_header(headers):
    """Check the exixtence of Authorization Header"""
    if 'Authorization' not in headers:
        raise Exception("Invalid Request")


def is_authorized_user(token):
    """Validate the authorization header"""
    if not users.authenticate_user(id_token=token):
        raise Exception("Invalid Authorization token")
