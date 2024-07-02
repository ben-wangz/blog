import os
import torch
import open_clip

model_path = os.getenv("MODEL_PATH", default="/app/model/wiki.en.bin")
model_name = os.getenv("MODEL_NAME", default="ViT-B-16")
sentence = os.getenv("SENTENCE", default="hello world")
device = "cuda" if torch.cuda.is_available() else "cpu"
model, _, preprocess = open_clip.create_model_and_transforms(
    model_name="ViT-B-16", pretrained=model_path, device=device
)
tokenizer = open_clip.get_tokenizer(model_name="ViT-B-16")
text = tokenizer([sentence])
with torch.no_grad(), torch.cuda.amp.autocast():
    text_features = model.encode_text(text)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    print(text_features)
