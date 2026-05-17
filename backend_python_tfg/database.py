import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base
from dotenv import load_dotenv

# Cargamos las variables ocultas del archivo .env
load_dotenv()

# Obtenemos la URL de la base de datos (por defecto intentará buscar la tuya de Neon)
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL")

# Creamos el motor de conexión

engine = create_engine(SQLALCHEMY_DATABASE_URL)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Dependencia para obtener la sesión de la base de datos
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()