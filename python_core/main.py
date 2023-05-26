from PIL import Image
import requests
import time
import random
import hashlib

bot_tokens = []

width, height = 512, 512
current_bot_index = 0

while True:
    # Создание двумерного массива
    collors = [[0] * width for _ in range(height)]

    # Заполнение массива случайными числами
    for i in range(512):
        for j in range(512):
            collors[i][j] = random.randint(1, 16777216)
    nohash = 0

    image = Image.new('RGB', (width, height))

    for h in range(512):
        for w in range(512):
            color = collors[h][w]
            R = (color // 65536) % 256
            G = (color // 256) % 256
            B = color % 256
            image.putpixel((w, h), (R, G, B))
            nohash += color


    temp_file_path = f'img.png'
    image.save(temp_file_path)

    current_bot_token = bot_tokens[current_bot_index]
    files = {'photo': open(temp_file_path, 'rb')}
    ChatID = '-1001966701084'
    post_link = f'https://t.me/TheLibraryofBabelImg'


    hash = hashlib.sha256(str(nohash).encode('utf-8')).hexdigest()

    while True:
        url = f'https://api.telegram.org/bot{current_bot_token}/getChatHistory'
        params = {
            'chat_id': ChatID,
            'text': hash
        }
        response = requests.get(url, params=params)
        if response.status_code != 404:
            break

        url = f'https://api.telegram.org/bot{current_bot_token}/sendPhoto'
        with open(f'{temp_file_path}', 'rb') as photo:
            # Создаем данные для отправки
            data = {'chat_id': ChatID, 'caption': f'{hash} https://t.me/TheLibraryofBabelImg'}
            files = {'photo': photo}
            # Отправляем запрос с помощью метода POST
            response = requests.post(url, data=data, files=files)
        if response.status_code == 200:
            print(f"Image sent successfully using Bot {current_bot_index+1}")
            #time.sleep(0.2)
            break  # Выход из цикла, если отправка прошла успешно
        print(f"Failed to send image: {response.text}")
        print("Retrying after 5 seconds...")
        time.sleep(5)  # Пауза перед повторной попыткой отправки
    current_bot_index = (current_bot_index + 1) % len(bot_tokens)