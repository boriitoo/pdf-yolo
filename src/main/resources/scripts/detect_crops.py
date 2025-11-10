import sys, json, base64, io, os
from ultralytics import YOLO
from PIL import Image
import cv2

# ✅ Load model once globally
MODEL_PATH = os.path.join("model", "best.pt")
model = YOLO(MODEL_PATH)
CONF_THRESHOLD = 0.5  # confidence threshold

def encode_crop(crop):
    buffer = io.BytesIO()
    crop.save(buffer, format="PNG")
    return base64.b64encode(buffer.getvalue()).decode("utf-8")

def predict(image_path):
    results = model(image_path, conf=CONF_THRESHOLD, verbose=False)
    image = cv2.imread(image_path)
    crops = []
    for box in results[0].boxes.xyxy:
        x1, y1, x2, y2 = map(int, box.tolist())
        crop = image[y1:y2, x1:x2]
        if crop.size == 0:
            continue
        crop_img = Image.fromarray(cv2.cvtColor(crop, cv2.COLOR_BGR2RGB))
        crops.append(encode_crop(crop_img))
    return crops

def main():
    data = json.load(sys.stdin)
    output = {}
    for img in data["images"]:
        output[os.path.basename(img)] = predict(img)
    print(json.dumps(output))

if __name__ == "__main__":
    main()
