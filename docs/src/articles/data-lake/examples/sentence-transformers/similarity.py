from sentence_transformers import SentenceTransformer, util
from PIL import Image, ImageFile
import os
import requests
import torch

def load_image(url_or_path):
    if url_or_path.startswith("http://") or url_or_path.startswith("https://"):
        return Image.open(requests.get(url_or_path, stream=True).raw)
    else:
        return Image.open(url_or_path)

# original clip-ViT-B-32 for encoding images
image_model_path = os.getenv("IMAGE_MODEL_PATH", default="/app/models/clip-ViT-B-32")
# text embedding model is aligned to the img_model and maps 50+ languages to the same vector space
text_model_path = os.getenv("TEXT_MODEL_PATH", default="/app/models/sentence-transformers/clip-ViT-B-32-multilingual-v1")

img_model = SentenceTransformer(image_model_path)
text_model = SentenceTransformer(text_model_path)
img_paths = [
    # Dog image
    "https://unsplash.com/photos/QtxgNsmJQSs/download?ixid=MnwxMjA3fDB8MXxhbGx8fHx8fHx8fHwxNjM1ODQ0MjY3&w=640",
    # Cat image
    "https://unsplash.com/photos/9UUoGaaHtNE/download?ixid=MnwxMjA3fDB8MXxzZWFyY2h8Mnx8Y2F0fHwwfHx8fDE2MzU4NDI1ODQ&w=640",
    # Beach image
    "https://unsplash.com/photos/Siuwr3uCir0/download?ixid=MnwxMjA3fDB8MXxzZWFyY2h8NHx8YmVhY2h8fDB8fHx8MTYzNTg0MjYzMg&w=640"
]
images = [load_image(img) for img in img_paths]
# Map images to the vector space
img_embeddings = img_model.encode(images)
# Now we encode our text:
texts = [
    "A dog in the snow",
    "Eine Katze",  # German: A cat
    "Una playa con palmeras."  # Spanish: a beach with palm trees
]
text_embeddings = text_model.encode(texts)
# Compute cosine similarities:
cos_sim = util.cos_sim(text_embeddings, img_embeddings)
for text, scores in zip(texts, cos_sim):
    max_img_idx = torch.argmax(scores)
    print("Text:", text)
    print("Score:", scores[max_img_idx] )
    print("Path:", img_paths[max_img_idx], "\n")
