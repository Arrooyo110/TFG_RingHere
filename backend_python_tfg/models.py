from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey
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

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, index=True)
    latitud = Column(Float)
    longitud = Column(Float)
    radio = Column(Float)
    is_active = Column(Boolean, default=True)
    
    # Clave foránea: a quién pertenece esta alarma
    usuario_id = Column(Integer, ForeignKey("usuarios.id"))
    
    propietario = relationship("Usuario", back_populates="alarmas")