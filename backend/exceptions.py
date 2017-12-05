""""#
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""


class InvalidUsage(Exception):
    """Custom Exceptions to handle user requests"""
    status_code = 400

    def __init__(self, message, status_code=None, payload=None):
        Exception.__init__(self)
        self.message = message
        if status_code is not None:
            self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or ())
        rv['message'] = self.message
        return rv
