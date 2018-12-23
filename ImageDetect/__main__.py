from __future__ import division
import cv2
import sys
import numpy as np
import imutils

def nothing(*arg):
    pass

onlyOneContour = False
epsilon = 0.015
numVertices = 6

# for cube
# icol = (27, 101, 5, 38, 249, 255)

# for ball
icol = (20, 128, 0, 41, 249, 255)

cv2.namedWindow('sliders')
# Lower range colour sliders.
cv2.createTrackbar('lowHue', 'sliders', icol[0], 255, nothing)
cv2.createTrackbar('lowSat', 'sliders', icol[1], 255, nothing)
cv2.createTrackbar('lowVal', 'sliders', icol[2], 255, nothing)
# Higher range colour sliders.
cv2.createTrackbar('highHue', 'sliders', icol[3], 255, nothing)
cv2.createTrackbar('highSat', 'sliders', icol[4], 255, nothing)
cv2.createTrackbar('highVal', 'sliders', icol[5], 255, nothing)

# how much to guassian blur
cv2.createTrackbar('guassian', 'sliders', 0, 20, nothing)

# the color threshold
cv2.createTrackbar('threshold', 'sliders', 0, 255, nothing)

# contour detection epsilon
cv2.createTrackbar('epsilon', 'sliders', 15, 20, nothing)

# image debug levels
# 2 = all, 1 = reduced, 0 = only final
cv2.createTrackbar('imgDebugLevels', 'sliders', 2, 2, nothing)

frame = cv2.imread('smallball.jpg')

while True:
    # Get HSV values from the GUI sliders.
    lowHue = cv2.getTrackbarPos('lowHue', 'sliders')
    lowSat = cv2.getTrackbarPos('lowSat', 'sliders')
    lowVal = cv2.getTrackbarPos('lowVal', 'sliders')
    highHue = cv2.getTrackbarPos('highHue', 'sliders')
    highSat = cv2.getTrackbarPos('highSat', 'sliders')
    highVal = cv2.getTrackbarPos('highVal', 'sliders')

    # how much to guassian blur
    blurVal = (cv2.getTrackbarPos('guassian', 'sliders') * 2) + 1

    # the color threshold
    threshold = cv2.getTrackbarPos('threshold', 'sliders')

    epsilon = cv2.getTrackbarPos('epsilon', 'sliders') / 1000

    imgDebugLevels = cv2.getTrackbarPos('imgDebugLevels', 'sliders') / 1000

    # Show the original image.
    cv2.imshow('frame', frame)

    # Blur methods available, comment or uncomment to try different blur methods.
    frameBGR = cv2.GaussianBlur(frame, (blurVal, blurVal), 0)
    frameBGR = cv2.medianBlur(frameBGR, blurVal)
    #frameBGR = cv2.bilateralFilter(frameBGR, 15 ,75, 75)
    """kernal = np.ones((15, 15), np.float32)/255
    frameBGR = cv2.filter2D(frameBGR, -1, kernal)"""

    # Show blurred image.
    if imgDebugLevels > 0:
        cv2.imshow('blurred', frameBGR)

    # HSV (Hue, Saturation, Value).
    # Convert the frame to HSV colour model.
    hsv = cv2.cvtColor(frameBGR, cv2.COLOR_BGR2HSV)

    # HSV values to define a colour range.
    colorLow = np.array([lowHue,lowSat,lowVal])
    colorHigh = np.array([highHue,highSat,highVal])
    mask = cv2.inRange(hsv, colorLow, colorHigh)
    # Show the first mask
    if imgDebugLevels > 1:
        cv2.imshow('mask-plain', mask)

    # i have no clue what this does
    # just copy-pasted
    kernal = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (7, 7))
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernal)
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernal)

    # Show morphological transformation mask
    if imgDebugLevels > 1:
        cv2.imshow('mask', mask)

    # Put mask over top of the original image.
    result = cv2.bitwise_and(frame, frame, mask = mask)

    # Show final output image
    cv2.imshow('colorTest', result)

    _,threshd = cv2.threshold(result, threshold, 255, cv2.THRESH_BINARY)

    if imgDebugLevels > 1:
        cv2.imshow('threshold', threshd)

    # convert to grayscale
    gray = cv2.cvtColor(threshd, cv2.COLOR_BGR2GRAY)
    gray = cv2.bilateralFilter(gray, 11, 17, 17)

    # find the edges of the image
    edged = cv2.Canny(gray, 30, 200)

    # and display them
    if imgDebugLevels > 0:
        cv2.imshow('edged', threshd)

    # find contours
    contours = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contours = imutils.grab_contours(contours)
    # i also don't know what this does
    contours = sorted(contours, key = cv2.contourArea, reverse = True)[:10]

    # the actual list of contours
    foundMatchingContours = []
    foundAllContours = []
    for c in contours:
        # approximate the contour
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, epsilon * peri, True)

        # add this contour
        foundAllContours.append(approx)

        # if our approximated contour has numVertices points, then
        if len(approx) == numVertices:
            foundMatchingContours.append(approx)
            if onlyOneContour:
                break


    # get the size of the result image
    height, width, channels = result.shape
    # and make a new blank image array with it
    newBlankArr = np.zeros((height,width), np.uint8)
    # and make that array an image
    newBlank = cv2.cvtColor(newBlankArr, cv2.COLOR_GRAY2BGR)

    # and draw the matching contours (have numVertices vertices) onto it
    matchClone = np.empty_like(newBlank)
    matchClone[:] = newBlank

    # write on black background
    cv2.drawContours(matchClone, foundMatchingContours, -1, (0, 255, 0), 3)
    cv2.putText(matchClone, str(len(foundMatchingContours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

    if imgDebugLevels > 0:
        cv2.imshow("contours with " + str(numVertices) + " on black background", matchClone)

    # on og image
    clonedResult = np.empty_like(result)
    clonedResult[:] = result
    cv2.drawContours(clonedResult, foundMatchingContours, -1, (0, 255, 0), 3)
    cv2.putText(clonedResult, str(len(foundMatchingContours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

    cv2.imshow("contours with " + str(numVertices) + " on og image", clonedResult)


    # draw the contours onto it
    clone = np.empty_like(newBlank)
    clone[:] = newBlank

    # write on black background
    cv2.drawContours(clone, foundAllContours, -1, (0, 255, 0), 3)
    cv2.putText(clone, str(len(foundAllContours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
    if imgDebugLevels > 0:
        cv2.imshow("all contours on black background", clone)

    # write on og image
    cv2.drawContours(result, foundAllContours, -1, (0, 255, 0), 3)
    cv2.putText(result, str(len(foundAllContours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
    if imgDebugLevels > 0:
        cv2.imshow("all contours on og image", result)



    k = cv2.waitKey(5) & 0xFF
    if k == 27:
        break

cv2.destroyAllWindows()
