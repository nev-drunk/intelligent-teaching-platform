"""Create demo submissions with proper UTF-8 encoding"""
import urllib.request
import json
import os

base = os.environ.get('API_BASE_URL', 'http://localhost:8081')
test_dir = os.path.join(os.path.dirname(__file__), 'test')

def upload(task_id, sid, name, filepath, text=None):
    boundary = '---BoundaryDemo'
    body = b''

    if filepath and os.path.exists(filepath):
        fname = os.path.basename(filepath)
        with open(filepath, 'rb') as f:
            fdata = f.read()
        body += f'--{boundary}\r\n'.encode()
        body += f'Content-Disposition: form-data; name="file"; filename="{fname}"\r\n'.encode()
        body += b'Content-Type: image/png\r\n\r\n'
        body += fdata + b'\r\n'

    fields = [
        ('taskId', str(task_id)),
        ('studentId', str(sid)),
        ('studentName', name),
    ]
    if text:
        fields.append(('submitText', text))

    for key, val in fields:
        body += f'--{boundary}\r\n'.encode()
        body += f'Content-Disposition: form-data; name="{key}"\r\n\r\n'.encode()
        body += val.encode('utf-8') + b'\r\n'

    body += f'--{boundary}--\r\n'.encode()

    req = urllib.request.Request(
        f'{base}/api/submission/upload',
        data=body,
        headers={'Content-Type': f'multipart/form-data; boundary={boundary}'}
    )
    resp = urllib.request.urlopen(req)
    return json.loads(resp.read())['data']['id']

# HOMEWORK task 12: text+image submissions
print('=== HOMEWORK (task 12) ===')
data = [
    (1, '李明', '01_answers_ABCD.png',
     'Transformer架构由编码器和解码器组成，核心是自注意力机制和多头注意力。编码器将输入序列映射到隐空间，解码器从隐空间生成输出序列。'),
    (2, '王小红', '03_judge_TFTT.png',
     'Transformer包含编码器和解码器，核心组件是自注意力机制。编码器负责将输入映射到隐空间，解码器生成输出。'),
    (3, '张伟', '28_pure_tf.png',
     '深度学习在自然语言处理中广泛应用。循环神经网络处理序列但存在梯度消失问题，LSTM是改进变体。'),
]
for sid, name, img, txt in data:
    sid = upload(12, sid, name, os.path.join(test_dir, img), txt)
    print(f'  {name}: SID={sid}')

# CHOICE task 13: answer card images
print('=== CHOICE (task 13) ===')
for sid, name, img in [(1, '李明', '22_clean_answer_card_12q.png'), (2, '王小红', '23_tf_card_10q.png')]:
    sid = upload(13, sid, name, os.path.join(test_dir, img))
    print(f'  {name}: SID={sid}')

# EXAM task 14: exam paper images
print('=== EXAM (task 14) ===')
for sid, name, img in [(1, '李明', '11_clean_answer_sheet.png'), (2, '王小红', '12_real_exam_choice_tf_gap.png')]:
    sid = upload(14, sid, name, os.path.join(test_dir, img))
    print(f'  {name}: SID={sid}')

print(f'\nDone! Check {os.environ.get("FRONTEND_URL", "http://localhost:5173")}')
