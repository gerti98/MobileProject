import requests
import json
import os

LOCAL_URL = 'http://127.0.0.1:5000/predict_text_emotion'
HEROKU_URL = 'https://mobile-group3.herokuapp.com/predict_text_emotion'
TEST_TEXT_FILE_PATH = "./UserLabels/"

if __name__ == "__main__":
    for file in os.listdir(TEST_TEXT_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_TEXT_FILE_PATH + file))
            list_of_msgs_with_labels = text_file['text_labels']
            
            list_of_msgs = [i[0] for i in list_of_msgs_with_labels]
            list_of_labels = [i[1] for i in list_of_msgs_with_labels]

            response = requests.post(LOCAL_URL, json={
                "msgs": list_of_msgs
            })

            list_of_model_labels = response.json()

            for i in range(len(list_of_msgs_with_labels)):
                print(
                    f"User msg: {list_of_msgs_with_labels[i][0]}, User label: {list_of_msgs_with_labels[i][1]}, Model label: {list_of_model_labels[i]}")



