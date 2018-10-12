# This file does the actual image processing.
import cv2
from ShapeDetector import ShapeDetector

ratio = 1
is_cv2 = True


def detectShape(c):
    shape = "unidentified"
    peri = cv2.arcLength(c, True)
    approx = cv2.approxPolyDP(c, 0.04 * peri, True)

    if len(approx) == 4:
        # compute the bounding box of the contour and use the
        # bounding box to compute the aspect ratio
        (x, y, w, h) = cv2.boundingRect(approx)
        ar = w / float(h)

        # a square will have an aspect ratio that is approximately
        # equal to one, otherwise, the shape is a rectangle
        shape = "square" if ar >= 0.95 and ar <= 1.05 else "rectangle"

    return shape


def process(inImg):
    # convert to B&W
    afterImg = cv2.cvtColor(inImg, cv2.COLOR_BGR2GRAY)
    # threshold color
    thresh = cv2.threshold(afterImg, 127, 255, cv2.THRESH_BINARY)
    # threshold returns extra data. remove that
    afterImg = thresh[1]

    t = cv2.threshold(afterImg, 127, 255, 1)
    # ret = t[0]
    thresh = t[1]

    t = cv2.findContours(thresh, 1, 2)
    contours = t[1]
    # h = t[1]

    for cnt in contours:
        approx = cv2.approxPolyDP(cnt, 0.01 * cv2.arcLength(cnt, True), True)
        print len(approx)
        if len(approx) == 5:
            print "pentagon"
            cv2.drawContours(afterImg, [cnt], 0, 255, -1)
        elif len(approx) == 3:
            print "triangle"
            cv2.drawContours(afterImg, [cnt], 0, (0, 255, 0), -1)
        elif len(approx) == 4:
            print "square"
            cv2.drawContours(afterImg, [cnt], 0, (0, 0, 255), -1)
        elif len(approx) == 9:
            print "half-circle"
            cv2.drawContours(afterImg, [cnt], 0, (255, 255, 0), -1)
        elif len(approx) > 15:
            print "circle"
            cv2.drawContours(afterImg, [cnt], 0, (0, 255, 255), -1)

    return afterImg
