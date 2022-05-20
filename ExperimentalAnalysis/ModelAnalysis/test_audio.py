import requests
import os
import json 

HEROKU_URL = 'https://mobile-group3.herokuapp.com/predict_voice_emotion'
LOCAL_URL = 'http://127.0.0.1:5000/predict_voice_emotion'
TEST_AUDIO_FILE_PATH = "./UserLabels/"

if __name__ == "__main__":
    for file in os.listdir(TEST_AUDIO_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_AUDIO_FILE_PATH + file))
            list_of_audio_with_labels = text_file['audio_labels']

            list_of_audio = [i[0] for i in list_of_audio_with_labels]
            list_of_labels = [i[1] for i in list_of_audio_with_labels]

            list_of_model_labels = []

            for audio in list_of_audio:
                audio_file = open(TEST_AUDIO_FILE_PATH + audio, "rb")
                values = {
                    "file": (TEST_AUDIO_FILE_PATH + audio, audio_file, "audio/wav")
                }
                response = requests.post(LOCAL_URL, files=values)
                list_of_model_labels.append(response.json())


            for i in range(len(list_of_audio_with_labels)):
                print(
                    f"User msg: {list_of_audio_with_labels[i][0]}, User label: {list_of_audio_with_labels[i][1]}, Model label: {list_of_model_labels[i]}")



            