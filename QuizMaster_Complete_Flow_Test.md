# QuizMaster - Teste de Fluxo Completo
## Desde Cadastro até Jogo em Equipe

### Environment Variables no Postman:
```
baseUrl: http://localhost:8080
hostId: (será definido após criar usuário host)
player1Id: (será definido após criar player1)
player2Id: (será definido após criar player2)
player3Id: (será definido após criar player3)
roomCode: (será definido após criar sala)
gameId: (será definido após iniciar jogo)
```

---

## **FASE 1: CADASTRO DE USUÁRIOS**

### **1.1 Criar Host (João)**
```http
POST {{baseUrl}}/api/users
Content-Type: application/json

{
  "username": "joao_host",
  "email": "joao@email.com",
  "fullName": "João Silva (Host)",
  "avatar": "https://example.com/avatar1.jpg"
}
```
**Response esperado:** `{ "id": 1, "username": "joao_host", ... }`
**Ação:** Copie o `id` para a variável `hostId`

### **1.2 Criar Player 1 (Maria)**
```http
POST {{baseUrl}}/api/users
Content-Type: application/json

{
  "username": "maria_player",
  "email": "maria@email.com",
  "fullName": "Maria Santos",
  "avatar": "https://example.com/avatar2.jpg"
}
```
**Ação:** Copie o `id` para a variável `player1Id`

### **1.3 Criar Player 2 (Carlos)**
```http
POST {{baseUrl}}/api/users
Content-Type: application/json

{
  "username": "carlos_player",
  "email": "carlos@email.com",
  "fullName": "Carlos Oliveira",
  "avatar": "https://example.com/avatar3.jpg"
}
```
**Ação:** Copie o `id` para a variável `player2Id`

### **1.4 Criar Player 3 (Ana)**
```http
POST {{baseUrl}}/api/users
Content-Type: application/json

{
  "username": "ana_player",
  "email": "ana@email.com",
  "fullName": "Ana Costa",
  "avatar": "https://example.com/avatar4.jpg"
}
```
**Ação:** Copie o `id` para a variável `player3Id`

---

## **FASE 2: AUTENTICAÇÃO**

### **2.1 Login do Host**
```http
POST {{baseUrl}}/api/auth/login?username=joao_host
```

### **2.2 Login dos Players**
```http
POST {{baseUrl}}/api/auth/login?username=maria_player
POST {{baseUrl}}/api/auth/login?username=carlos_player
POST {{baseUrl}}/api/auth/login?username=ana_player
```

---

## **FASE 3: CRIAÇÃO E CONFIGURAÇÃO DA SALA**

### **3.1 Host Cria Sala**
```http
POST {{baseUrl}}/api/rooms?hostId={{hostId}}
Content-Type: application/json

{
  "name": "Quiz em Equipe - Teste",
  "description": "Sala para teste completo com equipes",
  "maxPlayers": 6,
  "isPrivate": false,
  "password": null,
  "categories": ["MATH", "SCIENCE", "GEOGRAPHY"],
  "difficulty": "MEDIUM",
  "questionCount": 8,
  "timePerQuestion": 25,
  "gameMode": "TEAM"
}
```
**Response esperado:** `{ "roomCode": "ABC123", ... }`
**Ação:** Copie o `roomCode` para a variável `roomCode`

### **3.2 Verificar Detalhes da Sala**
```http
GET {{baseUrl}}/api/rooms/{{roomCode}}
```

### **3.3 Listar Salas Disponíveis**
```http
GET {{baseUrl}}/api/rooms?status=waiting
```

---

## **FASE 4: JOGADORES ENTRAM NA SALA**

### **4.1 Maria Entra na Sala**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/join?userId={{player1Id}}
```

### **4.2 Carlos Entra na Sala**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/join?userId={{player2Id}}
```

### **4.3 Ana Entra na Sala**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/join?userId={{player3Id}}
```

### **4.4 Verificar Jogadores na Sala**
```http
GET {{baseUrl}}/api/rooms/{{roomCode}}/players
```

---

## **FASE 5: CONFIGURAÇÃO DE EQUIPES**

### **5.1 Maria - Equipe AZUL**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player1Id}}/team?team=BLUE
```

### **5.2 Carlos - Equipe AZUL**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player2Id}}/team?team=BLUE
```

### **5.3 Ana - Equipe VERMELHA**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player3Id}}/team?team=RED
```

