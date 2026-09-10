import re

with open(r'd:\ProjectQuiz\quizmasterproback\src\main\java\quizmaster\quiz\config\DataInitializer.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Store Items
store_item_regex = re.compile(r'createStoreItem\(\s*"([^"]+)"\s*,\s*"([^"]*)"\s*,\s*(\d+)\s*,\s*ItemType\.(\w+)\s*,\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*.*?CurrencyType\.(\w+)\s*\)')
store_items = store_item_regex.findall(content)

# Titles
title_regex = re.compile(r'createTitle\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*TitleConditionType\.(\w+)\s*,\s*(\d+)\s*\)')
titles = title_regex.findall(content)

with open(r'd:\ProjectQuiz\quizmasterproback\src\main\resources\data_initializer.sql', 'w', encoding='utf-8') as out:
    out.write('-- SQL Generated from DataInitializer.java\n\n')
    out.write('SET NAMES utf8mb4;\n\n')
    
    out.write('-- Store Items\n')
    for item in store_items:
        name, desc, price, itype, val, rarity, ctype = item
        # Escape quotes
        name = name.replace("'", "''")
        desc = desc.replace("'", "''")
        val = val.replace("'", "''")
        out.write(f"INSERT INTO store_items (name, description, price, type, value, rarity, currency_type) VALUES ('{name}', '{desc}', {price}, '{itype}', '{val}', '{rarity}', '{ctype}');\n")
        
    out.write('\n-- Titles\n')
    for title in titles:
        name, desc, ctype, cval = title
        name = name.replace("'", "''")
        desc = desc.replace("'", "''")
        out.write(f"INSERT INTO titles (name, description, condition_type, condition_value) VALUES ('{name}', '{desc}', '{ctype}', {cval});\n")

print(f"Generated {len(store_items)} store items and {len(titles)} titles.")
