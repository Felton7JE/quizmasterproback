import re

items_to_crystals = {
    'Urso do Conhecimento': 30,
    'O Invisível Cósmico': 50,
    'Ser de Partículas': 50,
    'I\'m Legend': 100,
    'I\'m Rich': 100,
    'I\'m Sorry': 100,
    'No One Beats Me': 100,
    'Try Me': 100,
    'Guardiã do Multiverso': 250,
    'Guardião do Tempo': 250,
    'Mestre do Quiz': 250,
    'Moldura de Energia': 50,
    'Lenda Viva do Quiz!': 20,
    'Emoji: Coroa Dourada 👑': 15,
}

with open('d:/ProjectQuiz/quizmasterproback/src/main/java/quizmaster/quiz/config/DataInitializer.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    replaced = False
    for item, price in items_to_crystals.items():
        if f'\"{item}\"' in line:
            # We need to replace the price and the currency type
            # Find the price, which is before ItemType
            line = re.sub(r',\s*\d+,\s*ItemType\.', f', {price}, ItemType.', line)
            line = line.replace('quizmaster.quiz.enums.CurrencyType.COINS', 'quizmaster.quiz.enums.CurrencyType.CRYSTALS')
            replaced = True
            break
    new_lines.append(line)

with open('d:/ProjectQuiz/quizmasterproback/src/main/java/quizmaster/quiz/config/DataInitializer.java', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
