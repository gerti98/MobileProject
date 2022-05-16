from fileinput import filename
from flask import Flask, request, jsonify
from Utils.model import *
from Utils.general_utils import *
from audio_converter import *
import pickle 
import sys
import logging
import random
import os
import time


app = Flask(__name__)
app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)

svm_model = pickle.load(open("models/svm_model.pkl", 'rb'))
mlp_model = pickle.load(open("models/mlp_model.pkl", 'rb'))
vectorizer = pickle.load(open("models/tfid.pkl", 'rb'))
encoder = pickle.load(open("models/encoder.pkl", 'rb'))

@app.route('/')
def index():
    return "WebAPP used for emotion recognition"

@app.route('/predict_text_emotion', methods=['POST'])
def predict_text_emotion():
    request_json = request.json
    print(request_json)
    list_of_msg = request_json['msgs']
    svm_predictions = detect_emotions_with_proba(svm_model, vectorizer, list_of_msg)
    predict_emotion_inv = proba_to_labels(encoder, svm_predictions)
    print(predict_emotion_inv)
    return jsonify(predict_emotion_inv)

@app.route('/predict_voice_emotion', methods=['POST'])
def predict_voice_emotion():
    time_counter = 0
    audio_file = request.files['file']
    file_name = str(random.randint(0,100000))
    file_name_aac = file_name + '.aac'
    file_name_wav =  file_name + '.wav'
    audio_file.save(file_name_aac)

    from_aac_to_wav(file_name_aac, file_name_wav)

    features = extract_feature(file_name_wav, mfcc=True, chroma=True, mel=True)
    prediction = mlp_model.predict(features.reshape(1,-1))[0]

    os.remove(file_name_aac)
    os.remove(file_name_wav)

    return jsonify(prediction)


if __name__ == '__main__':  
  app.run(debug=True)




