import os
import re
import datetime

# Regular expression to match function definitions
# Matches: annotations?, modifiers?, fun, name
fun_regex = re.compile(r'^(\s*)(?:@[A-Za-z0-9_]+\s*)*(?:suspend\s+|private\s+|override\s+|public\s+|internal\s+)*fun\s+(?:<[^>]+>\s+)?([A-Za-z0-9_]+)\s*\(', re.MULTILINE)

# Spanish docstring generator based on function name heuristics
def get_docstring(func_name, indent):
    doc = f"{indent}/**\n"
    if func_name.endswith("Screen"):
        doc += f"{indent} * Componente de interfaz de usuario para la pantalla {func_name}.\n"
        doc += f"{indent} * Muestra los elementos visuales y maneja las interacciones del usuario.\n"
    elif func_name.endswith("Content"):
        doc += f"{indent} * Componente interno que renderiza el contenido de {func_name}.\n"
    elif func_name.endswith("Card") or func_name.endswith("Item") or func_name.endswith("Accordion"):
        doc += f"{indent} * Componente visual reutilizable para renderizar {func_name}.\n"
    elif func_name.startswith("on") or func_name.startswith("handle"):
        doc += f"{indent} * Manejador de evento para la accion {func_name}.\n"
    elif func_name.startswith("fetch") or func_name.startswith("load") or func_name.startswith("get"):
        doc += f"{indent} * Obtiene o recupera datos asociados a {func_name} desde la base de datos o API.\n"
    elif func_name.startswith("save") or func_name.startswith("update") or func_name.startswith("set"):
        doc += f"{indent} * Guarda o actualiza los datos de {func_name} en la base de datos.\n"
    elif func_name.startswith("delete") or func_name.startswith("remove"):
        doc += f"{indent} * Elimina el registro correspondiente a {func_name} del sistema.\n"
    elif func_name.startswith("abrir") or func_name.startswith("open"):
        doc += f"{indent} * Abre el recurso o vista {func_name} para la interaccion del usuario.\n"
    elif func_name.startswith("decode") or func_name.startswith("encode") or func_name.startswith("compress"):
        doc += f"{indent} * Realiza el procesamiento y conversion de archivos ({func_name}).\n"
    else:
        doc += f"{indent} * Metodo principal que ejecuta la operacion: {func_name}.\n"
        doc += f"{indent} * Contiene la logica de negocio y control de flujo.\n"
    
    doc += f"{indent} * @param param Parametros de entrada (depende de la firma).\n"
    doc += f"{indent} * @return Retorna el resultado de la operacion o Unit si es un componente.\n"
    doc += f"{indent} */\n"
    return doc

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Step 1: Update the top-level file header
    header_pattern = re.compile(r'^/\*\*.*?\*/', re.DOTALL)
    
    author_str = "\n * @author Agustin Parra, Carlos Palma\n * @date Agosto 2026\n *"
    
    if header_pattern.match(content):
        # Header exists, append author and date if not present
        header = header_pattern.match(content).group(0)
        if "@author" not in header:
            new_header = header.replace('*/', f'{author_str}/')
            content = content.replace(header, new_header, 1)
    else:
        # No header, insert one
        filename = os.path.basename(filepath)
        new_header = f"/**\n * Archivo de codigo fuente: {filename}\n * Documentacion generada para revision del proyecto.{author_str}/\n"
        content = new_header + content

    # Step 2: Inject function docstrings
    lines = content.split('\n')
    new_lines = []
    
    i = 0
    while i < len(lines):
        line = lines[i]
        match = fun_regex.match(line)
        if match:
            indent = match.group(1)
            func_name = match.group(2)
            
            # Check if there is already a docstring just above
            # Iterate backwards ignoring empty lines and annotations
            has_doc = False
            j = i - 1
            while j >= 0:
                prev_line = lines[j].strip()
                if prev_line == "":
                    j -= 1
                    continue
                if prev_line.startswith("@"):
                    j -= 1
                    continue
                if prev_line == "*/":
                    has_doc = True
                break
            
            if not has_doc:
                docstring = get_docstring(func_name, indent)
                new_lines.append(docstring.rstrip('\n'))
        
        new_lines.append(line)
        i += 1
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('\n'.join(new_lines))

def main():
    search_dir = r"c:\Users\agust\StudioProjects\edutask"
    count = 0
    for root, dirs, files in os.walk(search_dir):
        for file in files:
            if file.endswith(".kt"):
                process_file(os.path.join(root, file))
                count += 1
    print(f"Procesados {count} archivos.")

if __name__ == '__main__':
    main()
