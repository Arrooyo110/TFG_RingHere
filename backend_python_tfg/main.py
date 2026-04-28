from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
import models
import schemas
from database import engine, get_db

# Crea el archivo de la base de datos física si no existe
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="API de GeoAlarmas",
    description="Backend para el TFG de gestión de geovallas"
)

# Endpoint 1: Crear una nueva alarma (El móvil envía datos aquí)
@app.post("/alarmas/", response_model=schemas.AlarmaResponse)
def crear_alarma(alarma: schemas.AlarmaCreate, db: Session = Depends(get_db)):
    # Convertimos los datos de Pydantic al modelo de SQLAlchemy
    db_alarma = models.Alarma(**alarma.model_dump())
    db.add(db_alarma)
    db.commit()
    db.refresh(db_alarma) # Obtenemos el ID recién creado
    return db_alarma

# Endpoint 2: Obtener todas las alarmas (El móvil lee datos de aquí)
@app.get("/alarmas/", response_model=list[schemas.AlarmaResponse])
def leer_alarmas(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    alarmas = db.query(models.Alarma).offset(skip).limit(limit).all()
    return alarmas

# Endpoint 3: Actualizar una alarma existente (PUT)
@app.put("/alarmas/{alarma_id}", response_model=schemas.AlarmaResponse)
def actualizar_alarma(alarma_id: int, alarma_actualizada: schemas.AlarmaCreate, db: Session = Depends(get_db)):
    # Buscamos la alarma en la base de datos
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id).first()
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada")
    
    # Actualizamos los datos
    for key, value in alarma_actualizada.model_dump().items():
        setattr(db_alarma, key, value)
        
    db.commit()
    db.refresh(db_alarma)
    return db_alarma

# Endpoint 4: Borrar una alarma (DELETE)
@app.delete("/alarmas/{alarma_id}")
def borrar_alarma(alarma_id: int, db: Session = Depends(get_db)):
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id).first()
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada")
    
    db.delete(db_alarma)
    db.commit()
    return {"mensaje": "Alarma borrada correctamente"}