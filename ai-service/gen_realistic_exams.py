"""Generate exam papers with EMNIST-compatible handwritten answers"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os, random, math

test_dir = os.path.join(os.path.dirname(__file__), "test")
os.makedirs(test_dir, exist_ok=True)

try:
    title_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 26)
    body_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 16)
    small_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 13)
except:
    title_f = body_f = small_f = ImageFont.load_default()

BOX_BG = (248, 250, 255)
BOX_BORDER = (140, 170, 220)
HAND_COLOR = 20

def header(draw, W, title):
    draw.rectangle([0, 0, W, 50], fill=(25, 55, 140))
    draw.text((25, 10), title, fill=(255, 255, 255), font=title_f)

def footer(draw, W, H):
    draw.rectangle([0, H-35, W, H], fill=(240, 240, 245))
    draw.text((25, H-25), "Page 1/1 | Check answers before submitting", fill=(140,140,140), font=small_f)

def draw_realistic_char(draw, char, x, y, size=55):
    """Draw a single character that looks more like real handwriting:
    - Varies position slightly
    - Uses a handwriting font without anti-aliasing where possible
    - Adds slight rotation effect via position jitter
    """
    for attempt in range(6):
        try:
            font_paths = [
                'C:/Windows/Fonts/segoesc.ttf',
                'C:/Windows/Fonts/segoescb.ttf',
                'C:/Windows/Fonts/comic.ttf',
                'C:/Windows/Fonts/calibrib.ttf',
            ]
            fp = font_paths[attempt % len(font_paths)]
            f = ImageFont.truetype(fp, size + random.randint(-6, 6))
            break
        except:
            if attempt == 5:
                f = ImageFont.load_default()

    # Small random position offset
    ox = random.randint(-3, 3)
    oy = random.randint(-3, 3)

    # Draw the character
    draw.text((x + ox, y + oy), char, fill=HAND_COLOR, font=f)

def draw_answer_box(draw, x, y, w, h, answer):
    """Draw an answer box with realistic handwritten answer"""
    draw.rectangle([x, y, x+w, y+h], outline=BOX_BORDER, width=2, fill=BOX_BG)
    # Center the character in the box
    draw_realistic_char(draw, answer, x + w//3, y + h//6, size=h-10)


# ============================================================
# Exam A: 10 MC Questions — clear answer boxes, realistic writing
# ============================================================
W, H = 880, 900
img = Image.new('RGB', (W, H), (255, 255, 255))
d = ImageDraw.Draw(img)
header(d, W, "COMP101: Programming Fundamentals - Answer Sheet")

d.rectangle([25, 65, 520, 120], outline=(170, 180, 200), width=2)
d.text((38, 75), "Name: John Smith      ID: 2024001      Section: A", fill=(60,60,60), font=body_f)
d.text((38, 100), "Mark ONE answer per question.", fill=(100,100,100), font=small_f)

y = 150
qs = [
    "1. What is print(2+2)?                                          A. 2    B. 4    C. 6    D. 8",
    "2. Python function keyword?                                     A. func B. def  C. fn   D. lambda",
    "3. CPU stands for?                                              A. Central Processing Unit    B. Computer Unit",
    "4. FIFO data structure?                                         A. Stack B. Tree C. Queue     D. Graph",
    "5. Binary for decimal 5?                                        A. 100  B. 101  C. 110       D. 111",
    "6. Web browsing protocol?                                       A. HTTP B. FTP  C. SMTP       D. TCP",
    "7. RAM = ?                                                      A. Read Access  C. Random Access Memory",
    "8. Browser language?                                            A. Python B. Java C. C++      D. JavaScript",
    "9. Python file extension?                                       A. .java B. .py  C. .cpp      D. .js",
    "10. O(n log n) sort?                                            A. Merge  B. Bubble C. Insert D. Select",
]
answers = ['B', 'D', 'A', 'C', 'B', 'A', 'C', 'D', 'B', 'A']

for i, (q, ans) in enumerate(zip(qs, answers)):
    d.text((38, y), q, fill=(30, 30, 30), font=body_f)
    # Answer area on the right
    bx = 720
    d.rectangle([bx-5, y-5, bx+65, y+53], outline=(120, 150, 210), width=2, fill=BOX_BG)
    draw_answer_box(d, bx, y, 55, 50, ans)
    y += 68

footer(d, W, H)
img.save(f"{test_dir}/19_comp_mc_realistic.png")
print("19_comp_mc_realistic.png")

# ============================================================
# Exam B: T/F — large letters in individual boxes
# ============================================================
W2, H2 = 780, 750
img2 = Image.new('RGB', (W2, H2), (255, 255, 255))
d2 = ImageDraw.Draw(img2)
header(d2, W2, "BIO101: Cell Biology - True/False Quiz")

d2.rectangle([25, 65, 480, 110], outline=(170,180,200), width=2)
d2.text((38, 75), "Name: Alice Chen    ID: 2024102", fill=(60,60,60), font=body_f)
d2.text((38, 96), "Write T (True) or F (False) in each box.", fill=(100,100,100), font=small_f)

y2 = 140
tfs = [
    ("1. The mitochondria produces ATP for the cell.", "T"),
    ("2. DNA is a double-helix molecule.", "T"),
    ("3. Photosynthesis occurs in the mitochondria.", "F"),
    ("4. Humans have 23 pairs of chromosomes.", "T"),
    ("5. Prokaryotes have a membrane-bound nucleus.", "F"),
    ("6. Water (H2O) is a polar covalent molecule.", "T"),
    ("7. The human heart contains four chambers.", "T"),
    ("8. Viruses can reproduce without a host cell.", "F"),
    ("9. Enzymes are biological catalysts.", "T"),
    ("10. All bacteria cause diseases in humans.", "F"),
]
for q, ans in tfs:
    d2.text((38, y2), q, fill=(30,30,30), font=body_f)
    bx = 620
    d2.rectangle([bx-5, y2-5, bx+60, y2+48], outline=(100, 160, 120), width=2, fill=BOX_BG)
    draw_answer_box(d2, bx, y2, 55, 45, ans)
    y2 += 58

footer(d2, W2, H2)
img2.save(f"{test_dir}/20_bio_tf_realistic.png")
print("20_bio_tf_realistic.png")

# ============================================================
# Exam C: Large isolated answer boxes — best for EMNIST
# ============================================================
W3, H3 = 800, 850
img3 = Image.new('RGB', (W3, H3), (255, 255, 255))
d3 = ImageDraw.Draw(img3)
header(d3, W3, "Answer Card - Multiple Choice")

d3.text((30, 75), "Name:___________     Class:___________     Date:___________", fill=(80,80,80), font=body_f)
d3.text((30, 105), "Write ONE capital letter per box.", fill=(120,120,120), font=small_f)

y3 = 140
# Large answer boxes with big handwritten letters
mc_answers = ['B', 'D', 'A', 'C', 'B', 'A', 'D', 'C', 'B', 'D', 'A', 'C']
for i, ans in enumerate(mc_answers):
    col = i % 3
    row = i // 3
    x = 30 + col * 250
    yy = y3 + row * 155

    d3.text((x+5, yy), f"Q{i+1}", fill=(60,60,60), font=body_f)
    # Extra large answer box
    bx, by = x+60, yy-10
    d3.rectangle([bx, by, bx+90, by+82], outline=(80, 110, 200), width=3, fill=BOX_BG)
    # Big realistic handwritten character
    draw_realistic_char(d3, ans, bx+20, by+8, size=68)

footer(d3, W3, H3)
img3.save(f"{test_dir}/21_large_isolated_boxes.png")
print("21_large_isolated_boxes.png")

print(f"\nDone! Files in {test_dir}/")
