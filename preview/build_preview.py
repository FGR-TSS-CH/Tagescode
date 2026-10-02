from pathlib import Path
import base64,datetime,json,re,sys,html as html_module
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
if len(sys.argv)<3:
 raise SystemExit('Aufruf: python preview/build_preview.py AUSGABE.html TAGESCODES.txt')
source=Path(sys.argv[2])
codes={}
for match in re.finditer(r'(\d{1,2}/\d{1,2}/\d{4})\s+(\d{6})\b',source.read_text(encoding='utf-8-sig')):
 try: key=datetime.datetime.strptime(match[1],'%m/%d/%Y').date().isoformat()
 except ValueError: continue
 codes.setdefault(key,match[2])
logo='data:image/png;base64,'+base64.b64encode((root/'app/src/main/res/drawable/videojet_logo.png').read_bytes()).decode()
logo_dark='data:image/png;base64,'+base64.b64encode((root/'app/src/main/res/drawable-night/videojet_logo.png').read_bytes()).decode()
vector=ET.parse(root/'app/src/main/res/drawable/ic_swiss_badge.xml').getroot()
android='{http://schemas.android.com/apk/res/android}'
badge=ET.Element('svg', {'xmlns':'http://www.w3.org/2000/svg', 'class':'swiss-badge',
    'viewBox':f"0 0 {vector.get(android+'viewportWidth')} {vector.get(android+'viewportHeight')}",
    'role':'img', 'aria-label':'Schweizer Kreuz'})
for path in vector.findall('path'):
 ET.SubElement(badge,'path',{'fill':path.get(android+'fillColor'),'d':path.get(android+'pathData')})
swiss_badge=ET.tostring(badge,encoding='unicode')
calendar_vector=ET.parse(root/'app/src/main/res/drawable/ic_calendar.xml').getroot()
calendar=ET.Element('svg', {'xmlns':'http://www.w3.org/2000/svg','class':'calendar-icon','viewBox':'0 0 24 24','aria-hidden':'true'})
for path in calendar_vector.findall('path'):
 ET.SubElement(calendar,'path',{'fill':'currentColor','d':path.get(android+'pathData')})
calendar_icon=ET.tostring(calendar,encoding='unicode')
font='data:font/ttf;base64,'+base64.b64encode((root/'preview/assets/Roboto.ttf').read_bytes()).decode()
strings={element.get('name'):''.join(element.itertext()).strip() for element in ET.parse(root/'app/src/main/res/values/strings.xml').getroot()}
version=sys.argv[3] if len(sys.argv)>3 else 'Vorschau'
months=['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember']
today=datetime.date.today()
build_date=f'{months[today.month-1]} {today.year}'
build_info=strings['build_info_format'].replace('%1$s',version).replace('%2$s',build_date)
html=(root/'preview/template.html').read_text(encoding='utf-8-sig').replace('__IMPORTED_AT__',json.dumps(datetime.datetime.now().astimezone().isoformat())).replace('__FONT__',font).replace('__CALENDAR_ICON__',calendar_icon).replace('__DEVELOPED_BY__',html_module.escape(strings['developed_by'])).replace('__BUILD_INFO__',html_module.escape(build_info)).replace('__LOGO__',logo).replace('__LOGO_DARK__',logo_dark).replace('__SWISS_BADGE__',swiss_badge).replace('__CODES__',json.dumps(codes)).replace('__SOURCE_NOTE__',f'{len(codes):,} Tagescodes · Stand {datetime.datetime.now():%d.%m.%Y %H:%M}'.replace(',', '.'))
for icon in (root/'preview/assets/status').glob('*.svg'):
 html=html.replace('__ICON_'+icon.stem+'__','data:image/svg+xml;base64,'+base64.b64encode(icon.read_bytes()).decode())
output=Path(sys.argv[1]) if len(sys.argv)>1 else root/'preview/index.html'
output.write_text(html,encoding='utf-8')
print(json.dumps({'output':str(output.resolve()),'source':str(source),'count':len(codes),'first':min(codes) if codes else None,'last':max(codes) if codes else None}))
