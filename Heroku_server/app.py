from flask import Flask, request, jsonify

import pickle 
import sys
import logging
import random
import os

from Utils.model import *
from Utils.convert_wavs import *


app = Flask(__name__)
app.logger.addHandler(logging.StreamHandler(sys.stdout))
app.logger.setLevel(logging.ERROR)

# Loading models, vectorizer and encoder -> The vectorizer and encoder are used for text preprocessing
svm_model = pickle.load(open("models/svm_model.pkl", 'rb'))
mlp_model = pickle.load(open("models/mlp_model.pkl", 'rb'))
vectorizer = pickle.load(open("models/tfid.pkl", 'rb'))
encoder = pickle.load(open("models/encoder.pkl", 'rb'))

# Root API
@app.route('/')
def index():
    return "WebAPP used for emotion recognition"

# Predict text emotion API
@app.route('/predict_text_emotion', methods=['POST'])
def predict_text_emotion():
    """ 
    INPUT:  The format of the json in the request is:
                {
                    msgs: ['msg_1', 'msg_2', ..., 'msg_n']
                }
    OUTPUT: The API returns a json containing the list of emotions based on the list of msgs:
                [emotion_1, emotion_2, ..., emotion_n]
    """

    request_json = request.json # Get the request in json format
    print(request_json)
    list_of_msg = request_json['msgs'] # Get the list of msgs

    # Predictions based on the msgs
    if list_of_msg:
        svm_predictions = detect_emotions_with_proba(svm_model, vectorizer, list_of_msg)
        predict_emotion_inv = proba_to_labels(encoder, svm_predictions)
        print(predict_emotion_inv)
    else:
        predict_emotion_inv = []

    return jsonify(predict_emotion_inv) # Return the list of emotions predictions


# Predict audio emotion API
@app.route('/predict_voice_emotion', methods=['POST'])
def predict_voice_emotion():
    """ 
    INPUT:  The request contains the file audio used for the prediction  (.wav file)
    
    OUTPUT: The API returns the a json containing the emotion predicted from the model
    """

    audio_file = request.files['file'] # Get the file

    file_name = str(random.randint(0,100000)) # Generate a random filename
    file_name_to_convert = file_name + '_toconvert.wav' # Filename of the file that has to be converted
    file_name_wav =  file_name + '.wav' # Filename of the file after the conversion 
    audio_file.save(file_name_to_convert) # Save the file to be converted 
    
    convert_wavs(file_name_to_convert, file_name_wav) # Convert the file and save it 

    # Prediction based on the audio
    features = extract_feature(file_name_wav, mfcc=True, chroma=True, mel=True)
    prediction = mlp_model.predict(features.reshape(1,-1))[0]

    # To return the same emotions as text classifier, we assume that the class "happy" and "joy" are the same thing. 
    if prediction == 'happy':
        prediction = "joy"

    # Remove the files generated
    os.remove(file_name_to_convert)
    os.remove(file_name_wav)

    return jsonify(prediction) # Return the emotion predicted


if __name__ == '__main__':  
  app.run(debug=True)




