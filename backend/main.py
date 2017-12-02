""""#
# Contributors : MCC-2017-G12
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
"""
import logging
import base64
import http
import datetime
import requests
from flask import Flask, request, session, redirect, url_for, jsonify
from flask import render_template
import detect_image
import groups
import users
from request_validators import is_authorized_user, validate_authorization_header, validate_group_create_request, validate_group_join_request, validate_group_delete_request

app = Flask(__name__)
app.secret_key = 'F12Zr47j3yX R~X@lH!jmM]Lwf/,?KT'


@app.before_request
def session_management():
    """
    Initialize session data
    """
    session.permanent = True


@app.route('/photoorganizer/api/v1.0/status', methods=['GET'])
def status():
    """Status photoorganizer application"""
    stats = {
        'status': 'RUNNING',
        'version': 'v1.0',
        'timestamp': datetime.datetime.now()
    }
    return jsonify(stats)


@app.route('/photoorganizer/api/v1.0/group/create', methods=['POST'])
def create_group():
    """Create a photo sharing group"""
    try:
        validate_authorization_header(request.headers)
        is_authorized_user(request.headers['Authorization'])

        content = request.get_json()
        validate_group_create_request(content=content)
        author = content['author']
        group_name = content['group_name']
        valid_hours = content['validity']
        group_id, token = groups.create(
            group_name=group_name, validity=valid_hours, author=author)
        return jsonify({'group_id': str(group_id), 'token': token})
    except Exception as ex:
        logging.exception(ex)
    return jsonify({
        'status': http.HTTPStatus.BAD_REQUEST,
        'error': "Bad Request"
    })


@app.route('/photoorganizer/api/v1.0/group/join', methods=['POST'])
def join_group():
    """ Allow members to join a group """
    try:
        validate_authorization_header(request.headers)
        is_authorized_user(request.headers['Authorization'])

        content = request.get_json()
        validate_group_join_request(content=content)
        group_id = content['group_id']
        user_id = content['user_id']
        token = content['token']
        token = groups.update(
            group_id=group_id, user_id=user_id, user_token=token)
        return jsonify({'refreshedtoken': token})
    except Exception as ex:
        logging.exception(ex)
    return jsonify({
        'status': http.HTTPStatus.BAD_REQUEST,
        'error': "Bad Request"
    })


@app.route('/photoorganizer/api/v1.0/group/delete', methods=['POST'])
def delete_group():
    """" Delete the group and images """
    try:
        validate_authorization_header(request.headers)
        is_authorized_user(request.headers['Authorization'])

        content = request.get_json()
        validate_group_delete_request(content=content)
        group_id = content['group_id']
        user_id = content['user_id']
        groups.delete(group_id=group_id, user_id=user_id)
        return jsonify({'status': "success"})
    except Exception as ex:
        logging.exception(ex)
        return jsonify({
            'status': http.HTTPStatus.BAD_REQUEST,
            'error': "Bad Request"
        })


@app.route('/photoorganizer/api/v1.0/process', methods=['GET', 'POST'])
def process_image_v1():
    """Read the image from request info(url) and respond with base64 encoded"""
    try:
        image_url = request.args.get('image_url')
        data = base64.b64encode(requests.get(
            image_url).content).decode('UTF-8')
        is_contains_people = detect_image.detect_face(
            data)
        response = {
            'status': http.HTTPStatus.OK,
            'is_contains_people': is_contains_people
        }
        return jsonify({'response': response})
    except Exception as ex:
        logging.exception(ex)
    return jsonify({
        'status': http.HTTPStatus.BAD_REQUEST,
        'error': "Bad data"
    })


@app.route('/photoorganizer/api/v2.0/process', methods=['POST'])
def process_image_v2():
    """Read the base64 encoded image from request info and respond with base64 encoded"""
    try:
        content = request.get_json()
        is_contains_people = detect_image.detect_face(content['data'])
        response = {
            'status': http.HTTPStatus.OK,
            'is_contains_people': is_contains_people
        }
        return jsonify({'response': response})
    except Exception as ex:
        logging.exception(ex)
    return jsonify({
        'status': http.HTTPStatus.BAD_REQUEST,
        'error': "Bad data"
    })


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
        password = request.form['password']
        groups.authenticate_group_member(email_id=email, password=password)
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
def server_error_500(error):
    """ Handle Internal server error """
    logging.exception('An error occurred during a request..' + str(error))
    return render_template("filemanager/failure.html")


@app.errorhandler(403)
def server_error_403(error):
    """ Forbidden """
    logging.exception('An error occurred during a request..' + str(error))
    return jsonify({
        'status': http.HTTPStatus.FORBIDDEN,
        'error': "access forbidden"
    })


@app.route('/photoorganizer/housekeeping', methods=['GET', 'POST'])
def housekeeping():
    """ google cron(housekeeping)"""
    try:
        groups.housekeeper_cron()
        return jsonify({'status': "success"})
    except Exception as ex:
        logging.info(ex)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
