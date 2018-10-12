import cv2
from maincode import process

img = cv2.imread("test.png")
# cv2.imshow("image", img)
afterImg = process(img)
# cv2.imshow("image after", img)
cv2.imwrite("out.png", afterImg)
# cv2.waitKey(0)
# cv2.destroyAllWindows()
