from flask import Flask, request, jsonify
from Utils.model import *
import pickle 
import sys
import logging

app = Flask(__name__)

app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)

svm_model = pickle.load(open("svm_model.pkl", 'rb'))
vectorizer = pickle.load(open("tfid.pkl", 'rb'))
encoder = pickle.load(open("encoder.pkl", 'rb'))

@app.route('/')
def index():
    return "WebAPP used for emotion recognition"

@app.route('/predict_emotion')
def predict_emotion():
    msg = request.args.get('message')
    msg = [msg]
    svm_predictions = detect_emotions_with_proba(svm_model, vectorizer, msg)
    predict_emotion_inv = proba_to_labels(encoder, svm_predictions)
    return jsonify(predict_emotion_inv)

if __name__ == '__main__':  
  app.run(debug=True)
