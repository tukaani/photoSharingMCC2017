#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved

import logging
from flask import Flask, request, session, redirect, url_for, jsonify
from flask import render_template
import detect_image
import requests


app = Flask(__name__)
app.secret_key = 'F12Zr47j3yX R~X@lH!jmM]Lwf/,?KT'


@app.before_request
def session_management():
    """
    Initialize session data
    """
    session.permanent = True


@app.route('/photoorganizer/api/v1.0/process', methods=['GET', 'POST'])
def process_image():
    """Read the image from request info and respond with base64 encoded"""
    try:
        image_url = request.args.get('image_url')
        is_contains_people = detect_image.detect_face(
            requests.get(image_url).content)
        response = {
            'image_url': image_url,
            'is_contains_people': is_contains_people
        }
        return jsonify({'response': response})
    except Exception as ex:
        logging.exception(ex)


@app.route('/photoorganizer/api/v1.0/status', methods=['GET'])
def status():
    """Status photoorganizer application"""
    status = {
        'status': 'running',
        'version': 'v1.0'
    }
    return jsonify(status)


@app.route('/photoorganizer/api/v1.0/group/create', methods=['GET', 'POST'])
def create_group():
    """Create a photo sharing group"""
    return jsonify({'status': "Not implemented"})


@app.route('/photoorganizer/api/v1.0/group/join', methods=['GET', 'POST'])
def join_group():
    """ Allow members to join a group """
    return jsonify({'status': "Not implemented"})


@app.route('/photoorganizer/api/v1.0/group/delete', methods=['GET', 'POST'])
def delete_group():
    """" Delete the group and images """
    return jsonify({'status': "Not implemented"})


@app.route('/')
def index():
    """Loads Landing page"""
    if "user" in session:
        return render_template('filemanager/dashboard.html')
    return render_template('filemanager/login.html')


@app.route('/files', methods=['GET'])
def files():
    """List user uploaded files"""
    try:
        uploaded_files = []
    except Exception as ex:
        logging.exception(ex)
        return render_template("filemanager/failure.html")
    return render_template("filemanager/files.html", uploaded_files=uploaded_files)


@app.route('/login', methods=['POST'])
def login():
    """Initiate user session"""
    try:
        email = request.form['email']
        input_password = request.form['password']
        session.clear()
        session["user"] = email
        return render_template("filemanager/dashboard.html")
    except Exception as e:
        logging.exception(e)
    return render_template("filemanager/failure.html")


@app.route('/logout', methods=['GET'])
def logout():
    """ Terminte user session(login)"""
    session.clear()
    return redirect(url_for('index'))


@app.errorhandler(500)
def server_error(error):
    """ Handle Internal server error """
    logging.exception('An error occurred during a request..' + str(error))
    return render_template("filemanager/failure.html")


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
