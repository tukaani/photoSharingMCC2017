#
# Authors: Kalaiarasan Saminathan <kalaiarasan.saminathan@aalto.fi>,
#          Tuukka Rouhiainen <tuukka.rouhiainen@gmail.com>
#
# Copyright (c) 2017 Aalto University, Finland
#                    All rights reserved
import logging
from flask import Flask, request, session, redirect, url_for, jsonify
from flask import render_template

app = Flask(__name__)
app.secret_key = 'F12Zr47j3yX R~X@lH!jmM]Lwf/,?KT'


@app.before_request
def session_management():
    session.permanent = True


@app.route('/photoorganizer/api/v1.0/process', methods=['POST', 'GET'])
def process_images():
    """Read the image from request and respond with base64 encoded"""

    tasks = [
        {
            'id': 1,
            'title': u'Buy groceries',
            'description': u'Milk, Cheese, Pizza, Fruit, Tylenol',
            'done': False
        },
        {
            'id': 2,
            'title': u'Learn Python',
            'description': u'Need to find a good Python tutorial on the web',
            'done': False
        }
    ]
    return jsonify({'tasks': tasks})


@app.route('/photoorganizer/api/v1.0/status')
def status():
    """Status photoorganizer application"""
    status = {
        'status': 'running',
        'version': 'v1.0'
    }
    return jsonify(status)


@app.route('/')
def index():
    if "user" in session:
        return render_template('filemanager/dashboard.html')
    return render_template('filemanager/login.html')


@app.route('/files', methods=['GET'])
def files():
    try:
        uploaded_files = []
    except Exception as e:
        logging.exception(e)
        return render_template("filemanager/failure.html")
    return render_template("filemanager/files.html", uploaded_files=uploaded_files)


@app.route('/login', methods=['POST'])
def login():
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
    session.clear()
    return redirect(url_for('index'))


@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return render_template("filemanager/failure.html")


if __name__ == '__main__':
    # TODO: Remove the debug mode
    app.run(host='0.0.0.0', port=8080, debug=True)
