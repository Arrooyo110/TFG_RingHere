from fastapi import FastAPI, Depends, HTTPException, Body
from fastapi.security import OAuth2PasswordRequestForm, OAuth2PasswordBearer
from sqlalchemy.orm import Session
from jose import JWTError, jwt
import models
import schemas
import auth
from database import engine, get_db
from pydantic import BaseModel

# Crea el archivo de la base de datos física si no existe
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="API de GeoAlarmas",
    description="Backend para el TFG de gestión de geovallas"
)

# Le decimos a FastAPI dónde se consigue el token
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="login")

# Nuestro "portero"
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=401,
        detail="No se pudieron validar las credenciales",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # Desciframos el token usando la clave secreta de auth.py
        payload = jwt.decode(token, auth.SECRET_KEY, algorithms=[auth.ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    # Buscamos al usuario en la base de datos
    user = db.query(models.Usuario).filter(models.Usuario.email == email).first()
    if user is None:
        raise credentials_exception
    return user

# --- RUTAS DE AUTENTICACIÓN ---

@app.post("/register", response_model=schemas.UsuarioResponse)
def register(usuario: schemas.UsuarioCreate, db: Session = Depends(get_db)):
    # Comprobar si el email ya existe
    db_user = db.query(models.Usuario).filter(models.Usuario.email == usuario.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email ya registrado")
    
    # Crear usuario con contraseña cifrada
    hashed_pwd = auth.get_password_hash(usuario.password)
    new_user = models.Usuario(
        email=usuario.email, 
        hashed_password=hashed_pwd, 
        full_name=usuario.full_name
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@app.post("/login", response_model=schemas.Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    # Buscar usuario en BD
    user = db.query(models.Usuario).filter(models.Usuario.email == form_data.username).first()
    # Verificar que existe y la contraseña es correcta
    if not user or not auth.verify_password(form_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Email o contraseña incorrectos")
    
    # Generar el Token
    access_token = auth.create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}


# Endpoint 1: Crear una alarma (Solo para el usuario logueado)
@app.post("/alarmas/", response_model=schemas.AlarmaResponse)
def crear_alarma(alarma: schemas.AlarmaCreate, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    # Añadimos el usuario_id automáticamente sacándolo del token
    db_alarma = models.Alarma(**alarma.model_dump(), usuario_id=current_user.id)
    
    db.add(db_alarma)
    db.commit()
    db.refresh(db_alarma)
    return db_alarma

# Endpoint 2: Leer MIS alarmas
@app.get("/alarmas/", response_model=list[schemas.AlarmaResponse])
def leer_alarmas(skip: int = 0, limit: int = 100, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    # Filtramos la base de datos para que solo devuelva las alarmas de ESTE usuario
    alarmas = db.query(models.Alarma).filter(models.Alarma.usuario_id == current_user.id).offset(skip).limit(limit).all()
    return alarmas

# Endpoint 3: Actualizar una alarma existente (PUT) protegida
@app.put("/alarmas/{alarma_id}", response_model=schemas.AlarmaResponse)
def actualizar_alarma(
    alarma_id: str, 
    alarma_actualizada: dict = Body(...), # Usamos dict para asegurar que recibimos todos los campos de Android (como is_active)
    db: Session = Depends(get_db), 
    current_user: models.Usuario = Depends(get_current_user)
):
    # Buscamos la alarma, asegurándonos de que le pertenece al usuario actual
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id, models.Alarma.usuario_id == current_user.id).first()
    
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada o no tienes permisos para editarla")
    
    # Actualizamos los datos dinámicamente comprobando que la columna exista
    for key, value in alarma_actualizada.items():
        if hasattr(db_alarma, key) and key != "id": # Evitamos sobreescribir la clave primaria accidentalmente
            setattr(db_alarma, key, value)
        
    db.commit()
    db.refresh(db_alarma)
    return db_alarma

# Endpoint 4: Borrar una alarma (DELETE) protegida
@app.delete("/alarmas/{alarma_id}")
def borrar_alarma(alarma_id: str, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    # Buscamos la alarma, asegurándonos de que le pertenece al usuario actual
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id, models.Alarma.usuario_id == current_user.id).first()
    
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada o no tienes permisos para borrarla")
    
    db.delete(db_alarma)
    db.commit()
    return {"mensaje": "Alarma borrada correctamente"}

# --- RUTAS DE ADMINISTRACIÓN (Gestión de usuarios) ---

@app.get("/usuarios/", response_model=list[schemas.UsuarioResponse], tags=["Admin"])
def obtener_usuarios(db: Session = Depends(get_db)):
    # Trae todos los usuarios
    # FastAPI filtrará automáticamente las contraseñas cifradas para que no viajen.
    usuarios = db.query(models.Usuario).all()
    return usuarios

@app.delete("/usuarios/{usuario_id}", tags=["Admin"])
def borrar_usuario(usuario_id: int, db: Session = Depends(get_db)): 
    usuario = db.query(models.Usuario).filter(models.Usuario.id == usuario_id).first()
    
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    db.delete(usuario)
    db.commit()
    return {"mensaje": "Usuario borrado correctamente de la base de datos"}

# --- RUTA PARA CAMBIAR CONTRASEÑA ---

# Pequeño esquema para leer el cuerpo de la petición
from pydantic import BaseModel
class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str

@app.put("/usuarios/cambiar-password", tags=["Auth"])
def cambiar_password(
    passwords: ChangePasswordRequest, 
    db: Session = Depends(get_db), 
    current_user: models.Usuario = Depends(get_current_user) # Protegido con token
):
    # 1. Comprobamos que el usuario sabe su contraseña actual
    if not auth.verify_password(passwords.old_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="La contraseña actual es incorrecta")
    
    # 2. Generamos el hash de la nueva contraseña
    new_hashed_pwd = auth.get_password_hash(passwords.new_password)
    
    # 3. Sobrescribimos y guardamos en la base de datos
    current_user.hashed_password = new_hashed_pwd
    db.commit()
    
    return {"mensaje": "Contraseña actualizada con éxito"}