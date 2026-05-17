from fastapi import FastAPI, Depends, HTTPException, Body
from fastapi.security import OAuth2PasswordRequestForm, OAuth2PasswordBearer
from sqlalchemy.orm import Session
from jose import JWTError, jwt
from pydantic import BaseModel
import models
import schemas
import auth
from database import engine, get_db

# Crea el archivo de la base de datos física si no existe
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="API de GeoAlarmas",
    description="Backend para el TFG de gestión de geovallas"
)

# --- ESQUEMAS LOCALES ---
# (Pequeños esquemas para peticiones concretas que no están en schemas.py)
class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str

class RoleUpdateRequest(BaseModel):
    role: str

# --- DEPENDENCIAS (Seguridad y Control de Acceso) ---

# Le decimos a FastAPI dónde se consigue el token
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="login")

# Portero Nivel 1: Verifica que estás logueado
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

# Portero Nivel 2: Verifica que además eres Administrador
def get_current_admin(current_user: models.Usuario = Depends(get_current_user)):
    # Comprobamos que tenga la chapa de admin
    if current_user.role != "admin":
        raise HTTPException(
            status_code=403, 
            detail="Acceso denegado. No tienes privilegios de administrador."
        )
    return current_user


# =======================================================
#               RUTAS DE AUTENTICACIÓN
# =======================================================

@app.post("/register", response_model=schemas.UsuarioResponse, tags=["Auth"])
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
        # Si no pasamos role, SQLAlchemy pondrá el valor por defecto de models.py
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@app.post("/login", response_model=schemas.Token, tags=["Auth"])
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    # Buscar usuario en BD
    user = db.query(models.Usuario).filter(models.Usuario.email == form_data.username).first()
    
    # Verificar que existe y la contraseña es correcta
    if not user or not auth.verify_password(form_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Email o contraseña incorrectos")
    
    # Generar el Token inyectando el email Y EL ROL (Súper útil para el frontend)
    access_token = auth.create_access_token(data={"sub": user.email, "role": user.role})
    return {"access_token": access_token, "token_type": "bearer"}

@app.put("/usuarios/cambiar-password", tags=["Auth"])
def cambiar_password(
    passwords: ChangePasswordRequest, 
    db: Session = Depends(get_db), 
    current_user: models.Usuario = Depends(get_current_user)
):
    # Comprobamos que sabe su contraseña actual
    if not auth.verify_password(passwords.old_password, current_user.hashed_password):
        raise HTTPException(status_code=400, detail="La contraseña actual es incorrecta")
    
    # Generamos el hash nuevo y guardamos
    new_hashed_pwd = auth.get_password_hash(passwords.new_password)
    current_user.hashed_password = new_hashed_pwd
    db.commit()
    
    return {"mensaje": "Contraseña actualizada con éxito"}


# =======================================================
#               RUTAS DE ALARMAS (Usuario Estándar)
# =======================================================

@app.post("/alarmas/", response_model=schemas.AlarmaResponse, tags=["Alarmas"])
def crear_alarma(alarma: schemas.AlarmaCreate, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    db_alarma = models.Alarma(**alarma.model_dump(), usuario_id=current_user.id)
    db.add(db_alarma)
    db.commit()
    db.refresh(db_alarma)
    return db_alarma

@app.get("/alarmas/", response_model=list[schemas.AlarmaResponse], tags=["Alarmas"])
def leer_alarmas(skip: int = 0, limit: int = 100, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    alarmas = db.query(models.Alarma).filter(models.Alarma.usuario_id == current_user.id).offset(skip).limit(limit).all()
    return alarmas

@app.put("/alarmas/{alarma_id}", response_model=schemas.AlarmaResponse, tags=["Alarmas"])
def actualizar_alarma(
    alarma_id: str, 
    alarma_actualizada: dict = Body(...), 
    db: Session = Depends(get_db), 
    current_user: models.Usuario = Depends(get_current_user)
):
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id, models.Alarma.usuario_id == current_user.id).first()
    
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada o no tienes permisos para editarla")
    
    for key, value in alarma_actualizada.items():
        if hasattr(db_alarma, key) and key != "id": 
            setattr(db_alarma, key, value)
        
    db.commit()
    db.refresh(db_alarma)
    return db_alarma

@app.delete("/alarmas/{alarma_id}", tags=["Alarmas"])
def borrar_alarma(alarma_id: str, db: Session = Depends(get_db), current_user: models.Usuario = Depends(get_current_user)):
    db_alarma = db.query(models.Alarma).filter(models.Alarma.id == alarma_id, models.Alarma.usuario_id == current_user.id).first()
    
    if db_alarma is None:
        raise HTTPException(status_code=404, detail="Alarma no encontrada o no tienes permisos para borrarla")
    
    db.delete(db_alarma)
    db.commit()
    return {"mensaje": "Alarma borrada correctamente"}


# =======================================================
#               RUTAS DE ADMINISTRADOR
# =======================================================

@app.get("/usuarios/", response_model=list[schemas.UsuarioResponse], tags=["Admin"])
def obtener_usuarios(db: Session = Depends(get_db), admin_user: models.Usuario = Depends(get_current_admin)):
    # Solo accesible por admins gracias a get_current_admin
    usuarios = db.query(models.Usuario).all()
    return usuarios

@app.delete("/usuarios/{usuario_id}", tags=["Admin"])
def borrar_usuario(usuario_id: int, db: Session = Depends(get_db), admin_user: models.Usuario = Depends(get_current_admin)): 
    usuario = db.query(models.Usuario).filter(models.Usuario.id == usuario_id).first()
    
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    db.delete(usuario)
    db.commit()
    return {"mensaje": f"Usuario con ID {usuario_id} borrado correctamente de la base de datos"}

@app.put("/usuarios/{usuario_id}/rol", tags=["Admin"])
def actualizar_rol(
    usuario_id: int, 
    role_data: RoleUpdateRequest, 
    db: Session = Depends(get_db), 
    admin_user: models.Usuario = Depends(get_current_admin)
):
    if role_data.role not in ["admin", "user"]:
        raise HTTPException(status_code=400, detail="Rol inválido. Debe ser 'admin' o 'user'.")
    
    usuario = db.query(models.Usuario).filter(models.Usuario.id == usuario_id).first()
    
    if not usuario:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    
    usuario.role = role_data.role
    db.commit()
    
    return {"mensaje": f"El rol del usuario {usuario.email} ha sido actualizado a '{usuario.role}'"}