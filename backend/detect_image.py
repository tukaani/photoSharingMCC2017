#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

import base64
from google_service import Service

#ACCESS_TOKEN = os.environ.get('VISION_API')

# TODO: Remove it ..Local Testing
ACCESS_TOKEN = "AIzaSyD_xI4WpcVcd7Um7cPqv2c57SSKsqbz94U"


def detect_face(image):
    """Run a face detection request on a single image"""

    service = Service('vision', 'v1', access_token=ACCESS_TOKEN)
    image_content = base64.b64encode(image).decode('UTF-8')

    body = {
        'requests': [{
            'image': {
                'content': image_content,
            },
            'features': [{
                'type': 'FACE_DETECTION',
                'maxResults': 1,
            }]

        }]
    }
    response = service.execute(body=body)

    # Checks the response for facial properties
    if 'faceAnnotations' in response['responses'][0]:
        return True

    return False
