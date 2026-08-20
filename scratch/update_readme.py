import os
import re

def update_readme(readme_path):
    with open(readme_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern to find the code blocks and their preceding file paths
    # Matches: **Ruta:** `app/...` (or something similar) then optional text, then ```kotlin ... ```
    pattern = re.compile(
        r'(\*\*Ruta:\*\*\s*`([^`]+)`.*?\n\s*```kotlin\n)(.*?)(```)', 
        re.DOTALL
    )

    def replace_code_block(match):
        prefix = match.group(1)
        filepath = match.group(2).strip()
        suffix = "\n" + match.group(4)

        # Build absolute path
        abs_path = os.path.join(r"c:\Users\agust\StudioProjects\edutask", filepath)

        if os.path.exists(abs_path):
            with open(abs_path, 'r', encoding='utf-8') as src_file:
                code_content = src_file.read().strip()
            return prefix + code_content + suffix
        else:
            print(f"File not found: {abs_path}")
            return match.group(0)

    updated_content = pattern.sub(replace_code_block, content)

    with open(readme_path, 'w', encoding='utf-8') as f:
        f.write(updated_content)
    
    print(f"Updated {readme_path}")

def main():
    update_readme(r"c:\Users\agust\StudioProjects\edutask\app\README.md")
    # Also update core/README.md, tv/README.md, wear/README.md if needed
    for module in ['core', 'tv', 'wear']:
        path = rf"c:\Users\agust\StudioProjects\edutask\{module}\README.md"
        if os.path.exists(path):
            update_readme(path)

if __name__ == '__main__':
    main()
