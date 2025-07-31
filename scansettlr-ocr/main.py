from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from PIL import Image
import pytesseract
import io

app = FastAPI()

@app.post("/ocr")
async def ocr_image(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        text = pytesseract.image_to_string(image, lang='eng+pol', config="--psm 11")
        return JSONResponse(content={"text": text.strip()})
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
