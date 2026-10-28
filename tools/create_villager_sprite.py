"""Create original transparent pixel art for the GUI-only villager flyby.
No Minecraft entity, model, or third-party texture is used.
Run: python3 tools/create_villager_sprite.py
"""
from pathlib import Path
import struct
import zlib

WIDTH, HEIGHT = 32, 64
pixels = [[(0, 0, 0, 0) for _ in range(WIDTH)] for _ in range(HEIGHT)]

def rect(x0, y0, x1, y1, color):
    rgba = tuple(bytes.fromhex(color)) + (255,)
    for y in range(y0, y1):
        for x in range(x0, x1):
            pixels[y][x] = rgba

# Boots and long robe, shaded as a small front-facing voxel character.
rect(8, 56, 16, 63, '30251e')
rect(17, 56, 25, 63, '30251e')
rect(9, 56, 14, 60, '574438')
rect(18, 56, 23, 60, '574438')
rect(7, 23, 26, 57, '493329')
rect(9, 24, 24, 55, '78513b')
rect(10, 25, 17, 54, '906144')
rect(22, 25, 25, 56, '5b3c2e')
rect(14, 25, 18, 31, 'c39b76')
rect(14, 32, 18, 47, '654331')
rect(10, 53, 23, 56, '51392b')
# Crossed arms and sleeves.
rect(3, 28, 10, 39, '50372a')
rect(24, 28, 30, 39, '50372a')
rect(5, 30, 10, 37, '996a49')
rect(24, 30, 28, 37, '83583d')
rect(6, 35, 26, 42, '4d3629')
rect(7, 35, 24, 40, 'a57a55')
rect(13, 34, 20, 39, 'c39b76')
rect(17, 36, 24, 40, 'b38a65')
# Head, ears, heavy brows, green eyes and protruding nose.
rect(5, 3, 27, 25, '58442e')
rect(6, 2, 25, 23, 'bf9b76')
rect(8, 3, 22, 21, 'd5b28b')
rect(24, 8, 28, 19, 'a9815b')
rect(4, 9, 7, 18, 'b28c64')
rect(8, 9, 23, 12, '564636')
rect(8, 12, 14, 16, 'ece6c8')
rect(18, 12, 24, 16, 'ece6c8')
rect(11, 12, 14, 16, '3c6c39')
rect(18, 12, 21, 16, '3c6c39')
rect(14, 12, 19, 24, '8c6948')
rect(14, 12, 17, 22, 'b88d62')
rect(14, 22, 19, 25, '79583e')
rect(10, 23, 14, 24, '73533c')
rect(19, 22, 23, 24, '73533c')

def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

raw = b''.join(b'\0' + bytes(channel for pixel in row for channel in pixel) for row in pixels)
image = b'\x89PNG\r\n\x1a\n'
image += chunk(b'IHDR', struct.pack('>IIBBBBB', WIDTH, HEIGHT, 8, 6, 0, 0, 0))
image += chunk(b'IDAT', zlib.compress(raw, 9))
image += chunk(b'IEND', b'')
path = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/pepe/textures/gui/villager_flyby.png'
path.parent.mkdir(parents=True, exist_ok=True)
path.write_bytes(image)
