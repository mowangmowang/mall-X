import zipfile
import xml.etree.ElementTree as ET

def extract_text(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as z:
            xml_content = z.read('word/document.xml')
            tree = ET.fromstring(xml_content)
            ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            text = []
            for p in tree.iterfind('.//w:p', ns):
                p_text = []
                for t in p.iterfind('.//w:t', ns):
                    if t.text:
                        p_text.append(t.text)
                if p_text:
                    text.append(''.join(p_text))
            return '\n'.join(text)
    except Exception as e:
        return str(e)

print('--- KAITI ---')
print(extract_text(r'd:\course\Java\graduateProject\finish\mall\开题报告.docx'))
print('--- RENWU ---')
print(extract_text(r'd:\course\Java\graduateProject\finish\mall\毕业论文（设计）任务书.docx'))
