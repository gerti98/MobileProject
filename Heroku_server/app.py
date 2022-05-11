from flask import Flask, request, jsonify
from Utils.model import *
from Utils.general_utils import *
import pickle 
import sys
import logging

app = Flask(__name__)
app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)

svm_model = pickle.load(open("models/svm_model.pkl", 'rb'))
vectorizer = pickle.load(open("models/tfid.pkl", 'rb'))
encoder = pickle.load(open("models/encoder.pkl", 'rb'))

@app.route('/')
def index():
    return "WebAPP used for emotion recognition"

@app.route('/predict_emotion', methods=['POST'])
def predict_emotion():
    request_json = request.json
    print(request_json)
    list_of_msg = request_json['msgs']
    svm_predictions = detect_emotions_with_proba(svm_model, vectorizer, list_of_msg)
    predict_emotion_inv = proba_to_labels(encoder, svm_predictions)
    print(predict_emotion_inv)
    majority_class = get_list_max_instance(predict_emotion_inv)
    return jsonify(majority_class)

if __name__ == '__main__':  
  app.run(debug=True)
