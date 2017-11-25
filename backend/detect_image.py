"""
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
from google_service import Service

#ACCESS_TOKEN = os.environ.get('VISION_API')

# TODO: Remove it ..Local Testing
ACCESS_TOKEN = "AIzaSyAcSZ3-4igzr60jZzSuRa7tUFHr02c9OPQ"


def detect_face(image_content):
    """Run a face detection request on a single image"""

    service = Service('vision', 'v1', access_token=ACCESS_TOKEN)
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
