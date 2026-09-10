from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse

from paddleocr import PaddleOCR

import tempfile
import os

app = FastAPI()

# Initialize OCR model once when the application starts
ocr = PaddleOCR(
    use_doc_orientation_classify=False,
    use_doc_unwarping=False,
    use_textline_orientation=False,
    engine="paddle",
)

@app.get("/health")
async def health():
    return JSONResponse(
        content={
            "status": "ok",
            "service": "ocr-server"
        }
    )

@app.post("/ocr")
async def ocr_image(file: UploadFile = File(...)):
    temp_path = None

    try:
        # Read uploaded image
        contents = await file.read()

        # PaddleOCR works directly with image paths
        with tempfile.NamedTemporaryFile(
                delete=False,
                suffix=".jpg"
        ) as temp_file:
            temp_file.write(contents)
            temp_path = temp_file.name

        # Run OCR
        results = ocr.predict(temp_path)

        texts = []

        for result in results:
            data = result.json

            # PaddleOCR returns the actual OCR data inside "res"
            res = data.get("res", {})

            rec_texts = res.get("rec_texts", [])
            rec_scores = res.get("rec_scores", [])

            for text, score in zip(rec_texts, rec_scores):
                texts.append({
                    "text": text,
                    "confidence": float(score)
                })

        # Create plain text compatible with your old API
        text = "\n".join(
            item["text"]
            for item in texts
            if item["text"].strip()
        )

        return JSONResponse(
            content={
                "text": text,
                "results": texts
            }
        )

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

    finally:
        if temp_path and os.path.exists(temp_path):
            os.remove(temp_path)