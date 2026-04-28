from sqlalchemy import Column, Integer, String, Float, Boolean
from database import Base

class Alarma(Base):
    __tablename__ = "alarmas"

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, index=True)
    latitud = Column(Float)
    longitud = Column(Float)
    radio = Column(Float)
    is_active = Column(Boolean, default=True)