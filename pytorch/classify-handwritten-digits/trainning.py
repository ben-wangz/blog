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
train_dataset = datasets.MNIST(
    root="./.data", train=True, download=True, transform=transform
)
train_loader = DataLoader(dataset=train_dataset, batch_size=64, shuffle=True)
test_dataset = datasets.MNIST(
    root="./.data", train=False, download=True, transform=transform
)
test_loader = DataLoader(dataset=test_dataset, batch_size=64, shuffle=False)

# Initialize the network, loss function, and optimizer
model = Net().to(device)  # Move the model to the correct device
criterion = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=0.01, momentum=0.5)

# Train the network
num_epochs = 5  # You can increase this for better performance
for epoch in range(num_epochs):
    for images, labels in train_loader:
        images, labels = images.to(device), labels.to(device)
        optimizer.zero_grad()  # Clear gradients for this training step
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()  # Backpropagation, compute gradient of loss with respect to model parameters
        optimizer.step()  # Apply gradients

    print(f"Epoch {epoch+1}, Loss: {loss.item()}")

# Test the network
model.eval()  # Set the model to evaluation mode
correct = 0
total = 0
with torch.no_grad():  # Disable gradient calculation
    for images, labels in test_loader:
        images, labels = images.to(device), labels.to(device)
        outputs = model(images)
        _, predicted = torch.max(outputs.data, 1)
        total += labels.size(0)
        correct += (predicted == labels).sum().item()

    print(f"Accuracy of the network on the test images: {100 * correct / total}%")

# Save the model
torch.save(model.state_dict(), ".model/mnist.pth")
