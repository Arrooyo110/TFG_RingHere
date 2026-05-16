import uuid

from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey, BigInteger
from sqlalchemy.orm import relationship
from database import Base

class Usuario(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True)
    hashed_password = Column(String)
    full_name = Column(String)
    role = Column(String, default="user")

    # Relación: Un usuario tiene muchas alarmas
    alarmas = relationship("Alarma", back_populates="propietario")

class Alarma(Base):
    __tablename__ = "alarmas"

    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    nombre = Column(String, index=True)
    latitud = Column(Float)
    longitud = Column(Float)
    radio = Column(Float)
    is_active = Column(Boolean, default=True)
    
    fecha_creacion = Column(BigInteger, default=lambda: int(time.time() * 1000))
    # Clave foránea: a quién pertenece esta alarma
    usuario_id = Column(Integer, ForeignKey("usuarios.id"))
    user_email = Column(String, index=True)
    
    propietario = relationship("Usuario", back_populates="alarmas")