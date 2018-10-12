# This file does the actual image processing.
import cv2


def process(inImg):
    afterImg = cv2.cvtColor(inImg, cv2.COLOR_BGR2GRAY)
    temp = cv2.threshold(inImg, 127, 255, cv2.THRESH_BINARY)
    afterImg = temp[1]
    return afterImg
