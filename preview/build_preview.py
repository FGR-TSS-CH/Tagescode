from pathlib import Path
import base64,datetime,json,re,sys
root=Path(__file__).resolve().parents[1]
codes={}
for match in re.finditer(r'(\d{1,2}/\d{1,2}/\d{4})\s+(\d{6})\b',(root/'app/src/main/assets/tagescodes.txt').read_text(encoding='utf-8')):
 try: key=datetime.datetime.strptime(match[1],'%m/%d/%Y').date().isoformat()
 except ValueError: continue
 codes.setdefault(key,match[2])
logo='data:image/png;base64,'+base64.b64encode((root/'app/src/main/res/drawable/videojet_logo.png').read_bytes()).decode()
html=(root/'preview/template.html').read_text(encoding='utf-8-sig').replace('__LOGO__',logo).replace('__CODES__',json.dumps(codes))
output=Path(sys.argv[1]) if len(sys.argv)>1 else root/'preview/index.html'
output.write_text(html,encoding='utf-8')
print(str(output.resolve()))
