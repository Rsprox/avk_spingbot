import psycopg2

import bs4
import requests

connection = psycopg2.connect(user="user",
                              password="password",
                              host="localhost",
                              port="5432",
                              database="db")
cursor = connection.cursor()

# 3 страницы /week
# for i in range(1, 4):
#     s = requests.get('https://nekdo.ru/week/{}'.format(i))  # запрос к сайту с анекдотами
#     b = bs4.BeautifulSoup(s.text, "html.parser")  # получаем html код
#     p = b.select('.text')  # ищем блок с текстом анекдота
#     for x in p:  # в цикле заносим анекдот в базу
#         s = x.getText()
#         insert_joke_query = '''INSERT INTO jokes(
#             id, joke)
#             VALUES (nextval('seq_jokes_id'), '{}');'''.format(s)
#
#         cursor.execute(insert_joke_query)
#         connection.commit()

# 100 страниц рандомных
# for i in range(100):
#     s = requests.get('https://nekdo.ru/random')  # запрос к сайту с анекдотами
#     b = bs4.BeautifulSoup(s.text, "html.parser")  # получаем html код
#     p = b.select('.text')  # ищем блок с текстом анекдота
#     for x in p:  # в цикле заносим анекдот в базу
#         s = x.getText()
#         insert_joke_query = '''INSERT INTO jokes(
#             id, joke)
#             VALUES (nextval('seq_jokes_id'), '{}');'''.format(s)
#
#         cursor.execute(insert_joke_query)
#         connection.commit()

# 300 страниц топ-рейтинга с баша
for i in range(1, 301):
    s = requests.get('http://bashorg.org/byrating/page/{}/'.format(i))  # запрос к bash.org
    b = bs4.BeautifulSoup(s.text, "html.parser")  # получаем html код
    p = b.select('.quote')  # ищем блок с текстом цитаты
    for x in p:  # в цикле заносим цитату в базу
        s = x.getText('\n').replace("'", '"')
        insert_joke_query = '''INSERT INTO bash_quots(
            quote_id, t_quote_text)
            VALUES (nextval('seq_bash_quots_id'), '{}');'''.format(s)

        cursor.execute(insert_joke_query)
        connection.commit()