### **5.4 Verificar Equipes Formadas**
```http
GET {{baseUrl}}/api/rooms/{{roomCode}}/players
```

---

## **FASE 6: PREPARAÇÃO PARA O JOGO**

### **6.1 Jogadores Marcam como "Prontos"**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player1Id}}/ready
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player2Id}}/ready
POST {{baseUrl}}/api/rooms/{{roomCode}}/players/{{player3Id}}/ready
```

### **6.2 Host Atualiza Configurações (Opcional)**
```http
PUT {{baseUrl}}/api/rooms/{{roomCode}}/settings?hostId={{hostId}}
Content-Type: application/json

{
  "name": "Quiz Matemática e Ciências",
  "description": "Jogo em equipe atualizado",
  "maxPlayers": 6,
  "categories": ["MATH", "SCIENCE"],
  "difficulty": "MEDIUM",
  "questionCount": 6,
  "timePerQuestion": 30
}
```

---

## **FASE 7: INÍCIO DO JOGO**

### **7.1 Host Inicia o Jogo**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/start?hostId={{hostId}}
```

### **7.2 Obter Detalhes do Jogo Criado**
```http
GET {{baseUrl}}/api/rooms/{{roomCode}}
```
**Ação:** Procure pelo `gameId` na resposta e copie para a variável `gameId`

### **7.3 Verificar Informações do Jogo**
```http
GET {{baseUrl}}/api/games/{{gameId}}
```

---

## **FASE 8: DURANTE O JOGO - PERGUNTAS E RESPOSTAS**

### **8.1 Obter Perguntas do Jogo (para cada jogador)**
```http
GET {{baseUrl}}/api/games/{{gameId}}/questions?userId={{player1Id}}
GET {{baseUrl}}/api/games/{{gameId}}/questions?userId={{player2Id}}
GET {{baseUrl}}/api/games/{{gameId}}/questions?userId={{player3Id}}
```

### **8.2 Obter Pergunta Atual (Pergunta 1)**
```http
GET {{baseUrl}}/api/games/{{gameId}}/questions/0
```

### **8.3 Jogadores Respondem a Pergunta 1**

**Maria responde (Equipe AZUL):**
```http
POST {{baseUrl}}/api/games/{{gameId}}/answers
Content-Type: application/json

{
  "userId": {{player1Id}},
  "questionId": 1,
  "selectedAnswer": 1,
  "timeSpent": 18000
}
```

**Carlos responde (Equipe AZUL):**
```http
POST {{baseUrl}}/api/games/{{gameId}}/answers
Content-Type: application/json

{
  "userId": {{player2Id}},
  "questionId": 1,
  "selectedAnswer": 1,
  "timeSpent": 22000
}
```

**Ana responde (Equipe VERMELHA):**
```http
POST {{baseUrl}}/api/games/{{gameId}}/answers
Content-Type: application/json

{
  "userId": {{player3Id}},
  "questionId": 1,
  "selectedAnswer": 2,
  "timeSpent": 15000
}
```

### **8.4 Verificar Leaderboard em Tempo Real**
```http
GET {{baseUrl}}/api/games/{{gameId}}/leaderboard/live
```

### **8.5 Obter Pergunta 2**
```http
GET {{baseUrl}}/api/games/{{gameId}}/questions/1
```

### **8.6 Jogadores Respondem a Pergunta 2**
*Repita o processo do 8.3 alterando questionId para 2*

### **8.7 Continuar para mais perguntas...**
*Repita os passos 8.5 e 8.6 para todas as perguntas (até questionIndex = questionCount - 1)*

---

## **FASE 9: FINALIZAÇÃO DO JOGO**

### **9.1 Host Finaliza o Jogo**
```http
POST {{baseUrl}}/api/games/{{gameId}}/finish?hostId={{hostId}}
```

### **9.2 Obter Resultados Finais**
```http
GET {{baseUrl}}/api/games/{{gameId}}/results
```

### **9.3 Verificar Leaderboard Final**
```http
GET {{baseUrl}}/api/games/{{gameId}}/leaderboard
```

### **9.4 Obter Estatísticas do Jogo**
```http
GET {{baseUrl}}/api/games/{{gameId}}/stats
```

---

## **FASE 10: PÓS-JOGO - ANÁLISES**

