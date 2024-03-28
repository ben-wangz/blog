import os
from sentence_transformers import SentenceTransformer

model_name = os.getenv("MODEL_NAME", default="clip-ViT-B-32")
model_path = os.getenv("MODEL_PATH", default="/app/models/%s" % model_name)
model = SentenceTransformer(model_name)
model.save(model_path)
