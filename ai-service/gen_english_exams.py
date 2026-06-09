"""Generate English exam papers for EMNIST OCR demo"""
from PIL import Image, ImageDraw, ImageFont
import os

test_dir = os.path.join(os.path.dirname(__file__), "test")
os.makedirs(test_dir, exist_ok=True)

try:
    title_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 26)
    body_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 17)
    small_f = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 13)
    hand_f = ImageFont.truetype('C:/Windows/Fonts/segoesc.ttf', 65)
    hand_md = ImageFont.truetype('C:/Windows/Fonts/segoesc.ttf', 40)
except:
    title_f = body_f = small_f = hand_f = hand_md = ImageFont.load_default()

BOX_BG = (248, 250, 255)
BOX_BORDER = (140, 170, 220)
HAND_COLOR = (20, 70, 160)


def header(draw, W, title):
    draw.rectangle([0, 0, W, 55], fill=(25, 55, 140))
    draw.text((30, 12), title, fill=(255, 255, 255), font=title_f)


def answer_box(draw, x, y, ans, font=None):
    f = font or hand_f
    draw.rectangle([x, y-3, x+65, y+52], outline=BOX_BORDER, width=2, fill=BOX_BG)
    draw.text((x+15, y+3), ans, fill=HAND_COLOR, font=f)


def footer(draw, W, H):
    draw.rectangle([0, H-40, W, H], fill=(240, 240, 245))
    draw.text((30, H-30), "Page 1/1 | Please check all answers", fill=(140, 140, 140), font=small_f)


# ============================================================
# Exam 15: 10 Multiple Choice
# ============================================================
W, H = 900, 880
img = Image.new('RGB', (W, H), (255, 255, 255))
d = ImageDraw.Draw(img)
header(d, W, "COMP101: Introduction to Programming - Midterm Exam")

d.rectangle([30, 72, 520, 125], outline=(170, 180, 200), width=2)
d.text((45, 82), "Name: John Smith    ID: 2024001    Section: A", fill=(60, 60, 60), font=body_f)
d.text((45, 105), "Mark the best answer in the box.", fill=(100, 100, 100), font=small_f)

y = 155
qs = [
    ("1. What is print(2+2)?", "B", "A. 2   B. 4   C. 6   D. 8"),
    ("2. Keyword to define a function?", "D", "A. func   B. fn   C. lambda   D. def"),
    ("3. What does CPU stand for?", "A", "A. Central Processing Unit   B. Computer Unit"),
    ("4. Which structure uses FIFO?", "C", "A. Stack   B. Tree   C. Queue   D. Graph"),
    ("5. Binary representation of 5?", "B", "A. 100   B. 101   C. 110   D. 111"),
    ("6. Protocol for web browsing?", "A", "A. HTTP   B. FTP   C. SMTP   D. TCP"),
    ("7. What does RAM stand for?", "C", "A. Read Access Memory   C. Random Access Memory"),
    ("8. Language that runs in browser?", "D", "A. Python B. Java C. C++ D. JavaScript"),
    ("9. Python file extension?", "B", "A. .java   B. .py   C. .cpp   D. .js"),
    ("10. O(n log n) sorting algorithm?", "A", "A. Merge Sort  B. Bubble  C. Insertion  D. Selection"),
]
for q, ans, opts in qs:
    d.text((35, y), q, fill=(30, 30, 30), font=body_f)
    d.text((50, y+25), opts, fill=(100, 100, 100), font=small_f)
    answer_box(d, 750, y+8, ans)
    y += 70

footer(d, W, H)
img.save(f"{test_dir}/15_english_midterm_10q.png")
print("15_english_midterm_10q.png")

# ============================================================
# Exam 16: T/F Quiz
# ============================================================
W2, H2 = 800, 720
img2 = Image.new('RGB', (W2, H2), (255, 255, 255))
d2 = ImageDraw.Draw(img2)
header(d2, W2, "BIO101: Biology - True/False Quiz")

d2.rectangle([30, 70, 480, 115], outline=(170, 180, 200), width=2)
d2.text((45, 80), "Name: Alice Chen    ID: 2024102", fill=(60, 60, 60), font=body_f)
d2.text((45, 100), "Write T or F in the box.", fill=(100, 100, 100), font=small_f)

y2 = 145
tf = [
    ("1. Mitochondria is the powerhouse of the cell.", "T"),
    ("2. DNA stands for Deoxyribonucleic Acid.", "T"),
    ("3. Plants photosynthesize in their roots.", "F"),
    ("4. Humans have 46 chromosomes.", "T"),
    ("5. Bacteria are eukaryotic organisms.", "F"),
    ("6. Water is a polar molecule.", "T"),
    ("7. The heart has four chambers.", "T"),
    ("8. Viruses are considered living organisms.", "F"),
]
for q, ans in tf:
    d2.text((35, y2), q, fill=(30, 30, 30), font=body_f)
    answer_box(d2, 640, y2-3, ans)
    y2 += 68

