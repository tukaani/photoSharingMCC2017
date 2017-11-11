#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

from json import dumps
from flask_restful import Resource
from flask_jsonpify import jsonpify
import json
from http import HTTPStatus


class ImageProcessing(Resource):
    """
    Process clients image transformation requests
    """

    def get(self):
        return json.dumps({'app': 'photoorganizer'})


class HealthCheck(Resource):
    """
    Check the status of the service
    """

    def get(self):
        """
        """
        status = {
            'status': HTTPStatus.OK
        }
        return json.dumps(status)
