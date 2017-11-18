#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#

import base64
import requests


class Service(object):
    """
    Makes service calls to Cloud Vision API
    """

    def __init__(self, service_name, version, access_token):
        self.url = 'https://{}.googleapis.com/{}/images:annotate?key={}'.format(
            service_name, version, access_token)

    def execute(self, body):
        """
        POST image to vision Endpoint
        """
        header = {'Content-Type': 'application/json'}
        response = requests.post(self.url, headers=header, json=body)
        return response.json()


def encode_image(image):
    """
    Encode image as base64 data
    """
    image_content = image.read()
    return base64.b64encode(image_content).decode()
