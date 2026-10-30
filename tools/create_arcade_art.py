"""Hype 1.2 original pixel-art assets. Requires Pillow; deterministic, no game entities.
Run: python tools/create_arcade_art.py. All textures are independently usable PNGs.
"""
from PIL import Image, ImageDraw, ImageFilter
from pathlib import Path
import math, random
OUT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/pepe/textures/gui'
OUT.mkdir(parents=True,exist_ok=True)
MOODS=['idle','blink','focus','success','grin','wink','laugh','proud','failure','angry','sad','shocked','stack','amazed','rich','finale','ecstatic','jackpot']
def portrait(mood):
 im=Image.new('RGBA',(128,128),(9,29,28,255));d=ImageDraw.Draw(im)
 for y in range(128):
  d.line((0,y,127,y),fill=(9+int(5*math.sin(y/50)),28+y//7,30+y//9,255))
 for rad in range(54,20,-5):d.ellipse((64-rad,64-rad,64+rad,64+rad),outline=(18,57+(54-rad),49,255),width=1)
 # Angular robe, stitching and folded collar.
 d.polygon([(8,128),(14,109),(43,98),(84,98),(114,111),(123,128)],fill='#201c20')
 d.polygon([(12,128),(20,111),(45,102),(83,102),(108,113),(117,128)],fill='#75513f')
 for x in range(21,111,4):d.line((x,114,x-2,127),fill='#876048')
 d.polygon([(40,100),(62,113),(50,127),(32,107)],fill='#ae7954')
 d.polygon([(85,100),(64,113),(74,127),(96,108)],fill='#493830')
 d.line((43,103,62,115,53,126),fill='#d49c6c',width=2)
 # Head silhouette, side plane, ears and bevels.
 d.rectangle((18,42,111,81),fill='#322923');d.rectangle((20,45,108,77),fill='#a37e59')
 d.rectangle((25,10,103,103),fill='#342d28')
 d.rectangle((28,12,94,99),fill='#d2ad80');d.rectangle((95,15,102,97),fill='#866348')
 for y in range(15,99):
  t=y/100;d.line((30,y,93,y),fill=(int(222-27*t),int(188-29*t),int(144-28*t)))
 d.line((29,15,91,15),fill='#f0d0a2',width=2);d.line((30,17,30,88),fill='#e8c99a',width=2)
 d.rectangle((93,22,96,91),fill='#aa825e');d.rectangle((33,96,91,101),fill='#906b4c')
 rng=random.Random(932)
 for _ in range(90):
  x,y=rng.randrange(33,91),rng.randrange(20,94)
  if y<40 or y>83:d.rectangle((x,y,x+1,y+1),fill=rng.choice(['#c7a27a','#d8b88e','#d0aa7d']))
 d.line((39,29,81,29),fill='#bc976e');d.line((44,33,76,33),fill='#c39f76')
 # Thick connected brows are a recognizable Minecraft villager feature.
 d.rectangle((35,46,89,51),fill='#514334');d.line((35,46,88,46),fill='#6f5b42')
 happy=mood in ['success','grin','wink','laugh','proud','finale','ecstatic','jackpot']
 if mood in ['angry','failure']:
  d.polygon([(34,42),(55,49),(55,54),(34,47)],fill='#4b3a2d');d.polygon([(69,49),(90,42),(90,47),(69,54)],fill='#4b3a2d')
 if mood in ['amazed','shocked','rich']:d.rectangle((35,41,54,45),fill='#514334');d.rectangle((70,41,89,45),fill='#514334')
 for x in [35,70]:
  d.rectangle((x-1,54,x+20,68),fill='#9b7854');d.rectangle((x,54,x+18,65),fill='#f3eed8')
  if mood=='blink' or (mood=='wink' and x==70):
   d.rectangle((x,53,x+19,67),fill='#d3ae80');d.line((x,62,x+18,62),fill='#574c36',width=2)
  elif mood in ['stack','rich','jackpot']:
   d.polygon([(x+9,52),(x+16,59),(x+10,68),(x+3,61)],fill='#39e6a0');d.line((x+8,55,x+6,60),fill='#d2ffed',width=2)
  elif mood in ['laugh','ecstatic']:
   d.rectangle((x,54,x+19,67),fill='#d3ae80');d.line((x+1,60,x+9,55,x+17,60),fill='#316746',width=3)
  else:
   px=x+(9 if x==35 else 3)+(2 if mood=='focus' else 0)
   d.rectangle((px,54,px+7,65),fill='#377444');d.rectangle((px+2,55,px+5,65),fill='#1d482f');d.point((px+2,55),fill='#c4e3aa')
 # Mouths, teeth, dimples and emotional details.
 if happy:
  d.rectangle((43,83,84,95),fill='#573b31');d.rectangle((46,83,81,89),fill='#f3e9cd')
  d.line((46,89,80,89),fill='#c5b793');d.rectangle((51,93,76,96),fill='#a86f5d')
  if mood in ['grin','proud']:d.rectangle((46,91,81,96),fill='#c79e76')
 elif mood in ['shocked','amazed','stack','rich']:
  d.rounded_rectangle((53,81,75,99),radius=5,fill='#533a2e');d.rectangle((58,85,69,96),fill='#2c2522')
 else:
  d.line((44,91,56,87,73,87,84,91),fill='#674b36',width=3)
 if mood in ['sad','failure']:
  d.line((87,67,87,82),fill='#79dbe0',width=3);d.rectangle((85,80,89,84),fill='#b0f6ee')
 if mood in ['success','wink','laugh']:d.rectangle((34,72,44,76),fill='#cf8c79');d.rectangle((83,72,92,76),fill='#cf8c79')
 # Long cuboid nose with three separately lit planes and nostrils.
 d.rectangle((56,53,72,86),fill='#74533c');d.rectangle((56,52,67,82),fill='#c0986c')
 d.rectangle((57,54,60,79),fill='#dbb184');d.rectangle((67,55,72,82),fill='#9e764e')
 d.rectangle((56,81,74,88),fill='#8a603f');d.rectangle((58,81,70,83),fill='#bf9467')
 d.rectangle((58,85,61,86),fill='#60472f');d.rectangle((68,85,71,86),fill='#60472f')
 if mood in ['finale','ecstatic','jackpot','rich']:
  for x,y,c in [(10,23,'#ff7cce'),(112,35,'#71ffd3'),(12,88,'#ffe89c'),(111,91,'#ff7cce')]:
   d.line((x-3,y,x+3,y),fill=c,width=2);d.line((x,y-4,x,y+4),fill=c,width=2)
 if mood in ['proud','rich','jackpot']:
  d.polygon([(36,13),(33,3),(48,9),(62,0),(76,9),(92,3),(89,13)],fill='#efc474');d.line((37,13,88,13),fill='#fff0b3',width=2)
 return im
for mood in MOODS:portrait(mood).save(OUT/f'face_{mood}.png')
# Cartoon CRT cabinet. Window transparent, bevels and irregular feet fully drawn.
im=Image.new('RGBA',(128,154));d=ImageDraw.Draw(im)
d.line((45,19,30,2),fill='#66847b',width=3);d.line((74,19,98,3),fill='#92ad91',width=3)
d.rounded_rectangle((3,15,125,147),radius=14,fill='#131c27',outline='#ff85cd',width=2)
d.rounded_rectangle((7,18,121,143),radius=12,fill='#3a5356',outline='#79948b',width=2)
d.rounded_rectangle((11,22,117,139),radius=10,fill='#253b40')
d.line((18,22,106,22),fill='#b4d5be',width=2);d.line((9,34,9,128),fill='#688680',width=2)
d.rounded_rectangle((15,33,113,119),radius=10,fill='#050b15',outline='#0a171f',width=3)
d.rectangle((20,37,108,115),fill=(0,0,0,0))
for y in range(130,137,3):d.line((89,y,107,y),fill='#0d2228')
for x in [19,36]:d.ellipse((x,127,x+10,137),fill='#10212b',outline='#71968e',width=1)
d.line((24,129,24,133),fill='#ff92d4',width=2);d.line((41,129,41,133),fill='#8affce',width=2)
d.rounded_rectangle((18,145,40,153),radius=3,fill='#14212b');d.rounded_rectangle((88,145,110,153),radius=3,fill='#14212b')
im.save(OUT/'hype_crt.png')
# Eight full-body animation frames with detailed face, stitched robes and moving arms.
for frame in range(8):
 body=Image.new('RGBA',(128,256));b=ImageDraw.Draw(body)
 b.polygon([(29,102),(99,102),(114,225),(14,225)],fill='#30272a')
 for y in range(105,224):b.line((28-(y-105)//11,y,99+(y-105)//11,y),fill=(122-(y-105)//7,78-(y-105)//10,58-(y-105)//14))
 for x in [37,49,76,89]:b.line((x,117,x-4,217),fill='#513a31',width=2)
 b.rectangle((19,216,108,224),fill='#3b2c29');b.line((21,218,106,218),fill='#b28358',width=2)
 shift=round(math.sin(frame*math.pi/4)*4)
 b.rectangle((29+shift,224,56+shift,251),fill='#302827');b.rectangle((74-shift,224,101-shift,251),fill='#302827')
 b.rectangle((29+shift,246,58+shift,254),fill='#5b4940');b.rectangle((74-shift,246,103-shift,254),fill='#5b4940')
 head=portrait(['grin','success','wink','laugh','proud','success','grin','wink'][frame]).crop((16,0,112,103)).resize((96,103),Image.Resampling.NEAREST)
 mask=Image.new('L',(96,103));md=ImageDraw.Draw(mask)
 md.rectangle((9,10,87,102),fill=255);md.rectangle((2,42,95,81),fill=255);md.rectangle((17,0,76,14),fill=255)
 head.putalpha(mask)
 body.alpha_composite(head,(16,0))
 arm=round(math.sin(frame*math.pi/4)*6)
 b=ImageDraw.Draw(body);b.rounded_rectangle((11,116+arm,43,149+arm),radius=4,fill='#8e6547',outline='#3b3029',width=3)
 b.rounded_rectangle((85,116-arm,117,149-arm),radius=4,fill='#8e6547',outline='#3b3029',width=3)
 b.rectangle((29,137+arm,83,155+arm),fill='#c09b73');b.rectangle((45,145-arm,100,162-arm),fill='#ac855d')
 b.line((32,139+arm,79,139+arm),fill='#e1bc8e',width=2)
 body.save(OUT/('villager_flyby.png' if frame==0 else f'villager_flyby_{frame}.png'))
# Hand-authored bitmap alphabet, not Minecraft's default font. Six moving highlight phases.
patterns={
'A':['01110','11011','11011','11111','11011','11011','11011'],'B':['11110','11011','11011','11110','11011','11011','11110'],
'C':['01111','11000','11000','11000','11000','11000','01111'],'D':['11110','11011','11011','11011','11011','11011','11110'],
'E':['11111','11000','11000','11110','11000','11000','11111'],'F':['11111','11000','11000','11110','11000','11000','11000'],
'G':['01111','11000','11000','11011','11011','11011','01111'],'H':['11011','11011','11011','11111','11011','11011','11011'],
'I':['11111','00100','00100','00100','00100','00100','11111'],'J':['00111','00011','00011','00011','11011','11011','01110'],
'K':['11011','11011','11110','11100','11110','11011','11011'],'L':['11000','11000','11000','11000','11000','11000','11111'],
'M':['11011','11111','11111','11011','11011','11011','11011'],'N':['11011','11111','11111','11011','11011','11011','11011'],
'O':['01110','11011','11011','11011','11011','11011','01110'],'P':['11110','11011','11011','11110','11000','11000','11000'],
'Q':['01110','11011','11011','11011','11111','01110','00011'],'R':['11110','11011','11011','11110','11100','11011','11011'],
'S':['01111','11000','11000','01110','00011','00011','11110'],'T':['11111','00100','00100','00100','00100','00100','00100'],
'U':['11011','11011','11011','11011','11011','11011','01110'],'V':['11011','11011','11011','11011','11011','01110','00100'],
'W':['11011','11011','11011','11011','11111','11111','11011'],'X':['11011','11011','01110','00100','01110','11011','11011'],
'Y':['11011','11011','01110','00100','00100','00100','00100'],'Z':['11111','00011','00110','01100','11000','11000','11111'],
'0':['01110','11011','11011','11011','11011','11011','01110'],'1':['00110','01110','00110','00110','00110','00110','11111'],
'2':['11110','00011','00011','01110','11000','11000','11111'],'3':['11110','00011','00011','01110','00011','00011','11110'],
'4':['11011','11011','11011','11111','00011','00011','00011'],'5':['11111','11000','11000','11110','00011','00011','11110'],
'6':['01111','11000','11000','11110','11011','11011','01110'],'7':['11111','00011','00110','00110','01100','01100','01100'],
'8':['01110','11011','11011','01110','11011','11011','01110'],'9':['01110','11011','11011','01111','00011','00011','11110']}
(OUT/'type').mkdir(exist_ok=True)
for char,rows in patterns.items():
 mask=Image.new('L',(32,48));m=ImageDraw.Draw(mask)
 for y,row in enumerate(rows):
  for x,value in enumerate(row):
   if value=='1':
    shift=(6-y)//2;m.rectangle((5+x*4+shift,7+y*4,8+x*4+shift,10+y*4),fill=255)
 outline=mask.filter(ImageFilter.MaxFilter(7))
 for phase in range(6):
  im=Image.new('RGBA',(32,48));im.paste((24,12,39,255),(1,4),outline);im.paste((16,15,33,255),(0,0),outline)
  paint=Image.new('RGBA',(32,48));p=ImageDraw.Draw(paint)
  for y in range(48):
   shine=max(0,1-abs(y-(phase*7)%36)/5)
   base=(77,238,169) if y<23 else (242,105,200)
   p.line((0,y,31,y),fill=tuple(round(v+(255-v)*shine*.85) for v in base)+(255,))
  im.paste(paint,(0,0),mask);im.save(OUT/'type'/f'{char.lower()}_{phase}.png')
print('Generated',len(MOODS),'portraits, CRT cabinet, 8 animation frames, 216 animated glyph textures')
