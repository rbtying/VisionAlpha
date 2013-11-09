from flask import Flask, request, redirect, url_for, jsonify
from werkzeug import secure_filename

import subprocess
import os
import tempfile
import json
import requests
import xml.etree.ElementTree as ET
from base64 import b64encode

#imgur
client_id = 'c2e9463f5af7c57'
client_secret = '40f05f75d95fa16d6fb75a59638b5220f6eb529c'

#wolfram
appid = 'P7VYW4-68A3854LYU'

headers = {
        'Authorization': 'Client-ID ' + client_id
        }

imgur_api = 'https://api.imgur.com/3/upload.json'
wolfram_api = 'http://api.wolframalpha.com/v2/query'

app = Flask(__name__)
app.debug = True

UPLOAD_FOLDER = '/tmp/flask_upload'
ALLOWED_EXTENSIONS = set(['jpg', 'png', 'gif'])
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/')
def hello_world():
	return 'hi lol'

@app.route('/process', methods=['GET', 'POST'])
def process_file():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            tf = tempfile.NamedTemporaryFile()
            tf.write(file.read())


            tf2 = tempfile.NamedTemporaryFile()

            p = subprocess.Popen(['/home/ubuntu/visionalpha/recognize.sh',
                tf.name, tf2.name], stdout=subprocess.PIPE)
            out, err = p.communicate()

            latex = out.strip()

            resp = requests.post(imgur_api, headers=headers, data = {
                'key': client_secret,
                'image': b64encode(open(tf2.name, 'rb').read()),
                'type': 'base64',
                'title': file.filename
                })
            imgur_resp = json.loads(resp.text)['data']

            wa_resp = None

            if latex:
                wa_resp = dict()
                print 'Processing wolfram'
                resp = requests.get(wolfram_api, params={
                    'appid': appid,
                    'input': latex
                    })
                root = ET.fromstring(resp.text)

                pods = root.findall('.//pod')
                for pod in pods:
                    wa_resp[pod.attrib['title']] = dict()
                    # for img in pod.findall('.//img'):
                    wa_resp[pod.attrib['title']]['img'] = [img.attrib['src'] for img in pod.findall('.//img')]
                    wa_resp[pod.attrib['title']]['text'] = [text.text for text in pod.findall('.//plaintext')]

            json_resp = dict()

            if imgur_resp and 'link' in imgur_resp:
                json_resp['processed_img'] = imgur_resp['link']

            if wa_resp:
                json_resp['wolfram'] = wa_resp

            json_resp['latex'] = latex

            return jsonify(json_resp)
    return '''
    <!doctype html>
    <form action="" method="POST" enctype="multipart/form-data">
    <input type="file" name="file">
    <input type="submit" value="Upload">
    </form>
    '''

if __name__ == '__main__':
	app.run('0.0.0.0')
