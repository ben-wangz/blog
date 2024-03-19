import compress_fasttext
import os

model_path = os.getenv("MODEL_PATH", default="/app/model/wiki.en.bin")
sentence = os.getenv("SENTENCE", default="hello world")

small_model = compress_fasttext.models.CompressedFastTextKeyedVectors.load(model_path)
vector = small_model.get_sentence_vector(sentence)
print(vector)
