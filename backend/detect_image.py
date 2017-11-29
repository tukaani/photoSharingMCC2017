"""
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import os
import constants
from google_service import Service


def detect_face(image_content):
    """Run a face detection request on a single image"""

    service = Service(
        'vision', 'v1', access_token=os.environ.get('VISION_API_KEY', None))
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
