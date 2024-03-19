import fasttext
import os

model_path = os.getenv("MODEL_PATH", default="/app/model/wiki.en.bin")
sentence = os.getenv("SENTENCE", default="hello world")
model = fasttext.load_model(model_path)
vector = model.get_sentence_vector(sentence)
print(vector)
