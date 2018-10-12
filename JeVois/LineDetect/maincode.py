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
    resized = cv2.resize(inImg, (300, inImg.shape[0]))
    ratio = inImg.shape[0] / float(resized.shape[0])
    # convert to B&W
    # afterImg = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
    # threshold color
    # thresh = cv2.threshold(afterImg, 127, 255, cv2.THRESH_BINARY)
    # threshold returns extra data. remove that
    # afterImg = thresh[1]

    gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    thresh = cv2.threshold(blurred, 60, 255, cv2.THRESH_BINARY)[1]

    # find contours in the thresholded image and initialize the
    # shape detector
    contours = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    contours = contours[0] if is_cv2 else contours[1]
    sd = ShapeDetector()
    # loop over the contours
    for c in contours:
        # compute the center of the contour, then detect the name of the
        # shape using only the contour
        M = cv2.moments(c)
        print(M)
        if M["m00"] == 0:
            print("Division by 0!")
        else:
            cX = int((M["m10"] / M["m00"]) * ratio)
            cY = int((M["m01"] / M["m00"]) * ratio)
            shape = sd.detect(c)

            # multiply the contour (x, y)-coordinates by the resize ratio,
            # then draw the contours and the name of the shape on the image
            c = c.astype("float")
            c *= ratio
            c = c.astype("int")
            cv2.drawContours(inImg, [c], -1, (0, 255, 0), 2)
            cv2.putText(inImg, shape, (cX, cY), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2)

    return afterImg
