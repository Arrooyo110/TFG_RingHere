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

# --- ESQUEMAS DE USUARIO ---
class UsuarioBase(BaseModel):
    email: str
    full_name: str | None = None

class UsuarioCreate(UsuarioBase):
    password: str

class UsuarioResponse(UsuarioBase):
    id: int
    role: str

    class Config:
        from_attributes = True

# --- ESQUEMAS PARA EL TOKEN JWT ---
class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: str | None = None