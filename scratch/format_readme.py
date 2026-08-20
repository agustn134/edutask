import os

def update_readme():
    filepath = r"c:\Users\agust\StudioProjects\edutask\README.md"
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replacement 1: Add Index
    target_1 = """*(Nota: La aplicación para TV aún no está realizada, ya que está contemplada para la Unidad de aprendizaje III).*

---

### Funcionalidades Principales"""

    replacement_1 = """*(Nota: La aplicación para TV aún no está realizada, ya que está contemplada para la Unidad de aprendizaje III).*

---

### 📖 Documentación Técnica
Para revisar la documentación del código, arquitectura y detalles técnicos de cada módulo, consulta los siguientes enlaces rápidos:
* 📱 **[Módulo Móvil (App)](./app/README.md)** - Aplicación principal para Alumnos, Profesores y Coordinadores.
* ⌚ **[Módulo Wear OS (Reloj)](./wear/README.md)** - Aplicación complementaria para smartwatch de los Profesores.
* 📺 **[Módulo TV (Pantalla)](./tv/README.md)** - Interfaz institucional (dashboard) para Android TV.
* ⚙️ **[Módulo Core](./core/README.md)** - Base de datos, modelos y lógica compartida entre todos los módulos.

---

### Funcionalidades Principales"""
    
    content = content.replace(target_1, replacement_1)
    
    # Replacement 2: Modulo Alumno
    target_2 = """**1. Módulo del Alumno (App Móvil)**

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/50e3f05c-ca6d-425b-9614-43cfeadce754" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/1daa8231-faa9-4112-aa3d-75e39826c815" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/4a8b7735-b963-4a15-bf39-ab2f03637072" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/bb6ba882-8194-45ae-8658-fffca4fa65a8" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/8fd93c65-d2ab-48dd-a281-6f7a7388d2b2" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/72306a37-18ba-494c-9ef4-4bb023ba2b3f" />

<img width="250" height="560" alt="imagen" src="https://github.com/user-attachments/assets/57b0bb28-0165-4b15-a3f4-dbc975c8c2e2" />"""
    
    replacement_2 = """**1. Módulo del Alumno (App Móvil)**

| Inicio | Tareas | Detalle | Enviar Evidencia |
|:---:|:---:|:---:|:---:|
| <img width="200" src="https://github.com/user-attachments/assets/50e3f05c-ca6d-425b-9614-43cfeadce754" /> | <img width="200" src="https://github.com/user-attachments/assets/1daa8231-faa9-4112-aa3d-75e39826c815" /> | <img width="200" src="https://github.com/user-attachments/assets/4a8b7735-b963-4a15-bf39-ab2f03637072" /> | <img width="200" src="https://github.com/user-attachments/assets/bb6ba882-8194-45ae-8658-fffca4fa65a8" /> |
| **Enviada** | **Perfil** | **Calificaciones** | |
| <img width="200" src="https://github.com/user-attachments/assets/8fd93c65-d2ab-48dd-a281-6f7a7388d2b2" /> | <img width="200" src="https://github.com/user-attachments/assets/72306a37-18ba-494c-9ef4-4bb023ba2b3f" /> | <img width="200" src="https://github.com/user-attachments/assets/57b0bb28-0165-4b15-a3f4-dbc975c8c2e2" /> | |"""
    content = content.replace(target_2, replacement_2)
    
    # Replacement 3: Modulo Profesor
    target_3 = """**2. Módulo del Profesor (App Móvil)**

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/9b8e6bf0-d4ec-4683-904b-0b1137760256" />

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/543e949d-1cc7-416d-b779-003705a10e66" />

<img width="698" height="1600" alt="imagen" src="https://github.com/user-attachments/assets/4f3690fa-1984-4889-bfd6-6101e83486cf" />

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/6d1a4e4e-a1c0-4729-9664-bb4f60ad8398" />"""

    replacement_3 = """**2. Módulo del Profesor (App Móvil)**

| Dashboard | Clases | Tareas | Evaluar |
|:---:|:---:|:---:|:---:|
| <img width="200" src="https://github.com/user-attachments/assets/9b8e6bf0-d4ec-4683-904b-0b1137760256" /> | <img width="200" src="https://github.com/user-attachments/assets/543e949d-1cc7-416d-b779-003705a10e66" /> | <img width="200" src="https://github.com/user-attachments/assets/4f3690fa-1984-4889-bfd6-6101e83486cf" /> | <img width="200" src="https://github.com/user-attachments/assets/6d1a4e4e-a1c0-4729-9664-bb4f60ad8398" /> |"""
    content = content.replace(target_3, replacement_3)

    # Replacement 4: Wear OS
    target_4 = """**3. Módulo del Reloj (Wear OS)**

<img width="272" height="256" alt="imagen" src="https://github.com/user-attachments/assets/9cdc26c8-c510-4d5b-a92f-7eb3369f1f93" />

<img width="270" height="251" alt="imagen" src="https://github.com/user-attachments/assets/80eaeb4c-03ce-492d-8dde-98d642634acd" />

<img width="262" height="263" alt="imagen" src="https://github.com/user-attachments/assets/12c391ed-8133-42ec-8546-051d7cf6e8f6" />

<img width="256" height="259" alt="imagen" src="https://github.com/user-attachments/assets/882658f1-e561-480f-9e38-c3330649ea79" />"""
    
    replacement_4 = """**3. Módulo del Reloj (Wear OS)**

| Notificación | Lista | Detalle | Calificación |
|:---:|:---:|:---:|:---:|
| <img width="200" src="https://github.com/user-attachments/assets/9cdc26c8-c510-4d5b-a92f-7eb3369f1f93" /> | <img width="200" src="https://github.com/user-attachments/assets/80eaeb4c-03ce-492d-8dde-98d642634acd" /> | <img width="200" src="https://github.com/user-attachments/assets/12c391ed-8133-42ec-8546-051d7cf6e8f6" /> | <img width="200" src="https://github.com/user-attachments/assets/882658f1-e561-480f-9e38-c3330649ea79" /> |"""
    content = content.replace(target_4, replacement_4)
    
    # Replacement 5: Coordinador
    target_5 = """**4. Módulo del Coordinador**

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/8f2c1097-f242-4eac-ab26-ad22bc7820e4" />

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/3121e975-b86b-4a11-a58c-a59c2105111f" />

<img width="698" height="1600" alt="imagen" src="https://github.com/user-attachments/assets/72c962cc-ebc3-46ae-80ca-3e9b1098229b" />

<img width="720" height="1650" alt="imagen" src="https://github.com/user-attachments/assets/65499877-2e48-4e6a-b88c-9d89478b3128" />"""

    replacement_5 = """**4. Módulo del Coordinador**

| Dashboard | Lista Usuarios | Formulario | Eventos |
|:---:|:---:|:---:|:---:|
| <img width="200" src="https://github.com/user-attachments/assets/8f2c1097-f242-4eac-ab26-ad22bc7820e4" /> | <img width="200" src="https://github.com/user-attachments/assets/3121e975-b86b-4a11-a58c-a59c2105111f" /> | <img width="200" src="https://github.com/user-attachments/assets/72c962cc-ebc3-46ae-80ca-3e9b1098229b" /> | <img width="200" src="https://github.com/user-attachments/assets/65499877-2e48-4e6a-b88c-9d89478b3128" /> |"""
    content = content.replace(target_5, replacement_5)
    
    # Replacement 6: Base de datos
    target_6 = """**6. Base de Datos (Firebase)**

<img width="1920" height="1080" alt="{FCFEF5E6-69CB-4292-8C93-3FA6B453C99A}" src="https://github.com/user-attachments/assets/395bf70a-5647-4b7a-ad19-73a30fd1cf06" />

<img width="1920" height="1080" alt="{55631EB2-2F2E-4DF3-9F86-01498EA9A92A}" src="https://github.com/user-attachments/assets/fc061273-b1c6-45d2-89a9-9e67e141a72a" />"""

    replacement_6 = """**6. Base de Datos (Firebase)**

| Autenticación | Firestore |
|:---:|:---:|
| <img width="400" src="https://github.com/user-attachments/assets/395bf70a-5647-4b7a-ad19-73a30fd1cf06" /> | <img width="400" src="https://github.com/user-attachments/assets/fc061273-b1c6-45d2-89a9-9e67e141a72a" /> |"""
    content = content.replace(target_6, replacement_6)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

update_readme()
