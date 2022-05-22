import requests
import os
import json 
import pandas as pd

HEROKU_URL_TEXT = 'https://mobile-group3.herokuapp.com/predict_text_emotion'
HEROKU_URL_VOICE = 'https://mobile-group3.herokuapp.com/predict_voice_emotion'

LOCAL_URL_TEXT = 'http://127.0.0.1:5000/predict_text_emotion'
LOCAL_URL_VOICE = 'http://127.0.0.1:5000/predict_voice_emotion'

TEST_FILE_PATH = "./UserLabels/"

def test_text(urltype = "local"):

    if urltype == "local":
        url = LOCAL_URL_TEXT
    elif urltype == "heroku": 
        url = HEROKU_URL_TEXT
    else:
        print("Inserted a wrong urltype, choose one between 'local' and 'heroku'")
        exit()

    msgs = []
    users_labels = []
    model_labels = []

    for file in os.listdir(TEST_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_FILE_PATH + file))
            list_of_msgs_with_labels = text_file['text_labels']
            
            list_of_msgs = [i[0] for i in list_of_msgs_with_labels]
            list_of_labels = [i[1] for i in list_of_msgs_with_labels]

            response = requests.post(url, json={
                "msgs": list_of_msgs
            })

            list_of_model_labels = response.json()

            msgs.extend(list_of_msgs)
            users_labels.extend(list_of_labels)
            model_labels.extend(list_of_model_labels)

    data_tuples = list(zip(msgs, users_labels, model_labels))

    df = pd.DataFrame(data_tuples, columns=['Message', 'User_Label', 'Model_Label'])
    
    return df

def test_audio(urltype = "local"):

    if urltype == "local":
            url = LOCAL_URL_VOICE
    elif urltype == "heroku": 
        url = HEROKU_URL_VOICE
    else:
        print("Inserted a wrong urltype, choose one between 'local' and 'heroku'")
        exit()

    audios = []
    users_labels = []
    model_labels = []

    for file in os.listdir(TEST_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_FILE_PATH + file))
            list_of_audio_with_labels = text_file['audio_labels']

            list_of_audio = [i[0] for i in list_of_audio_with_labels]
            list_of_labels = [i[1] for i in list_of_audio_with_labels]

            list_of_model_labels = []

            for audio in list_of_audio:
                audio_file = open(TEST_FILE_PATH + audio, "rb")
                values = {
                    "file": (TEST_FILE_PATH + audio, audio_file, "audio/wav")
                }
                response = requests.post(url, files=values)
                list_of_model_labels.append(response.json())

            audios.extend(list_of_audio)
            users_labels.extend(list_of_labels)
            model_labels.extend(list_of_model_labels)

    data_tuples = list(zip(audios, users_labels, model_labels))

    df = pd.DataFrame(data_tuples, columns=['Audio_Name', 'User_Label', 'Model_Label'])
    
    return df
            

if __name__ == "__main__":

    urltype = "heroku"

    df_text  = test_text(urltype)
    df_audio = test_audio(urltype)
   
    print("\n------------------------------------------ TEXT ANALYSIS -----------------------------------------\n")
    print(df_text.to_markdown())

    print("\n")
    
    print("\n------------------------------------------ AUDIO ANALYSIS -----------------------------------------\n")
    print(df_audio.to_markdown())