from datetime import datetime, timedelta
from jose import JWTError, jwt
import bcrypt # Usamos el motor criptográfico directo

# CONFIGURACIÓN DE SEGURIDAD
SECRET_KEY = "MI_CLAVE_SUPER_SECRETA_PARA_EL_TFG" 
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

def verify_password(plain_password: str, hashed_password: str) -> bool:
    # Bcrypt requiere que los textos se conviertan a bytes (utf-8) para compararlos
    return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))

def get_password_hash(password: str) -> str:
    # Generamos una "sal" (código aleatorio) y ciframos la contraseña
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    # Lo devolvemos decodificado como texto normal para poder guardarlo en la Base de Datos
    return hashed.decode('utf-8')

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt