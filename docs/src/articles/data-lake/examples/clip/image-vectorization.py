import os
import torch
import open_clip

from PIL import Image

model_path = os.getenv("MODEL_PATH", default="/app/model/wiki.en.bin")
model_name = os.getenv("MODEL_NAME", default="ViT-B-16")
image_path = os.getenv("IMAGE_PATH", default="/app/image.jpg")
device = "cuda" if torch.cuda.is_available() else "cpu"
model, _, preprocess = open_clip.create_model_and_transforms(model_name='ViT-B-16', pretrained=model_path, device=device)
image = preprocess(Image.open(image_path)).unsqueeze(0)
with torch.no_grad(), torch.cuda.amp.autocast():
    image_features = model.encode_image(image)
    image_features /= image_features.norm(dim=-1, keepdim=True)
    print(image_features)
