import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import transforms

# Define the neural network
class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        self.fc1 = nn.Linear(28*28, 512)  # Input layer to hidden layer
        self.fc2 = nn.Linear(512, 256)   # Hidden layer to another hidden layer
        self.fc3 = nn.Linear(256, 10)    # Hidden layer to output layer

    def forward(self, x):
        x = x.view(-1, 28*28)  # Flatten the image
        x = torch.relu(self.fc1(x))  # Activation function for hidden layer
        x = torch.relu(self.fc2(x))
        x = self.fc3(x)
        return x

class ImageTransformer:
    def __init__(self):
        self.transform = transforms.Compose([
            transforms.ToTensor(),
            transforms.Normalize((0.1307,), (0.3081,))
        ])

    def __call__(self, image):
        return self.transform(image)