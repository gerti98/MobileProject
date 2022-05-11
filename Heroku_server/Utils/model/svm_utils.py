import numpy as np
import re
import unidecode
from nltk import PorterStemmer, word_tokenize
from nltk.stem import SnowballStemmer
from nltk.corpus import stopwords

stop_words = stopwords.words("english")

def preprocess_text(text, stop_words):
    # make lowercase and strip empty spaces
    result = text.lower()
    # strip extra spaces
    result = result.strip()
    # eliminate URLs
    result = re.result = re.sub(r"http\S+", "", result)
    # eliminate email addresses
    result = re.sub('\S*@\S*\s?', '', result)
    # strip diacritics
    result = unidecode.unidecode(result)
    # english stopwords
    word_list = word_tokenize(result)
    # english stemmer
    ps = SnowballStemmer("english")

    stemmed_sentence = ""
    for word in word_list:
        if word not in stop_words:
            stemmed_sentence += ps.stem(word)
            stemmed_sentence += " "

    result = stemmed_sentence
    whitelist = set('abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ')
    result = ''.join(filter(whitelist.__contains__, result))
    result = ''.join([i for i in result if not i.isdigit()])
    return result

def vectorize_texts(vectorizer, texts):
    vectorized_texts = []
    for text in texts:
      processed_text = preprocess_text(text, stop_words)
      vectorized_texts.append(processed_text)
    return vectorizer.transform(vectorized_texts)


def detect_emotions_with_labels(classifier, vectorizer, encoder, texts):
    vectorized_texts = vectorize_texts(vectorizer, texts)
    predictions = classifier.predict_log_proba(vectorized_texts)
    predictions  = list(encoder.inverse_transform(np.argmax(predictions, axis=1)))
    return predictions

def detect_emotions_with_proba(classifier, vectorizer, texts):
    vectorized_texts = vectorize_texts(vectorizer, texts)
    predictions = classifier.predict_log_proba(vectorized_texts)
    return predictions

def proba_to_labels(encoder, proba):
    labels  = list(encoder.inverse_transform(np.argmax(proba, axis=1)))
    return labels