### **10.1 Verificar Estatísticas dos Usuários**
```http
GET {{baseUrl}}/api/users/{{hostId}}/stats
GET {{baseUrl}}/api/users/{{player1Id}}/stats
GET {{baseUrl}}/api/users/{{player2Id}}/stats
GET {{baseUrl}}/api/users/{{player3Id}}/stats
```

### **10.2 Verificar Histórico de Jogos**
```http
GET {{baseUrl}}/api/users/{{player1Id}}/history
GET {{baseUrl}}/api/users/{{player2Id}}/history
GET {{baseUrl}}/api/users/{{player3Id}}/history
```

### **10.3 Verificar Respostas Individuais**
```http
GET {{baseUrl}}/api/games/{{gameId}}/players/{{player1Id}}/answers
GET {{baseUrl}}/api/games/{{gameId}}/players/{{player2Id}}/answers
GET {{baseUrl}}/api/games/{{gameId}}/players/{{player3Id}}/answers
```

### **10.4 Verificar Ranking Geral**
```http
GET {{baseUrl}}/api/users/ranking?period=global&page=0&size=10
```

---

## **FASE 11: LIMPEZA (OPCIONAL)**

### **11.1 Deletar Sala**
```http
DELETE {{baseUrl}}/api/rooms/{{roomCode}}?hostId={{hostId}}
```

### **11.2 Deletar Usuários de Teste**
```http
DELETE {{baseUrl}}/api/users/{{hostId}}
DELETE {{baseUrl}}/api/users/{{player1Id}}
DELETE {{baseUrl}}/api/users/{{player2Id}}
DELETE {{baseUrl}}/api/users/{{player3Id}}
```

---

## **TESTES ADICIONAIS DE CENÁRIOS**

### **Teste 1: Jogador Sai da Sala**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/leave?userId={{player3Id}}
```

### **Teste 2: Host Expulsa Jogador**
```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/kick?hostId={{hostId}}&playerId={{player2Id}}
```

### **Teste 3: Pausar e Retomar Jogo**
```http
POST {{baseUrl}}/api/games/{{gameId}}/pause?hostId={{hostId}}
POST {{baseUrl}}/api/games/{{gameId}}/resume?hostId={{hostId}}
```

### **Teste 4: Sala Privada com Senha**
```http
POST {{baseUrl}}/api/rooms?hostId={{hostId}}
Content-Type: application/json

{
  "name": "Sala Privada",
  "description": "Apenas com senha",
  "maxPlayers": 4,
  "isPrivate": true,
  "password": "123456",
  "categories": ["MATH"],
  "difficulty": "EASY",
  "questionCount": 5,
  "timePerQuestion": 30,
  "gameMode": "CLASSIC"
}
```

```http
POST {{baseUrl}}/api/rooms/{{roomCode}}/join?userId={{player1Id}}&password=123456
```

---

## **VERIFICAÇÕES IMPORTANTES DURANTE O TESTE**

### ✅ **Checklist de Validação:**

1. **Usuários criados com sucesso**
2. **Autenticação funcionando**
3. **Sala criada com configurações corretas**
4. **Jogadores conseguem entrar na sala**
5. **Equipes são atribuídas corretamente**
6. **Status "pronto" funciona**
7. **Jogo inicia corretamente**
8. **Perguntas são carregadas**
9. **Respostas são registradas**
10. **Leaderboard atualiza em tempo real**
11. **Jogo finaliza corretamente**
12. **Resultados são calculados**
13. **Estatísticas são atualizadas**

### 📊 **Dados para Análise:**

- **Pontuação por equipe**
- **Tempo médio de resposta**
- **Taxa de acerto por jogador**
- **Ranking final**
- **Histórico de jogos**

---

## **SIMULAÇÃO WEBSOCKET (Complementar)**

Para completar o teste, use o HTML WebSocket que forneci anteriormente para simular:

1. **Notificações de entrada/saída de jogadores**
2. **Chat da sala**
3. **Atualizações de equipe em tempo real**
4. **Início do jogo**
5. **Submissão de respostas em tempo real**
6. **Atualizações do leaderboard**
7. **Finalização do jogo**

---

**Este fluxo completo testa toda a funcionalidade do QuizMaster, desde o cadastro até um jogo em equipe com múltiplos jogadores!**
