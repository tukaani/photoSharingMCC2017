#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

from flask import Flask, request
from flask_restful import Api, Resource
from json import dumps
from flask_jsonpify import jsonpify
from app.views import ImageProcessing, HealthCheck, Index

app = Flask(__name__)

# api is a collection of objects, where each object contains a specific functionality (GET, POST, etc)
api = Api(app)


# Photo Organizer HTTP Endpoints
api.add_resource(Index, '/')
api.add_resource(HealthCheck, '/photoorganizer/api/v1.0/status')
api.add_resource(ImageProcessing, '/photoorganizer/api/v1.0/process')
