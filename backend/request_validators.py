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


def validate_group_create_request(content):
    """ Validate group creation request"""
    if "author" not in content:
        raise Exception("author information is missing")

    if "group_name" not in content:
        raise Exception("Group information is missing")

    if "validity" not in content:
        raise Exception("Group validity is missing")


def validate_group_join_request(content):
    """ Validate group Join request"""
    if "group_id" not in content:
        raise Exception("Group information is missing")

    if "user_id" not in content:
        raise Exception("User information is missing")

    if "token" not in content:
        raise Exception("Group token is missing")


def validate_group_delete_request(content):
    """ Validate group delete request"""
    if "group_id" not in content:
        raise Exception("Group information is missing")

    if "user_id" not in content:
        raise Exception("User information is missing")
