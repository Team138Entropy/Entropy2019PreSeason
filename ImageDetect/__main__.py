from __future__ import division
import cv2
import time
import sys
import numpy as np
import imutils
import glob

def nothing(*arg):
    pass

onlyOneContour = True
epsilon = 0.015
numVertices = 10
timeDelayMs = 0

# for cube
# icol = (27, 101, 5, 38, 249, 255)

# for ball
icol = (21, 128, 0, 41, 249, 255)

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

# how much to guassian blur after filtering
cv2.createTrackbar('postGuassian', 'sliders', 0, 20, nothing)

# the color threshold
cv2.createTrackbar('threshold', 'sliders', 0, 255, nothing)

# contour detection epsilon
cv2.createTrackbar('epsilon', 'sliders', 15, 20, nothing)

# minimum contour area (as a 10000th of source image area)
cv2.createTrackbar('minArea10000th', 'sliders', 0, 50, nothing)

# image debug levels
# 3 = all, 2 = some, 1 = reduced, 0 = only final
cv2.createTrackbar('imgDebugLevels', 'sliders', 0, 3, nothing)

cv2.createTrackbar('timeDelayMs', 'sliders', timeDelayMs, 5000, nothing)

cv2.createTrackbar('numVertices', 'sliders', numVertices, 20, nothing)

# a list of all window titles
windows = []
lastWindows = []
def imshow (title, img):
    windows.append(title)
    cv2.imshow(title, img)

args = []

for arg in sys.argv[1:]:
    for file in glob.glob(arg):
        args.append(file)

