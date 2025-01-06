import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets
from torch.utils.data import DataLoader
from model import Net
from model import ImageTransformer

# Set the device according to the availability of CUDA
if torch.cuda.is_available():
    device = torch.device('cuda')
    print("using device: cuda")
else:
    device = torch.device('cpu')
    print("using device: cpu")

# Load and normalize the data
transform = ImageTransformer()
test_dataset = datasets.MNIST(
    root="./.data", train=False, download=True, transform=transform
)
test_loader = DataLoader(dataset=test_dataset, batch_size=64, shuffle=False)

# Load the model
model = Net().to(device)
model.load_state_dict(torch.load(".model/mnist.pth", weights_only=True))
model.eval()  # Set the model to evaluation mode

# Make a prediction
dataiter = iter(test_loader)
images, labels = next(dataiter)

img = images[0]  # Select one image
img = img.unsqueeze(0)  # Add batch dimension
img = img.to(device)

output = model(img)
_, predicted = torch.max(output.data, 1)

print(f"Predicted label: {predicted.item()}")
