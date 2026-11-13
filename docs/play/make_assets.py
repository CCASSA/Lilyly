"""Render store artwork using the original Android vector. Requires Pillow and Inkscape."""
from pathlib import Path
import tempfile, xml.etree.ElementTree as E, subprocess, math
from PIL import Image, ImageDraw, ImageFont
import sys
out=Path(sys.argv[1]);out.mkdir(parents=True,exist_ok=True)
res=Path(__file__).resolve().parents[2]/'android/app/src/main/res'
ns='{http://schemas.android.com/apk/res/android}'
paths=''.join('<path fill="{}" d="{}"/>'.format(e.attrib[ns+'fillColor'],e.attrib[ns+'pathData']) for e in E.parse(res/'drawable/lilyly_moon_lily.xml').getroot())
with tempfile.TemporaryDirectory() as d:
    svg=Path(d)/'icon.svg';svg.write_text('<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 108 108"><rect width="108" height="108" fill="#271D30"/>'+paths+'</svg>')
    subprocess.run(['inkscape',str(svg),'--export-filename='+str((out/'icon-512.png').resolve())],check=True)
    icon=Image.open(out/'icon-512.png').convert('RGB')
    icon.convert('RGBA').save(out/'icon-512.png')
    im=Image.new('RGB',(1024,500),'#271D30');im.paste(icon.resize((484,484),Image.Resampling.LANCZOS),(12,8))
    draw=ImageDraw.Draw(im)
    for x,y,r in [(48,61,1),(924,117,3),(875,402,2),(541,119,2),(799,100,1),(646,404,2),(119,410,2),(970,328,1)]:
        draw.polygon([(x,y-r*2),(x+r,y),(x,y+r*2),(x-r,y)],fill='#b3a184')
    serif=ImageFont.truetype('DejaVuSerif.ttf',96);small=ImageFont.truetype('DejaVuSans.ttf',23)
    draw.text((492,132),'Lilyly',font=serif,fill='#f3e4cd')
    draw.line((500,269,901,269),fill='#907c62',width=1)
    draw.text((498,289),'A journal for your inner world',font=small,fill='#d9c2ba')
    im.save(out/'feature-1024x500.png')
