"""Original 2D CRT villager portraits and emerald vignette; standard library only.
Reproducible: python3 tools/create_sale_hud.py. No entity rendering or borrowed art.
"""
from pathlib import Path
import math
import struct
import zlib

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/pepe/textures/gui'
OUT.mkdir(parents=True, exist_ok=True)


def png(name, pixels):
    h, w = len(pixels), len(pixels[0])
    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)
    rows = b''.join(b'\x00' + bytes(c for pixel in row for c in pixel) for row in pixels)
    (OUT / name).write_bytes(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
                             + chunk(b'IDAT', zlib.compress(rows, 9)) + chunk(b'IEND', b''))


def face(mood):
    p = [[(10, 31 + y // 4, 24 + y // 7, 255) for _ in range(64)] for y in range(64)]
    def rect(x0, y0, x1, y1, color):
        rgba = tuple(bytes.fromhex(color)) + (255,)
        for y in range(max(0, y0), min(64, y1)):
            for x in range(max(0, x0), min(64, x1)):
                p[y][x] = rgba
    def line(x0, y0, x1, y1, color, thick=2):
        steps = max(abs(x1-x0), abs(y1-y0), 1)
        for i in range(steps+1):
            x, y = round(x0+(x1-x0)*i/steps), round(y0+(y1-y0)*i/steps)
            rect(x, y, x+thick, y+thick, color)
    # Shoulders, robe and angular ears/head. Heavy outlines stay crisp at GUI scale.
    rect(5, 53, 59, 64, '1b1c19'); rect(8, 55, 56, 64, '79533c')
    rect(18, 57, 46, 64, '49392d'); rect(26, 49, 39, 60, '97734f')
    rect(6, 24, 58, 43, '302820'); rect(8, 26, 56, 41, 'a78260')
    rect(11, 5, 53, 54, '332d21'); rect(13, 7, 51, 52, 'b58f68')
    rect(15, 7, 46, 49, 'd2ae83'); rect(16, 9, 22, 23, 'dfbd92')
    rect(47, 10, 51, 49, '94734f'); rect(16, 47, 47, 53, '8b684b')
    rect(17, 11, 46, 13, 'b49068')
    if mood == 'idle':
        rect(17, 23, 46, 27, '4d4030')
        rect(17, 28, 28, 34, 'ede4c3'); rect(36, 28, 47, 34, 'ede4c3')
        rect(23, 28, 27, 34, '397541'); rect(36, 28, 40, 34, '397541')
        rect(23, 45, 43, 48, '4f3b2b')
    elif mood == 'success':
        line(17, 24, 26, 21, '4d4030'); line(37, 21, 46, 24, '4d4030')
        line(17, 31, 22, 28, '31583c'); line(22, 28, 27, 31, '31583c')
        line(36, 31, 41, 28, '31583c'); line(41, 28, 46, 31, '31583c')
        rect(21, 42, 45, 49, '513726'); rect(23, 42, 43, 45, 'fff1cf')
        rect(16, 36, 22, 39, 'd49077'); rect(43, 36, 49, 39, 'd49077')
    elif mood == 'failure':
        line(17, 21, 27, 25, '49342a', 3); line(36, 25, 46, 21, '49342a', 3)
        rect(17, 30, 28, 34, 'fff0da'); rect(36, 30, 47, 34, 'fff0da')
        rect(24, 30, 27, 34, '34302d'); rect(36, 30, 39, 34, '34302d')
        line(23, 47, 29, 43, '613f30'); line(38, 43, 44, 47, '613f30')
        rect(45, 34, 48, 43, '7fcecb'); rect(46, 42, 49, 45, 'b2eeeb')
    elif mood == 'stack':
        rect(17, 21, 28, 24, '4c3c2a'); rect(36, 21, 47, 24, '4c3c2a')
        for x in [18, 37]:
            rect(x, 27, x+9, 36, '166f50'); rect(x+2, 25, x+7, 38, '34d68c')
            rect(x+2, 27, x+5, 31, 'bdffe1')
        rect(26, 42, 41, 51, '503829'); rect(29, 44, 38, 49, '251e1b')
        rect(4, 6, 8, 17, '38e49a'); rect(1, 9, 11, 13, 'b1ffe3')
        rect(55, 44, 59, 56, '38e49a'); rect(52, 48, 62, 52, 'b1ffe3')
    else:  # Combo 8: ecstatic eyes, grin and celebration sparks.
        line(16, 22, 26, 18, '563c25', 3); line(37, 18, 47, 22, '563c25', 3)
        for x in [18, 38]:
            rect(x+2, 26, x+5, 36, 'e9ffd3'); rect(x-1, 29, x+8, 32, 'f4ffdf')
            rect(x+2, 29, x+5, 32, '28d998')
        rect(20, 41, 46, 50, '503326'); rect(22, 41, 44, 46, 'fff0c1')
        rect(25, 49, 41, 51, 'e2bd80')
        for x, y, c in [(3, 9, 'ffda61'), (55, 4, '51f0dc'), (2, 41, 'f47a91'), (55, 29, 'ffda61')]:
            rect(x+2, y, x+4, y+8, c); rect(x, y+3, x+7, y+5, c)
    # Villager's characteristic long protruding nose, shaded independently of emotion.
    rect(28, 28, 37, 44, '78593e'); rect(28, 27, 34, 41, 'bc946c')
    rect(29, 28, 32, 39, 'd0a77b'); rect(28, 41, 38, 45, '896443')
    png('face_' + mood + '.png', p)


for mood in ['idle', 'success', 'failure', 'stack', 'finale']:
    face(mood)

vignette = []
for y in range(256):
    row = []
    for x in range(256):
        dx, dy = abs((x+0.5-128)/128), abs((y+0.5-128)/128)
        # Superellipse keeps a large completely transparent center and smooth edges.
        r = (dx**4 + dy**4)**0.25
        t = max(0, min(1, (r-0.55)/0.64))
        a = round(78 * t*t*(3-2*t))
        row.append((22, 204, 123, a))
    vignette.append(row)
png('emerald_vignette.png', vignette)
