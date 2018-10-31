# This is for when you'd like to test image processing on your own machiene
# (not JeVois)
import cv2
from maincode import process

img = cv2.imread("test.png")
afterImg = process(img)
cv2.imwrite("out.png", afterImg)
try:
    cv2.imshow('squares', afterImg)
    cv2.waitKey();
except Exception as e:
    pass
