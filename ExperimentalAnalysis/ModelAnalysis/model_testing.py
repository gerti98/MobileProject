import requests
import os
import json 
import pandas as pd

# Heroku APIs
HEROKU_URL_TEXT = 'https://mobile-group3.herokuapp.com/predict_text_emotion'
HEROKU_URL_VOICE = 'https://mobile-group3.herokuapp.com/predict_voice_emotion'

# Localhost APIs
LOCAL_URL_TEXT = 'http://127.0.0.1:5000/predict_text_emotion'
LOCAL_URL_VOICE = 'http://127.0.0.1:5000/predict_voice_emotion'

# Folder that contains the files
TEST_FILE_PATH = "./UserLabels/"

def test_text(urltype = "local"):
    """
    This function is used to parse the json files in the TEST_FILE_PATH folder and make the prediction on the
    msgs in each json file. 
    These info will be used to compare the user's labels with the labels given by the model 
    to experimentally test the model accuracy.
    Each json file has this format:
        {
            "text_labels": 
                [
                    [msg_1, label_1]
                    [msg_2, label_2]
                    .
                    .
                    [msg_n, label_n]
                ]
            "audio_labels": 
                [
                    [audio_1, label_1]
                    [audio_2, label_2]
                    .
                    .
                    [audio_n, label_n]
                ]
        }
    It will only be parsed the list of msgs.
    """

    # Checking the urltype to determine which server use for the test, if the local or heroku APIs.
    if urltype == "local":
        url = LOCAL_URL_TEXT
    elif urltype == "heroku": 
        url = HEROKU_URL_TEXT
    else:
        print("Inserted a wrong urltype, choose one between 'local' and 'heroku'")
        exit()

    # Lists that contains the user msgs, the user labels and the models predictions
    msgs = []
    users_labels = []
    model_labels = []

    for file in os.listdir(TEST_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_FILE_PATH + file))  # Open the json file
            list_of_msgs_with_labels = text_file['text_labels'] # Take the list of msgs with the labels 
            
            list_of_msgs = [i[0] for i in list_of_msgs_with_labels] # List of user msgs
            list_of_labels = [i[1] for i in list_of_msgs_with_labels] # List of user labels

            # Make the request to the API for the emotion prediction 
            response = requests.post(url, json={
                "msgs": list_of_msgs
            })

            list_of_model_labels = response.json() # Get the API's response (prediction)

            # Add each prediction to the corresponding (user msg + user label)
            msgs.extend(list_of_msgs)
            users_labels.extend(list_of_labels)
            model_labels.extend(list_of_model_labels)

    # Create a dataframe with the data parsed from the json files
    data_tuples = list(zip(msgs, users_labels, model_labels))
    df = pd.DataFrame(data_tuples, columns=['Message', 'User_Label', 'Model_Label'])
    
    return df  

def test_audio(urltype = "local"):
    """
    This function is used to parse the json files in the TEST_FILE_PATH folder and make the prediction on the
    audios in each json file. 
    These info will be used to compare the user's labels with the labels given by the model 
    to experimentally test the model accuracy.
    Each json file has this format:
        {
            "text_labels": 
                [
                    [msg_1, label_1]
                    [msg_2, label_2]
                    .
                    .
                    [msg_n, label_n]
                ]
            "audio_labels": 
                [
                    [audio_1, label_1]
                    [audio_2, label_2]
                    .
                    .
                    [audio_n, label_n]
                ]
        }
        It will only be parsed the list of audios.
    """

    # Checking the urltype to determine which server use for the test, if the local or heroku APIs.
    if urltype == "local":
            url = LOCAL_URL_VOICE
    elif urltype == "heroku": 
        url = HEROKU_URL_VOICE
    else:
        print("Inserted a wrong urltype, choose one between 'local' and 'heroku'")
        exit()

    # Lists that contains the user audios, the user labels and the models predictions
    audios = []
    users_labels = []
    model_labels = []

    for file in os.listdir(TEST_FILE_PATH):
        if file.endswith('.json'):
            text_file = json.load(open(TEST_FILE_PATH + file)) # Open the json file
            list_of_audio_with_labels = text_file['audio_labels'] # Take the list of audios with the labels

            list_of_audio = [i[0] for i in list_of_audio_with_labels] # List of user audios
            list_of_labels = [i[1] for i in list_of_audio_with_labels] # List of user msgs

            list_of_model_labels = []

            # Foreach audio file found in the json files search it in the directory and send it to the API
            for audio in list_of_audio: 
                audio_file = open(TEST_FILE_PATH + audio, "rb") # Open the audio file
                # Prepare the request to the API
                values = {
                    "file": (TEST_FILE_PATH + audio, audio_file, "audio/wav")
                }
                # Make the request to the API for the emotion prediction 
                response = requests.post(url, files=values)
                list_of_model_labels.append(response.json()) # Append the label to the list of labels given by the model

            # Add each prediction to the corresponding (user audio + user label)
            audios.extend(list_of_audio)
            users_labels.extend(list_of_labels)
            model_labels.extend(list_of_model_labels)

    # Create a dataframe with the data parsed from the json files
    data_tuples = list(zip(audios, users_labels, model_labels))
    df = pd.DataFrame(data_tuples, columns=['Audio_Name', 'User_Label', 'Model_Label'])
    
    return df
            
# Example of a test 
if __name__ == "__main__":

    urltype = "local"

    df_text  = test_text(urltype)
    df_audio = test_audio(urltype)
   
    print("\n------------------------------------------ TEXT ANALYSIS -----------------------------------------\n")
    print(df_text.to_markdown())

    print("\n")
    
    print("\n------------------------------------------ AUDIO ANALYSIS -----------------------------------------\n")
    print(df_audio.to_markdown())