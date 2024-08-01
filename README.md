# Hu Moments and K-Nearest Neighbors Project

This project involves the implementation of a K-Nearest Neighbors (K-NN) algorithm combined with Hu Moments to classify image textures. The program calculates Hu Moments for a set of images and uses them as features for the K-NN classifier.

## Project Structure

The project consists of the following key components:

1. **Image Class**:
    - A class representing an image, including its name, distance metric, and Hu Moments vector.

2. **K_Nearest Class**:
    - This is the main class that implements the `PlugInFilter` interface from ImageJ.
    - The `setup` method initializes the plugin, creating a file for Hu Moments.
    - The `run` method collects user input for the number of neighbors (K) and calculates Hu Moments for the reference image.
    - The `search` method calculates distances between the reference image's Hu Moments and other images in the directory, using Manhattan, Euclidean, or Chebyshev distance functions.

## How It Works

### Hu Moments Calculation

The Hu Moments are calculated for each image, providing a 7-element feature vector. These moments are then stored in a file, which is used to compare images.

### K-Nearest Neighbors Classification

1. **Distance Calculation**:
    - The user selects the distance function (Manhattan, Euclidean, or Chebyshev).
    - Distances between the reference image and other images are computed.

2. **Sorting and Selection**:
    - Images are sorted based on the calculated distances.
    - The closest K images are identified and displayed.

## Technologies Used

- Java
- ImageJ (an open-source image processing software)

This project is a demonstration of how image feature extraction and simple classification techniques can be combined to analyze image data.
