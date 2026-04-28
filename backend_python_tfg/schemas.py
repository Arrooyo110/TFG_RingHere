from pydantic import BaseModel

# Datos básicos que esperamos recibir del móvil
class AlarmaBase(BaseModel):
    nombre: str
    latitud: float
    longitud: float
    radio: float
    is_active: bool = True

class AlarmaCreate(AlarmaBase):
    pass

# Lo que le respondemos al móvil (incluye el ID generado por la BD)
class AlarmaResponse(AlarmaBase):
    id: int

    class Config:
        from_attributes = True