footer(d2, W2, H2)
img2.save(f"{test_dir}/16_biology_tf_quiz.png")
print("16_biology_tf_quiz.png")

# ============================================================
# Exam 17: Mixed Final (MC + T/F)
# ============================================================
W3, H3 = 950, 1050
img3 = Image.new('RGB', (W3, H3), (255, 255, 255))
d3 = ImageDraw.Draw(img3)
header(d3, W3, "CS201: Data Structures - Final Examination")

d3.rectangle([30, 72, 550, 130], outline=(170, 180, 200), width=2)
d3.text((45, 82), "Name: Bob Wang    ID: 2024155    Class: CS-2A", fill=(60, 60, 60), font=body_f)
d3.text((45, 108), "Total: 100 pts | Duration: 90 min", fill=(90, 90, 90), font=small_f)

y3 = 155
d3.rectangle([25, y3, 925, y3+32], fill=(50, 100, 200))
d3.text((35, y3+5), "Section A: Multiple Choice (6 x 10 = 60 pts)", fill=(255, 255, 255), font=body_f)
y3 += 48
mc = [
    ("1. Which uses LIFO?", "B", "A. Queue   B. Stack   C. Tree   D. Heap"),
    ("2. Binary search time complexity?", "A", "A. O(log n)   B. O(n)   C. O(n^2)   D. O(1)"),
    ("3. NOT a primitive type?", "C", "A. int   B. float   C. array   D. char"),
    ("4. Hash table provides?", "D", "A. Sorting   B. LIFO   C. FIFO   D. O(1) lookup"),
    ("5. Inorder of BST gives?", "A", "A. Sorted   B. Reverse  C. Random  D. Level"),
    ("6. Divide-and-conquer algorithm?", "B", "A. BFS   B. Merge Sort   C. Linear Search   D. Insertion"),
]
for q, ans, opts in mc:
    d3.text((35, y3), q, fill=(30, 30, 30), font=body_f)
    d3.text((50, y3+25), opts, fill=(100, 100, 100), font=small_f)
    answer_box(d3, 780, y3+8, ans)
    y3 += 72

y3 += 10
d3.rectangle([25, y3, 925, y3+32], fill=(20, 160, 110))
d3.text((35, y3+5), "Section B: True/False (4 x 10 = 40 pts)", fill=(255, 255, 255), font=body_f)
y3 += 48
tf2 = [
    ("7. A binary tree has at most 2 children per node.", "T"),
    ("8. QuickSort has O(n^2) worst-case.", "T"),
    ("9. BFS uses a stack.", "F"),
    ("10. Complete graph n vertices = n(n-1)/2 edges.", "T"),
]
for q, ans in tf2:
    d3.text((35, y3), q, fill=(30, 30, 30), font=body_f)
    answer_box(d3, 780, y3-3, ans)
    y3 += 70

footer(d3, W3, H3)
img3.save(f"{test_dir}/17_cs_final_mixed.png")
print("17_cs_final_mixed.png")

# ============================================================
# Exam 18: Large clean answer boxes
# ============================================================
W4, H4 = 750, 880
img4 = Image.new('RGB', (W4, H4), (255, 255, 255))
d4 = ImageDraw.Draw(img4)
header(d4, W4, "MATH101: Calculus - Answer Card")

d4.text((35, 80), "Name:_________  ID:_________", fill=(80, 80, 80), font=body_f)
d4.text((35, 108), "Write ONLY the answer letter in each large box.", fill=(120, 120, 120), font=small_f)

y4 = 150
answers = ['A', 'C', 'D', 'B', 'A', 'D', 'B', 'C', 'A', 'D']
for i, ans in enumerate(answers):
    col = i % 2
    row = i // 2
    x = 50 + col * 360
    yy = y4 + row * 125

    d4.text((x, yy), f"Q{i+1}.", fill=(60, 60, 60), font=body_f)
    bx, by = x+55, yy-12
    # Large box
    d4.rectangle([bx, by, bx+95, by+78], outline=(90, 120, 200), width=3, fill=(245, 248, 255))
    # Big handwritten answer
    d4.text((bx+24, by+6), ans, fill=(15, 60, 150), font=hand_f)

footer(d4, W4, H4)
img4.save(f"{test_dir}/18_large_box_answers.png")
print("18_large_box_answers.png")

print("\nDone! All 4 English exams in", test_dir)