print("loading " + str(args))
while True:
    for file in args:
        frame = cv2.imread(file)
        height, width = frame.shape[:2]

        windows = []
        # Get HSV values from the GUI sliders.
        lowHue = cv2.getTrackbarPos('lowHue', 'sliders')
        lowSat = cv2.getTrackbarPos('lowSat', 'sliders')
        lowVal = cv2.getTrackbarPos('lowVal', 'sliders')
        highHue = cv2.getTrackbarPos('highHue', 'sliders')
        highSat = cv2.getTrackbarPos('highSat', 'sliders')
        highVal = cv2.getTrackbarPos('highVal', 'sliders')

        # it's a percent, so make it in the range of 0-1
        # then multiply by the total area
        # now it's on a scale of 0 to the image area
        minArea = cv2.getTrackbarPos('minArea10000th', 'sliders') * (height * width) / 10000

        # how much to guassian blur
        blurVal = (cv2.getTrackbarPos('guassian', 'sliders') * 2) + 1

        # how much to guassian blur after processing
        blurValPost = (cv2.getTrackbarPos('postGuassian', 'sliders') * 2) + 1

        # the color threshold
        threshold = cv2.getTrackbarPos('threshold', 'sliders')

        epsilon = cv2.getTrackbarPos('epsilon', 'sliders') / 1000

        imgDebugLevels = cv2.getTrackbarPos('imgDebugLevels', 'sliders')

        numVertices = cv2.getTrackbarPos('numVertices', 'sliders')

        timeDelayMs = cv2.getTrackbarPos('timeDelayMs', 'sliders')

        if imgDebugLevels > 0:
            # Show the original image.
            imshow('frame', frame)

        # Blur methods available, comment or uncomment to try different blur methods.
        frameBGR = cv2.GaussianBlur(frame, (blurVal, blurVal), 0)
        #frameBGR = cv2.medianBlur(frameBGR, blurVal)
        #frameBGR = cv2.bilateralFilter(frameBGR, 15 ,75, 75)
        """kernal = np.ones((15, 15), np.float32)/255
        frameBGR = cv2.filter2D(frameBGR, -1, kernal)"""

        # Show blurred image.
        if imgDebugLevels > 1:
            imshow('blurred', frameBGR)

        # HSV (Hue, Saturation, Value).
        # Convert the frame to HSV colour model.
        hsv = cv2.cvtColor(frameBGR, cv2.COLOR_BGR2HSV)

        # HSV values to define a colour range.
        colorLow = np.array([lowHue,lowSat,lowVal])
        colorHigh = np.array([highHue,highSat,highVal])
        mask = cv2.inRange(hsv, colorLow, colorHigh)
        # Show the first mask
        if imgDebugLevels > 2:
            imshow('mask-plain', mask)

        # i have no clue what this does
        # just copy-pasted
        kernal = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (7, 7))
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernal)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernal)

        # Show morphological transformation mask
        if imgDebugLevels > 2:
            imshow('mask', mask)

        # Put mask over top of the original image.
        result = cv2.bitwise_and(frame, frame, mask = mask)

        # Show final output image
        if imgDebugLevels > 1:
            imshow('colorTest', result)

        _,threshd = cv2.threshold(result, threshold, 255, cv2.THRESH_BINARY)

        if imgDebugLevels > 2:
            imshow('threshold', threshd)

        # convert to grayscale
        gray = cv2.cvtColor(threshd, cv2.COLOR_BGR2GRAY)
        gray = cv2.bilateralFilter(gray, 11, 17, 17)

        # post guassian
        gray = cv2.GaussianBlur(gray, (blurValPost, blurValPost), 0)

        # find the edges of the image
        edged = cv2.Canny(gray, 30, 200)

        # and display them
        if imgDebugLevels > 1:
            imshow('edged', threshd)

        # find contours
        contours = cv2.findContours(edged.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        contours = imutils.grab_contours(contours)
        # i also don't know what this does
        contours = sorted(contours, key = cv2.contourArea, reverse = True)[:10]

        # the actual list of contours
        foundMatchingContours = []
        foundAllContours = []

        # a list of dicts, containing contours and their data
        contoursAndData = []

        for c in contours:
            # approximate the contour
            peri = cv2.arcLength(c, True)
            approx = cv2.approxPolyDP(c, epsilon * peri, True)
            c = approx


            if cv2.contourArea(c) < minArea:
                break

            # add this contour
            foundAllContours.append(c)

            contoursAndData.append({"contour": c, "area": cv2.contourArea(c), "num": len(c), "area": cv2.contourArea(c)})

            # if our approximated contour has numVertices points, then
            if len(c) == numVertices or numVertices == 0:
                foundMatchingContours.append(c)

        # if we've enabling filtering to only one contour...
        if onlyOneContour and len(contoursAndData) > 0:
            # sort the contours
            contoursAndData = sorted(contoursAndData, key=lambda c: c["area"], reverse=True)
            # filter the biggest one
            foundAllContours = [contoursAndData[0]["contour"]]
            if contoursAndData[0]["num"] == numVertices or numVertices == 0:
                foundMatchingContours = foundAllContours
            else:
                foundMatchingContours = []

        # get the size of the result image
        height, width, channels = result.shape
        # and make a new blank image array with it
        newBlankArr = np.zeros((height,width), np.uint8)
        # and make that array an image
        newBlank = cv2.cvtColor(newBlankArr, cv2.COLOR_GRAY2BGR)


        # and draw the matching contours (have numVertices vertices) onto it
        def displayContours(contours, limited = False):
            # print(contours)
            matchClone = np.empty_like(newBlank)
            matchClone[:] = newBlank

            # write on black background
            cv2.drawContours(matchClone, contours, -1, (0, 255, 0), 3)
            cv2.putText(matchClone, str(len(contours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

            if imgDebugLevels > 1:
                imshow("contours with " + str(numVertices) + " on black background" if limited else "contours on a black background", matchClone)

            # on og image
            clonedResult = np.empty_like(result)
            clonedResult[:] = result
            cv2.drawContours(clonedResult, contours, -1, (0, 255, 0), 3)
            cv2.putText(clonedResult, str(len(contours)) + 'contour(s) found', (0, height - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)

            if imgDebugLevels > 0:
                imshow("contours with " + str(numVertices) + " on og image" if limited else "contours on og image", clonedResult)

        displayContours(foundMatchingContours, True)
        displayContours(foundAllContours, False)

        if onlyOneContour and len(contoursAndData) > 0:
            finalImg = np.empty_like(result)
            finalImg[:] = result

            # average the points in that contour
            xSum = 0
            ySum = 0
            xCount = 0
            yCount = 0
            for point in foundAllContours[0]:
                point = point[0]
                cv2.circle(
                    finalImg,
                    (int(point[0]), int(point[1])),
                    int(1),
                    (int(0), int(0), int(255)),
                    int(2)
                )
                xSum += point[0]
                ySum += point[1]
                xCount += 1
                yCount += 1

            xAvg = xSum / xCount
            yAvg = ySum / yCount

            # draw a circle where that average is
            cv2.circle(
                finalImg,
                (int(xAvg), int(yAvg)),
                int(1),
                (int(255), int(0), int(0)),
                int(5)
            )


            cv2.putText(finalImg, "found " + str(contoursAndData[0]["num"]) + " contour points & area 10000th " + str(contoursAndData[0]["area"] / (width * height) * 10000), (0, 15), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
            if imgDebugLevels > 0:
                imshow("final", finalImg)
            outStr = ""
            # if the point is on the left, turn right
            if xAvg > width / 2:
                outStr += " turn right " + "{:2.2}".format(str(
                    (xAvg - width / 2) / (width / 2) * 100 / 2
                )) + "%"
            # otherwise, turn left
            else:
                outStr += " turn left " + "{:2.2}".format(str(
                    (width / 2 - xAvg) / (width / 2) * 100 / 2
                )) + "%"

            # if the point is too high, look down
            if yAvg > height / 2:
                outStr += " look down " + "{:2.2}".format(str(
                    (yAvg - height / 2) / (height / 2) * 100 / 2
                )) + "%"
            else:
                outStr += " look up " + "{:2.2}".format(str(
                    (height / 2 - yAvg) / (height / 2) * 100 / 2
                )) + "%"

            print(outStr)
            cv2.putText(finalImg, outStr, (0, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
            imshow("final", finalImg)



        ## window cleanup

        # finds anything in the first list that isn't in the second
        def diff(first, second):
            # second = set(second)
            return [item for item in first if item not in second]

        # if a window wan't show this time around, destroy the old copy
        for title in diff(lastWindows, windows):
            cv2.destroyWindow(title)

        lastWindows = windows
        time.sleep(timeDelayMs / 1000)

        k = cv2.waitKey(5) & 0xFF
        if k == 27:
            sys.exit(0)
