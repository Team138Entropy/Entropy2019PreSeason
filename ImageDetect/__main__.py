from __future__ import division
import cv2
import sys
import numpy as np
import imutils

def nothing(*arg):
    pass

blurVal = 13
# Initial HSV GUI slider values to load on program start.
icol = (27, 101, 5, 38, 249, 255)    # Green
#icol = (18, 0, 196, 36, 255, 255)  # Yellow
#icol = (89, 0, 0, 125, 255, 255)  # Blue
#icol = (0, 100, 80, 10, 255, 255)   # Red
cv2.namedWindow('colorTest')
# Lower range colour sliders.
cv2.createTrackbar('lowHue', 'colorTest', icol[0], 255, nothing)
cv2.createTrackbar('lowSat', 'colorTest', icol[1], 255, nothing)
cv2.createTrackbar('lowVal', 'colorTest', icol[2], 255, nothing)
# Higher range colour sliders.
cv2.createTrackbar('highHue', 'colorTest', icol[3], 255, nothing)
cv2.createTrackbar('highSat', 'colorTest', icol[4], 255, nothing)
cv2.createTrackbar('highVal', 'colorTest', icol[5], 255, nothing)

cv2.createTrackbar('guassian', 'colorTest', 0, 20, nothing)
cv2.createTrackbar('threshold', 'colorTest', 0, 255, nothing)

# Raspberry pi file path example.
#frame = cv2.imread('/home/pi/python3/opencv/color-test/colour-circles-test.jpg')
# Windows file path example.
frame = cv2.imread('smallball.jpg')

while True:
    # Get HSV values from the GUI sliders.
    lowHue = cv2.getTrackbarPos('lowHue', 'colorTest')
    lowSat = cv2.getTrackbarPos('lowSat', 'colorTest')
    lowVal = cv2.getTrackbarPos('lowVal', 'colorTest')
    highHue = cv2.getTrackbarPos('highHue', 'colorTest')
    highSat = cv2.getTrackbarPos('highSat', 'colorTest')
    highVal = cv2.getTrackbarPos('highVal', 'colorTest')
    blurVal = (cv2.getTrackbarPos('guassian', 'colorTest') * 2) + 1
    threshold = cv2.getTrackbarPos('threshold', 'colorTest')

    # Show the original image.
    cv2.imshow('frame', frame)

    # Blur methods available, comment or uncomment to try different blur methods.
    frameBGR = cv2.GaussianBlur(frame, (blurVal, blurVal), 0)
    frameBGR = cv2.medianBlur(frameBGR, blurVal)
    #frameBGR = cv2.bilateralFilter(frameBGR, 15 ,75, 75)
    """kernal = np.ones((15, 15), np.float32)/255
    frameBGR = cv2.filter2D(frameBGR, -1, kernal)"""

    # Show blurred image.
    cv2.imshow('blurred', frameBGR)

    # HSV (Hue, Saturation, Value).
    # Convert the frame to HSV colour model.
    hsv = cv2.cvtColor(frameBGR, cv2.COLOR_BGR2HSV)

    # HSV values to define a colour range.
    colorLow = np.array([lowHue,lowSat,lowVal])
    colorHigh = np.array([highHue,highSat,highVal])
    mask = cv2.inRange(hsv, colorLow, colorHigh)
    # Show the first mask
    cv2.imshow('mask-plain', mask)

    kernal = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (7, 7))
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernal)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernal)

    # Show morphological transformation mask
    cv2.imshow('mask', mask)

    # Put mask over top of the original image.
    result = cv2.bitwise_and(frame, frame, mask = mask)

    # Show final output image
    cv2.imshow('colorTest', result)

    _,threshd = cv2.threshold(result, threshold, 255, cv2.THRESH_BINARY)

    cv2.imshow('threshold', threshd)
    #threshd = cv2.cvtColor(threshd, cv2.CV_8UC1)

    gray = cv2.cvtColor(threshd, cv2.COLOR_BGR2GRAY)
    gray = cv2.bilateralFilter(gray, 11, 17, 17)
    edged = cv2.Canny(gray, 30, 200)

    cv2.imshow('edged', edged)

    contors = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contors = imutils.grab_contours(contors)
    contors = sorted(contors, key = cv2.contourArea, reverse = True)[:10]
    screenCnt = None


    for c in contors:
        # approximate the contour
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.015 * peri, True)

        # if our approximated contour has 6 points, then
        if len(approx) == 6:
            cv2.drawContours(result, [approx], -1, (0, 255, 0), 3)
            screenCnt = approx
            break

    cv2.imshow("contours", result)

    cv2.imshow('gray', gray)
    #cv2.imshow('contours', res#)

    k = cv2.waitKey(5) & 0xFF
    if k == 27:
        break

cv2.destroyAllWindows()
